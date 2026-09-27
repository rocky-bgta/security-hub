package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.registration.SubPackageAssignRequestDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientTrainingAssignmentService {

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final RegistrationServiceClient registrationServiceClient;

    public void assignTrainingSubPackageIfNeeded(CampaignRecipient recipient, RecipientStatus triggerStatus) {
        try {
            Optional<Campaign> campaignOpt = campaignRepository.findById(recipient.getCampaignId());
            if (campaignOpt.isEmpty()) {
                return;
            }
            Campaign campaign = campaignOpt.get();

            if (recipient.isTrainingAssigned()) {
                log.debug("Training already assigned for recipient {}, skipping re-assignment", recipient.getId());
                return;
            }

            if (!shouldAssignTraining(campaign, triggerStatus)) {
                log.info("No training assignment needed for recipient {}, campaign {}, triggerStatus={}",
                        recipient.getId(), campaign.getId(), triggerStatus);
                return;
            }

            CampaignTrainingData trainingData = campaign.getTrainingData();
            if (trainingData == null || trainingData.getTrainingModuleId() == null) {
                return;
            }
            String userId = recipient.getUserId();
            if (userId == null || userId.isBlank()) {
                log.warn("Cannot assign training sub-package: recipient userId is blank, trackingId={}",
                        recipient.getTrackingId());
                return;
            }

            Long validForDays = computeValidForDays(trainingData.getCompletionDays());

            SubPackageAssignRequestDto.CompletionDays completionDaysDto;
            if (trainingData.getCompletionDays() != null
                    && trainingData.getCompletionDays().getDurationUnit() != null
                    && trainingData.getCompletionDays().getDurationValue() != null) {
                completionDaysDto = SubPackageAssignRequestDto.CompletionDays.builder()
                        .durationUnit(trainingData.getCompletionDays().getDurationUnit())
                        .durationValue(trainingData.getCompletionDays().getDurationValue())
                        .build();
            } else {
                completionDaysDto = SubPackageAssignRequestDto.CompletionDays.builder()
                        .durationUnit("DAYS")
                        .durationValue(7)
                        .build();
            }

            SubPackageAssignRequestDto.SubPackageData data = SubPackageAssignRequestDto.SubPackageData.builder()
                    .subPackageId(trainingData.getTrainingModuleId())
                    .userIdList(List.of(userId))
                    .productId(trainingData.getProductId())
                    .packageId(trainingData.getPackageId())
                    .productPackageId(trainingData.getProductPackageId())
                    .clientAdminId(trainingData.getClientId())
                    .status("ACTIVE")
                    .validFor(validForDays)
                    .channel(campaign.getChannel() != null ? campaign.getChannel().name() : null)
                    .completionDays(completionDaysDto)
                    .enableFirstUserNotificationEmail(true)
                    .secondaryEmails(List.of())
                    .thirdLevelEmails(List.of())
                    .fourthHREmails(List.of())
                    .subPackageName(trainingData.getName())
                    .build();

            SubPackageAssignRequestDto assignRequest = SubPackageAssignRequestDto.builder()
                    .subPackageData(List.of(data))
                    .build();

            registrationServiceClient.assignSubPackageToUser(assignRequest);
            log.info("Assigned training sub-package {} to user {} for campaign {} on trigger {}",
                    trainingData.getTrainingModuleId(), userId, campaign.getId(), triggerStatus);

            markTrainingAssigned(recipient, campaign);
        } catch (Exception e) {
            log.error("Failed to assign training sub-package for recipient {}: {}",
                    recipient.getId(), e.getMessage(), e);
        }
    }

    public void markTrainingAssigned(CampaignRecipient recipient, Campaign campaign) {
        Instant now = Instant.now();
        recipient.setTrainingAssigned(true);
        recipient.setTrainingAssignedAt(now);
        recipientRepository.save(recipient);

        try {
            campaignRepository.findById(campaign.getId()).ifPresent(latest -> {
                CampaignStats stats = latest.getStats();
                if (stats == null) {
                    stats = new CampaignStats();
                    latest.setStats(stats);
                }
                stats.setTrainingAssignedCount(stats.getTrainingAssignedCount() + 1);
                stats.setLastUpdatedAt(now);
                campaignRepository.save(latest);
            });
        } catch (Exception e) {
            log.error("Failed to increment trainingAssignedCount for campaign {}: {}",
                    campaign.getId(), e.getMessage());
        }
    }

    public boolean shouldAssignTraining(Campaign campaign, RecipientStatus triggerStatus) {
        if (campaign == null || campaign.getCampaignType() == null) {
            return false;
        }
        if (campaign.getCampaignType() == CampaignType.SIMULATED_PHISHING
                || campaign.getCampaignType() == CampaignType.SMISHING_SIMULATION
                || campaign.getCampaignType() == CampaignType.VISHING_SIMULATION) {
            return false;
        }
        if (blocksTrainingAssignment(campaign)) {
            return false;
        }

        CampaignTrainingData trainingData = campaign.getTrainingData();
        if (trainingData == null || trainingData.getAssignedFor() == null) {
            return isClickTrigger(triggerStatus) || isCompromiseTrigger(triggerStatus);
        }

        SubPackageAssignedFor assignedFor = trainingData.getAssignedFor();
        return switch (assignedFor) {
            case SIMULATED_PHISHING -> false;
            case PHISHING_WITH_TRAINING, PHISHING_TRAINING_FOR_ALL, PHISHING_TRAINING_FOR_CLICKS ->
                    isClickTrigger(triggerStatus);
            case PHISHING_TRAINING_FOR_COMPROMISES -> isCompromiseTrigger(triggerStatus);
        };
    }

    private boolean isClickTrigger(RecipientStatus triggerStatus) {
        return triggerStatus == RecipientStatus.CLICKED || triggerStatus == RecipientStatus.VOICE_ENGAGED;
    }

    private boolean isCompromiseTrigger(RecipientStatus triggerStatus) {
        return triggerStatus == RecipientStatus.DATA_SUBMITTED || triggerStatus == RecipientStatus.COMPROMISED;
    }

    private boolean blocksTrainingAssignment(Campaign campaign) {
        if (campaign.isExpired()) {
            return true;
        }
        CampaignStatus status = campaign.getStatus();
        return status == CampaignStatus.COMPLETED || status == CampaignStatus.EXPIRED
                || status == CampaignStatus.CANCELLED;
    }

    private long computeValidForDays(CampaignTrainingData.CompletionDays completionDays) {
        if (completionDays == null || completionDays.getDurationUnit() == null) {
            return 7L;
        }
        String unit = completionDays.getDurationUnit().trim().toUpperCase(Locale.ROOT);
        int value = completionDays.getDurationValue();
        return switch (unit) {
            case "DAYS" -> value;
            case "WEEKS" -> value * 7L;
            case "MONTHS" -> value * 30L;
            default -> 7L;
        };
    }
}
