package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.exception.TransientVoiceFailureException;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.service.VishingCallAudioService;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.voice.VoiceCallRequest;
import com.aspire.asat.phishing.voice.VoiceCallResult;
import com.aspire.asat.phishing.voice.VoiceProvider;
import com.aspire.asat.phishing.voice.VoiceProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignVoiceConsumer {

    private static final String TWIML_SUFFIX = "/twiml";
    private static final String STATUS_SUFFIX = "/twilio-status";

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final VoiceServerConfigurationRepository voiceServerConfigurationRepository;
    private final VishingCallLogRepository callLogRepository;
    private final VoiceProviderFactory voiceProviderFactory;
    private final VishingCallAudioService vishingCallAudioService;
    private final RecipientRiskScoringService recipientRiskScoringService;
    private final CampaignCompletionEvaluator completionEvaluator;

    @Value("${tracking.base-url}")
    private String trackingBaseUrl;

    public void processVoiceMessage(CampaignVoiceMessage message) {
        log.info("Initiating voice call to {} for campaign {}", message.getToPhone(), message.getCampaignId());

        CampaignRecipient recipient = recipientRepository.findById(message.getRecipientId()).orElse(null);
        if (recipient == null) {
            log.warn("Recipient {} not found; skipping voice message", message.getRecipientId());
            return;
        }
        if (!isEligibleForCall(recipient.getStatus())) {
            log.info("Recipient {} in status {}; skipping duplicate voice call",
                    recipient.getId(), recipient.getStatus());
            return;
        }

        VoiceServerConfiguration config = voiceServerConfigurationRepository.findById(message.getVoiceServerConfigurationId())
                .orElseThrow(() -> new IllegalStateException("Voice server configuration not found"));

        VoiceProvider provider = voiceProviderFactory.create(config);
        VoiceCallResult result;
        try {
            String audioS3Key = vishingCallAudioService.synthesizeForCall(
                    message.getClientId(),
                    message.getVoiceCloneProvider(),
                    message.getExternalVoiceId(),
                    message.getRenderedScript(),
                    message.getLanguage());

            upsertQueuedCallLog(message, recipient, audioS3Key);

            result = provider.initiateCall(VoiceCallRequest.builder()
                    .toPhone(message.getToPhone())
                    .scriptBody(message.getRenderedScript())
                    .callerId(message.getCallerId())
                    .externalVoiceId(message.getExternalVoiceId())
                    .trackingId(message.getTrackingId())
                    .twimlUrl(buildCallbackUrl(message.getTrackingId(), TWIML_SUFFIX))
                    .statusCallbackUrl(buildCallbackUrl(message.getTrackingId(), STATUS_SUFFIX))
                    .build());
        } catch (Exception e) {
            if (isTransientFailure(e)) {
                throw new TransientVoiceFailureException("Transient voice failure", e);
            }
            result = VoiceCallResult.fail(e.getMessage());
        }

        if (result.success()) {
            onInitiateSuccess(message);
        } else {
            onInitiateFailure(message, result.errorMessage());
        }
    }

    private boolean isEligibleForCall(RecipientStatus status) {
        return status == RecipientStatus.PENDING || status == RecipientStatus.CALL_QUEUED;
    }

    private void upsertQueuedCallLog(CampaignVoiceMessage message, CampaignRecipient recipient, String audioS3Key) {
        VishingCallLog logEntry = callLogRepository.findByTrackingId(message.getTrackingId())
                .orElseGet(() -> VishingCallLog.builder()
                        .campaignId(message.getCampaignId())
                        .campaignName(message.getCampaignName())
                        .clientId(message.getClientId())
                        .recipientId(recipient.getId())
                        .trackingId(message.getTrackingId())
                        .recipientName(recipient.getFullName())
                        .phoneNumber(message.getToPhone())
                        .build());
        logEntry.setRenderedScript(message.getRenderedScript());
        logEntry.setAudioS3Key(audioS3Key);
        if (logEntry.getStatus() == null) {
            logEntry.setStatus(RecipientStatus.CALL_QUEUED);
        }
        callLogRepository.save(logEntry);
    }

    private String buildCallbackUrl(String trackingId, String suffix) {
        String base = trackingBaseUrl.endsWith("/")
                ? trackingBaseUrl.substring(0, trackingBaseUrl.length() - 1)
                : trackingBaseUrl;
        return base + WebApiUrlConstants.VOICE_BASE_PATH + "/calls/" + trackingId + suffix;
    }

    private void onInitiateSuccess(CampaignVoiceMessage message) {
        Instant now = Instant.now();
        String recipientEmail = recipientRepository.findById(message.getRecipientId()).map(recipient -> {
            recipient.setStatus(RecipientStatus.CALL_QUEUED);
            recipient.setCallQueuedAt(now);
            recipientRepository.save(recipient);
            return recipient.getEmail();
        }).orElse(null);

        saveActivity(message, ActivityType.VOICE_INITIATED, recipientEmail);
        incrementStat(message.getCampaignId(), ActivityType.VOICE_INITIATED, false);

        recipientRepository.findById(message.getRecipientId())
                .ifPresent(recipient -> recipientRiskScoringService.updateUserRiskProfilePhishingScore(
                        recipient, ActivityType.VOICE_INITIATED));
    }

    private void onInitiateFailure(CampaignVoiceMessage message, String error) {
        Instant now = Instant.now();
        String recipientEmail = recipientRepository.findById(message.getRecipientId()).map(recipient -> {
            recipient.setStatus(RecipientStatus.CALL_FAILED);
            recipient.setCallFailedAt(now);
            recipientRepository.save(recipient);
            return recipient.getEmail();
        }).orElse(null);

        saveActivity(message, ActivityType.VOICE_FAILED, recipientEmail);
        incrementStat(message.getCampaignId(), ActivityType.VOICE_FAILED, true);
        log.error("Voice call initiation failed for {}: {}", message.getToPhone(), error);
    }

    private void saveActivity(CampaignVoiceMessage message, ActivityType activityType, String recipientEmail) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            if (message.getToPhone() != null) {
                metadata.put("phoneNumber", message.getToPhone());
            }
            String identity = (recipientEmail != null && !recipientEmail.isBlank())
                    ? recipientEmail
                    : message.getToPhone();
            EmailActivity activity = EmailActivity.builder()
                    .clientId(message.getClientId())
                    .campaignId(message.getCampaignId())
                    .recipientId(message.getRecipientId())
                    .recipientEmail(identity)
                    .campaignName(message.getCampaignName())
                    .trackingId(message.getTrackingId())
                    .activityType(activityType)
                    .channel(message.getChannel() != null ? message.getChannel() : CampaignChannel.VOICE)
                    .timestamp(Instant.now())
                    .metadata(metadata)
                    .build();
            emailActivityRepository.save(activity);
        } catch (Exception e) {
            log.error("Failed to save voice activity: {}", e.getMessage());
        }
    }

    private void incrementStat(String campaignId, ActivityType type, boolean failed) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (campaign.getStats() != null) {
                if (type == ActivityType.VOICE_INITIATED) {
                    campaign.getStats().setCallsTotal(campaign.getStats().getCallsTotal() + 1);
                } else if (failed) {
                    campaign.getStats().setCallsFailed(campaign.getStats().getCallsFailed() + 1);
                }
                campaign.getStats().setLastUpdatedAt(Instant.now());
            }
            completionEvaluator.evaluateAndApply(campaign);
            campaignRepository.save(campaign);
        });
    }

    private boolean isTransientFailure(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("throttl") || msg.contains("rate")
                || msg.contains("temporary") || msg.contains("unavailable");
    }
}
