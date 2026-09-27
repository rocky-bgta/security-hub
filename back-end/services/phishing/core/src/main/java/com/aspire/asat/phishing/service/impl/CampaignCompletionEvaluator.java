package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Decides when a RUNNING campaign should transition to COMPLETED.
 *
 * <p>Completion rules:
 * <ul>
 *   <li>Expiry always wins: an expired campaign is never completed here (it is handled by the
 *       expiry scheduler / lazy tracking checks).</li>
 *   <li>{@code SIMULATED_PHISHING} / {@code SMISHING_SIMULATION}: completed once every recipient
 *       has recorded a qualifying engagement ({@code OPENED}, {@code CLICKED},
 *       {@code DATA_SUBMITTED}, {@code REPORTED}) or is {@code BOUNCED} (terminal, cannot engage).
 *       Simply sending the email/SMS ({@code SENT}/{@code DELIVERED}) is not enough.</li>
 *   <li>{@code PHISHING_WITH_TRAINING} / {@code SMISHING_WITH_TRAINING}: completed once all
 *       messages are processed AND at least one non-bounced recipient was assigned training AND
 *       every such recipient has completed training in CMS. If nobody was assigned training, the
 *       campaign stays {@code RUNNING} until {@code expiresAt} ({@code EXPIRED}).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignCompletionEvaluator {

    private static final List<RecipientStatus> SIMULATED_BLOCKING_STATUSES =
            List.of(RecipientStatus.PENDING, RecipientStatus.SENT, RecipientStatus.DELIVERED);

    private static final List<RecipientStatus> VOICE_SIMULATED_BLOCKING_STATUSES =
            List.of(RecipientStatus.PENDING, RecipientStatus.CALL_QUEUED, RecipientStatus.CALL_RINGING);

    private static final List<RecipientStatus> VOICE_PROCESSING_BLOCKING_STATUSES =
            List.of(RecipientStatus.PENDING, RecipientStatus.CALL_QUEUED, RecipientStatus.CALL_RINGING);

    private final CampaignRecipientRepository recipientRepository;

    /**
     * Evaluates completion for the given campaign and, when satisfied, mutates it to COMPLETED
     * in memory. The caller is responsible for persisting the campaign.
     *
     * @return {@code true} if the status was changed to COMPLETED, {@code false} otherwise.
     */
    public boolean evaluateAndApply(Campaign campaign) {
        if (campaign == null || campaign.getId() == null) {
            return false;
        }
        if (campaign.getStatus() != CampaignStatus.RUNNING) {
            return false;
        }
        // Expiry takes precedence over completion.
        if (campaign.isExpired()) {
            return false;
        }
        if (campaign.getCampaignType() == CampaignType.SIMULATED_PHISHING
                || campaign.getCampaignType() == CampaignType.SMISHING_SIMULATION) {
            if (!isSimulatedEngagementComplete(campaign.getId())) {
                return false;
            }
        } else if (campaign.getCampaignType() == CampaignType.VISHING_SIMULATION) {
            if (!isVoiceSimulatedEngagementComplete(campaign.getId())) {
                return false;
            }
        } else if (campaign.getCampaignType() == CampaignType.VISHING_WITH_TRAINING) {
            if (!allVoiceCallsProcessed(campaign.getId())) {
                return false;
            }
            if (!isTrainingComplete(campaign)) {
                return false;
            }
        } else {
            // Email/SMS "with training" (PHISHING_WITH_TRAINING, SMISHING_WITH_TRAINING).
            if (!allMessagesProcessed(campaign.getId())) {
                return false;
            }
            if (!isTrainingComplete(campaign)) {
                return false;
            }
        }

        markCompleted(campaign);
        return true;
    }

    private boolean isSimulatedEngagementComplete(String campaignId) {
        if (recipientRepository.countByCampaignId(campaignId) == 0) {
            return false;
        }
        return recipientRepository.countByCampaignIdAndStatusIn(campaignId, SIMULATED_BLOCKING_STATUSES) == 0;
    }

    private boolean isVoiceSimulatedEngagementComplete(String campaignId) {
        if (recipientRepository.countByCampaignId(campaignId) == 0) {
            return false;
        }
        return recipientRepository.countByCampaignIdAndStatusIn(campaignId, VOICE_SIMULATED_BLOCKING_STATUSES) == 0;
    }

    private boolean allVoiceCallsProcessed(String campaignId) {
        return recipientRepository.countByCampaignIdAndStatusIn(campaignId, VOICE_PROCESSING_BLOCKING_STATUSES) == 0
                && recipientRepository.countByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING) == 0;
    }

    private boolean allMessagesProcessed(String campaignId) {
        return recipientRepository.countByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING) == 0;
    }

    private boolean isTrainingComplete(Campaign campaign) {
        long assigned = recipientRepository.countByCampaignIdAndTrainingAssignedTrue(campaign.getId());
        if (assigned == 0) {
            return false;
        }
        long completed = recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(
                campaign.getId());
        return completed >= assigned;
    }

    private void markCompleted(Campaign campaign) {
        Instant now = Instant.now();
        campaign.setStatus(CampaignStatus.COMPLETED);
        if (campaign.getCompletedAt() == null) {
            campaign.setCompletedAt(now);
        }
        if (campaign.getCampaignType() == CampaignType.PHISHING_WITH_TRAINING
                || campaign.getCampaignType() == CampaignType.SMISHING_WITH_TRAINING
                || campaign.getCampaignType() == CampaignType.VISHING_WITH_TRAINING) {
            if (campaign.getTrainingCompletedAt() == null) {
                campaign.setTrainingCompletedAt(now);
            }
        }
        if (campaign.getStats() != null) {
            campaign.getStats().setLastUpdatedAt(now);
        }
        log.info("Campaign {} marked COMPLETED (type={})", campaign.getId(), campaign.getCampaignType());
    }
}

