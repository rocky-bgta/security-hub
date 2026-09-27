package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.PhishingRiskScore;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.dto.request.VoiceCallStatusRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.VoiceIngestionService;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.service.support.RecipientTrainingAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceIngestionServiceImpl implements VoiceIngestionService {

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final VishingCallLogRepository callLogRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final RecipientTrainingAssignmentService recipientTrainingAssignmentService;
    private final RecipientRiskScoringService recipientRiskScoringService;
    private final CampaignCompletionEvaluator completionEvaluator;

    @Override
    public void recordCallStatus(String trackingId, VoiceCallStatusRequest request) {
        CampaignRecipient recipient = recipientRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found for tracking id"));

        if (isCampaignExpired(recipient.getCampaignId())) {
            log.debug("Skipping voice status for expired campaign, trackingId={}", trackingId);
            return;
        }

        RecipientStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new PhishingValidationException("Call status is required");
        }

        advanceStatus(recipient, newStatus);
        Instant now = Instant.now();
        switch (newStatus) {
            case CALL_QUEUED -> recipient.setCallQueuedAt(now);
            case ANSWERED -> recipient.setCallAnsweredAt(now);
            case CALL_FAILED, NO_ANSWER -> recipient.setCallFailedAt(now);
            default -> { }
        }
        recipientRepository.save(recipient);

        ActivityType activityType = mapStatusActivity(newStatus);
        if (activityType != null) {
            saveActivity(recipient, activityType);
            incrementVoiceStat(recipient.getCampaignId(), activityType, request.getRetries());
            recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, activityType);
        }
    }

    @Override
    public void processCallResult(String trackingId, VoiceCallResultRequest request) {
        CampaignRecipient recipient = recipientRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found for tracking id"));

        Campaign campaign = campaignRepository.findById(recipient.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        if (isCampaignExpired(recipient.getCampaignId())) {
            log.debug("Skipping voice result for expired campaign, trackingId={}", trackingId);
            return;
        }

        VishingCallOutcome outcome = request.getOutcome();
        if (outcome == null) {
            throw new PhishingValidationException("Call outcome is required");
        }

        VishingCallLog existingLog = callLogRepository.findByTrackingId(recipient.getTrackingId()).orElse(null);
        VishingCallOutcome previousOutcome = existingLog != null ? existingLog.getOutcome() : null;
        if (previousOutcome != null && outcomeRank(previousOutcome) >= outcomeRank(outcome)) {
            // Twilio retries/redeliveries must not double-count stats or regress a
            // recipient who already reached an equal/higher-severity outcome.
            log.debug("Skipping voice result for trackingId={}: existing outcome {} >= incoming {}",
                    trackingId, previousOutcome, outcome);
            return;
        }

        RecipientStatus finalStatus = mapOutcomeToStatus(outcome);
        recipient.setStatus(finalStatus);
        recipient.setRiskScore(resolveRiskScore(outcome));
        Instant now = Instant.now();
        Instant answeredAt = request.getStartedAt() != null ? request.getStartedAt() : now;
        switch (outcome) {
            case ANSWERED -> recipient.setCallAnsweredAt(answeredAt);
            case ENGAGED -> recipient.setCallAnsweredAt(answeredAt);
            case COMPROMISED -> {
                recipient.setCallAnsweredAt(answeredAt);
                recipient.setCallCompromisedAt(now);
                if (request.getSensitiveDataCaptured() != null && !request.getSensitiveDataCaptured().isEmpty()) {
                    recipient.setSubmittedData(new HashMap<>(request.getSensitiveDataCaptured()));
                }
            }
            case ANSWERED_BUT_REPORTED -> {
                recipient.setCallAnsweredAt(answeredAt);
                recipient.setReportedAt(now);
            }
            case REPORTED_WITHOUT_ANSWER -> recipient.setReportedAt(now);
            case NO_ANSWER, FAILED -> recipient.setCallFailedAt(now);
        }
        recipientRepository.save(recipient);

        upsertCallLog(recipient, campaign, request, finalStatus);

        ActivityType activityType = mapOutcomeActivity(outcome);
        saveActivity(recipient, activityType);
        incrementVoiceOutcomeStats(recipient.getCampaignId(), previousOutcome, outcome, request.getRetries());
        recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, activityType);

        if (outcome == VishingCallOutcome.ENGAGED) {
            recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.VOICE_ENGAGED);
        } else if (outcome == VishingCallOutcome.COMPROMISED) {
            recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.COMPROMISED);
        }
    }

    /**
     * Monotonic severity ranking used to make {@link #processCallResult} idempotent:
     * a new outcome is only applied when it is strictly more severe than the one
     * already recorded, so retries never double-count or regress a recipient.
     */
    private int outcomeRank(VishingCallOutcome outcome) {
        return switch (outcome) {
            case COMPROMISED -> 5;
            case ANSWERED_BUT_REPORTED, REPORTED_WITHOUT_ANSWER -> 4;
            case ENGAGED -> 3;
            case ANSWERED -> 2;
            case NO_ANSWER, FAILED -> 1;
        };
    }

    private double resolveRiskScore(VishingCallOutcome outcome) {
        return switch (outcome) {
            case ANSWERED -> PhishingRiskScore.VOICE_ANSWERED.getPoints();
            case ENGAGED -> PhishingRiskScore.VOICE_ENGAGED.getPoints();
            case COMPROMISED -> PhishingRiskScore.VOICE_COMPROMISED.getPoints();
            case ANSWERED_BUT_REPORTED -> PhishingRiskScore.VOICE_ANSWERED_BUT_REPORTED.getPoints();
            case NO_ANSWER, FAILED, REPORTED_WITHOUT_ANSWER -> PhishingRiskScore.VOICE_NO_ANSWER.getPoints();
        };
    }

    private void upsertCallLog(CampaignRecipient recipient, Campaign campaign,
                               VoiceCallResultRequest request, RecipientStatus status) {
        VishingCallLog logEntry = callLogRepository.findByTrackingId(recipient.getTrackingId())
                .orElseGet(() -> VishingCallLog.builder()
                        .campaignId(recipient.getCampaignId())
                        .campaignName(campaign.getCampaignName())
                        .clientId(recipient.getClientId())
                        .recipientId(recipient.getId())
                        .trackingId(recipient.getTrackingId())
                        .recipientName(recipient.getFullName())
                        .phoneNumber(recipient.getPhoneNumber())
                        .build());

        logEntry.setStatus(status);
        logEntry.setOutcome(request.getOutcome());
        logEntry.setDurationSeconds(request.getDurationSeconds());
        logEntry.setRetries(request.getRetries());
        logEntry.setRecordingS3Key(request.getRecordingS3Key());
        logEntry.setTranscript(request.getTranscript());
        logEntry.setDetectedKeywords(request.getDetectedKeywords() != null
                ? new ArrayList<>(request.getDetectedKeywords()) : new ArrayList<>());
        logEntry.setSensitiveDataCaptured(request.getSensitiveDataCaptured() != null
                ? new HashMap<>(request.getSensitiveDataCaptured()) : new HashMap<>());
        logEntry.setSttLatencyMs(request.getSttLatencyMs());
        logEntry.setLlmLatencyMs(request.getLlmLatencyMs());
        logEntry.setTtsLatencyMs(request.getTtsLatencyMs());
        logEntry.setStartedAt(request.getStartedAt());
        logEntry.setEndedAt(request.getEndedAt());
        callLogRepository.save(logEntry);
    }

    private void incrementVoiceStat(String campaignId, ActivityType type, int retries) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            CampaignStats stats = ensureStats(campaign);
            switch (type) {
                case VOICE_ANSWERED -> stats.setCallsAnswered(stats.getCallsAnswered() + 1);
                case VOICE_ENGAGED -> stats.setCallsEngaged(stats.getCallsEngaged() + 1);
                case VOICE_REPORTED -> stats.setCallsReported(stats.getCallsReported() + 1);
                case VOICE_COMPROMISED -> stats.setCallsCompromised(stats.getCallsCompromised() + 1);
                case VOICE_NO_ANSWER -> stats.setCallsNoAnswer(stats.getCallsNoAnswer() + 1);
                case VOICE_FAILED -> stats.setCallsFailed(stats.getCallsFailed() + 1);
                default -> { }
            }
            applyRetriesAndSave(campaign, stats, retries);
        });
    }

    /**
     * Funnel-aware campaign stats for a call result.
     * COMPROMISED ⊆ ENGAGED ⊆ ANSWERED; only missing tiers are incremented on upgrade.
     */
    private void incrementVoiceOutcomeStats(String campaignId, VishingCallOutcome previous,
                                            VishingCallOutcome outcome, int retries) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            CampaignStats stats = ensureStats(campaign);

            boolean wasAnswered = isAnsweredTier(previous);
            boolean wasEngaged = isEngagedTier(previous);
            boolean wasCompromised = previous == VishingCallOutcome.COMPROMISED;
            boolean wasReported = isReportedOutcome(previous);
            boolean wasNoAnswer = previous == VishingCallOutcome.NO_ANSWER;
            boolean wasFailed = previous == VishingCallOutcome.FAILED;

            if (isAnsweredTier(outcome) && !wasAnswered) {
                stats.setCallsAnswered(stats.getCallsAnswered() + 1);
            }
            if (isEngagedTier(outcome) && !wasEngaged) {
                stats.setCallsEngaged(stats.getCallsEngaged() + 1);
            }
            if (outcome == VishingCallOutcome.COMPROMISED && !wasCompromised) {
                stats.setCallsCompromised(stats.getCallsCompromised() + 1);
            }
            if (isReportedOutcome(outcome) && !wasReported) {
                stats.setCallsReported(stats.getCallsReported() + 1);
            }
            if (outcome == VishingCallOutcome.NO_ANSWER && !wasNoAnswer) {
                stats.setCallsNoAnswer(stats.getCallsNoAnswer() + 1);
            }
            if (outcome == VishingCallOutcome.FAILED && !wasFailed) {
                stats.setCallsFailed(stats.getCallsFailed() + 1);
            }

            applyRetriesAndSave(campaign, stats, retries);
        });
    }

    private static boolean isAnsweredTier(VishingCallOutcome outcome) {
        return outcome == VishingCallOutcome.ANSWERED
                || outcome == VishingCallOutcome.ENGAGED
                || outcome == VishingCallOutcome.COMPROMISED
                || outcome == VishingCallOutcome.ANSWERED_BUT_REPORTED;
    }

    private static boolean isEngagedTier(VishingCallOutcome outcome) {
        return outcome == VishingCallOutcome.ENGAGED
                || outcome == VishingCallOutcome.COMPROMISED;
    }

    private static boolean isReportedOutcome(VishingCallOutcome outcome) {
        return outcome == VishingCallOutcome.ANSWERED_BUT_REPORTED
                || outcome == VishingCallOutcome.REPORTED_WITHOUT_ANSWER;
    }

    private CampaignStats ensureStats(Campaign campaign) {
        CampaignStats stats = campaign.getStats();
        if (stats == null) {
            stats = new CampaignStats();
            campaign.setStats(stats);
        }
        return stats;
    }

    private void applyRetriesAndSave(Campaign campaign, CampaignStats stats, int retries) {
        if (retries > 0) {
            stats.setRetriesTriggered(stats.getRetriesTriggered() + retries);
        }
        stats.setLastUpdatedAt(Instant.now());
        completionEvaluator.evaluateAndApply(campaign);
        campaignRepository.save(campaign);
    }

    private void saveActivity(CampaignRecipient recipient, ActivityType activityType) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            if (recipient.getPhoneNumber() != null) {
                metadata.put("phoneNumber", recipient.getPhoneNumber());
            }
            String identity = StringUtils.hasText(recipient.getEmail())
                    ? recipient.getEmail()
                    : recipient.getPhoneNumber();
            EmailActivity activity = EmailActivity.builder()
                    .clientId(recipient.getClientId())
                    .campaignId(recipient.getCampaignId())
                    .recipientId(recipient.getId())
                    .recipientEmail(identity)
                    .campaignName(recipient.getCampaignName())
                    .trackingId(recipient.getTrackingId())
                    .activityType(activityType)
                    .channel(CampaignChannel.VOICE)
                    .timestamp(Instant.now())
                    .metadata(metadata)
                    .build();
            emailActivityRepository.save(activity);
        } catch (Exception e) {
            log.error("Failed to save voice ingestion activity: {}", e.getMessage());
        }
    }

    private RecipientStatus mapOutcomeToStatus(VishingCallOutcome outcome) {
        return switch (outcome) {
            case ANSWERED -> RecipientStatus.ANSWERED;
            case ENGAGED -> RecipientStatus.VOICE_ENGAGED;
            case COMPROMISED -> RecipientStatus.COMPROMISED;
            case NO_ANSWER -> RecipientStatus.NO_ANSWER;
            case FAILED -> RecipientStatus.CALL_FAILED;
            case ANSWERED_BUT_REPORTED, REPORTED_WITHOUT_ANSWER -> RecipientStatus.REPORTED;
        };
    }

    private ActivityType mapOutcomeActivity(VishingCallOutcome outcome) {
        return switch (outcome) {
            case ANSWERED -> ActivityType.VOICE_ANSWERED;
            case ENGAGED -> ActivityType.VOICE_ENGAGED;
            case COMPROMISED -> ActivityType.VOICE_COMPROMISED;
            case NO_ANSWER -> ActivityType.VOICE_NO_ANSWER;
            case FAILED -> ActivityType.VOICE_FAILED;
            case ANSWERED_BUT_REPORTED, REPORTED_WITHOUT_ANSWER -> ActivityType.VOICE_REPORTED;
        };
    }

    private ActivityType mapStatusActivity(RecipientStatus status) {
        return switch (status) {
            case ANSWERED -> ActivityType.VOICE_ANSWERED;
            case NO_ANSWER -> ActivityType.VOICE_NO_ANSWER;
            case CALL_FAILED -> ActivityType.VOICE_FAILED;
            default -> null;
        };
    }

    private void advanceStatus(CampaignRecipient recipient, RecipientStatus newStatus) {
        if (recipient.getStatus().ordinal() < newStatus.ordinal()) {
            recipient.setStatus(newStatus);
        }
    }

    private boolean isCampaignExpired(String campaignId) {
        Optional<Campaign> campaignOpt = campaignRepository.findById(campaignId);
        return campaignOpt.map(Campaign::isExpired).orElse(false);
    }
}
