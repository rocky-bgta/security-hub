package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.request.*;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Mapper for converting between Campaign entities and DTOs.
 */
@Component
public class CampaignMapper {

    private static final int TOTAL_STEPS = 9;

    /**
     * Convert Campaign entity to DTO
     */
    public CampaignDto toDto(Campaign entity) {
        if (entity == null) {
            return null;
        }

        return CampaignDto.builder()
                .campaignId(entity.getId())
                .campaignName(entity.getCampaignName())
                .campaignType(entity.getCampaignType())
                .channel(entity.getChannel() != null ? entity.getChannel() : com.aspire.asat.phishing.dto.enums.CampaignChannel.EMAIL)
                .productPackageId(entity.getProductPackageId())
                .assignedFor(entity.getAssignedFor())
                .responseStages(entity.getResponseStages())
                .learningMode(entity.getLearningMode())
                .status(entity.getStatus())
                .emailTemplateId(entity.getEmailTemplateId())
                .voiceData(entity.getVoiceData())
                .landingPageId(entity.getLandingPageId())
                .trackingDomainId(entity.getTrackingDomainId())
                .landingPageType(entity.getLandingPageType())
                .voiceScenario(entity.getVoiceScenario())
                .senderProfileId(entity.getSenderProfileId())
                .smsServerConfigurationId(entity.getSmsServerConfigurationId())
                .voiceServerConfigurationId(entity.getVoiceServerConfigurationId())
                .telephonyData(entity.getTelephonyData())
                .campaignTags(entity.getCampaignTags())
                .audience(toAudienceDto(entity.getAudience()))
                .trainingData(entity.getTrainingData())
                .schedule(toScheduleDto(entity.getSchedule()))
                .stats(toStatsDto(entity.getStats()))
                .currentStep(entity.getCurrentStep())
                .totalSteps(TOTAL_STEPS)
                .isComplete(entity.getCurrentStep() >= TOTAL_STEPS)
                .canEdit(entity.isEditable())
                .canLaunch(entity.canLaunch())
                .canPause(entity.canPause())
                .canResume(entity.canResume())
                .canCancel(entity.canCancel())
                .canDelete(entity.getStatus() == CampaignStatus.DRAFT)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .launchedAt(entity.getLaunchedAt())
                .completedAt(entity.getCompletedAt())
                .expireDate(entity.getExpireDate())
                .expiresAt(entity.getExpiresAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    /**
     * Convert CampaignAudience to DTO
     */
    public CampaignAudienceDto toAudienceDto(CampaignAudience audience) {
        if (audience == null) {
            return null;
        }

        return CampaignAudienceDto.builder()
                .type(audience.getType())
                .departmentIds(audience.getDepartmentIds())
                .groupIds(audience.getGroupIds())
                .userIds(audience.getUserIds())
                .recipientCount(audience.getRecipientCount())
                .build();
    }

    /**
     * Convert CampaignSchedule to DTO
     */
    public CampaignScheduleDto toScheduleDto(CampaignSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        CampaignScheduleDto.CampaignScheduleDtoBuilder builder = CampaignScheduleDto.builder()
                .type(schedule.getType())
                .startDateTime(schedule.getStartDateTime())
                .endDateTime(schedule.getEndDateTime())
                .timeZone(schedule.getTimeZone());

        if (schedule.getSendingConfig() != null) {
            builder.sendingPattern(schedule.getSendingConfig().getPattern())
                   .batchSize(schedule.getSendingConfig().getBatchSize())
                   .batchIntervalMinutes(schedule.getSendingConfig().getBatchIntervalMinutes());
        }

        if (schedule.getRecurring() != null) {
            builder.recurringFrequency(schedule.getRecurring().getFrequency())
                   .recurringDescription(buildRecurringDescription(schedule.getRecurring()));
        }

        return builder.build();
    }

    /**
     * Convert CampaignStats to DTO
     */
    public CampaignStatsDto toStatsDto(CampaignStats stats) {
        if (stats == null) {
            stats = new CampaignStats();
        }

        int totalRecipients = stats.getTotalRecipients();

        double deliveryRate = stats.getEmailsSent() > 0 
            ? (double) stats.getEmailsDelivered() / stats.getEmailsSent() * 100 : 0;
        double openRate = totalRecipients > 0
            ? (double) stats.getEmailsOpened() / totalRecipients * 100 : 0;
        double clickRate = totalRecipients > 0
            ? (double) stats.getLinksClicked() / totalRecipients * 100 : 0;
        double submissionRate = totalRecipients > 0
            ? (double) stats.getDataSubmitted() / totalRecipients * 100 : 0;
        double reportRate = totalRecipients > 0
            ? (double) stats.getEmailsReported() / totalRecipients * 100 : 0;

        return CampaignStatsDto.builder()
                .totalRecipients(totalRecipients)
                .emailsSent(stats.getEmailsSent())
                .emailsDelivered(stats.getEmailsDelivered())
                .emailsOpened(stats.getEmailsOpened())
                .linksClicked(stats.getLinksClicked())
                .attachmentsOpened(stats.getAttachmentsOpened())
                .dataSubmitted(stats.getDataSubmitted())
                .emailsReported(stats.getEmailsReported())
                .emailsBounced(stats.getEmailsBounced())
                .smsSent(stats.getSmsSent())
                .smsDelivered(stats.getSmsDelivered())
                .smsFailed(stats.getSmsFailed())
                .callsTotal(stats.getCallsTotal())
                .callsAnswered(stats.getCallsAnswered())
                .callsCompromised(stats.getCallsCompromised())
                .callsNoAnswer(stats.getCallsNoAnswer())
                .callsFailed(stats.getCallsFailed())
                .retriesTriggered(stats.getRetriesTriggered())
                .deliveryRate(deliveryRate)
                .openRate(openRate)
                .clickRate(clickRate)
                .submissionRate(submissionRate)
                .reportRate(reportRate)
                .lastUpdatedAt(stats.getLastUpdatedAt())
                .build();
    }

    /**
     * Convert CampaignRecipient to DTO
     */
    public CampaignRecipientDto toRecipientDto(CampaignRecipient recipient) {
        if (recipient == null) {
            return null;
        }

        return CampaignRecipientDto.builder()
                .recipientId(recipient.getId())
                .campaignId(recipient.getCampaignId())
                .userId(recipient.getUserId())
                .email(recipient.getEmail())
                .firstName(recipient.getFirstName())
                .lastName(recipient.getLastName())
                .fullName(recipient.getFullName())
                .department(recipient.getDepartment())
                .phoneNumber(recipient.getPhoneNumber())
                .countryName(recipient.getCountryName())
                .status(recipient.getStatus())
                .emailSentAt(recipient.getEmailSentAt())
                .emailOpenedAt(recipient.getEmailOpenedAt())
                .linkClickedAt(recipient.getLinkClickedAt())
                .dataSubmittedAt(recipient.getDataSubmittedAt())
                .reportedAt(recipient.getReportedAt())
                .openCount(recipient.getOpenCount())
                .clickCount(recipient.getClickCount())
                .hasSubmittedData(!recipient.getSubmittedData().isEmpty())
                .build();
    }

    /**
     * Create Campaign entity from create request
     */
    public Campaign toEntity(CampaignCreateRequest request, String clientId) {
        return Campaign.builder()
                .clientId(clientId)
                .campaignName(request.getCampaignName())
                .campaignType(request.getCampaignType())
                .channel(request.getChannel() != null ? request.getChannel() : com.aspire.asat.phishing.dto.enums.CampaignChannel.EMAIL)
                .productPackageId(trimToNull(request.getProductPackageId()))
                .assignedFor(request.getAssignedFor())
                .status(CampaignStatus.DRAFT)
                .currentStep(1)
                .campaignTags(new ArrayList<>())
                .stats(new CampaignStats())
                .build();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Apply audience request to campaign
     */
    public void applyAudienceRequest(Campaign campaign, CampaignAudienceRequest request) {
        CampaignAudience audience = CampaignAudience.builder()
                .type(request.getAudienceType())
                .departmentIds(request.getDepartmentIds() != null ? request.getDepartmentIds() : new ArrayList<>())
                .groupIds(request.getGroupIds() != null ? request.getGroupIds() : new ArrayList<>())
                .userIds(request.getUserIds() != null ? request.getUserIds() : new ArrayList<>())
                .build();
        campaign.setAudience(audience);
    }

    /**
     * Apply schedule request to campaign
     */
    public void applyScheduleRequest(
            Campaign campaign,
            CampaignScheduleRequest request,
            Instant startDateTime,
            Instant endDateTime,
            String timeZoneDisplay) {
        SendingConfig sendingConfig = SendingConfig.builder()
                .pattern(request.getSendingPattern())
                .batchSize(request.getBatchSize() != null ? request.getBatchSize() : 0)
                .batchIntervalMinutes(request.getBatchIntervalMinutes() != null ? request.getBatchIntervalMinutes() : 0)
                .build();

        RecurringConfig recurringConfig = null;
        if (request.getRecurringFrequency() != null && !request.getRecurringFrequency().isBlank()) {
            recurringConfig = RecurringConfig.builder()
                    .frequency(request.getRecurringFrequency())
                    .daysOfWeek(request.getDaysOfWeek())
                    .dayOfMonth(request.getDayOfMonth())
                    .timeOfDay(request.getTimeOfDay())
                    .repeatCount(request.getRepeatCount())
                    .neverExpires(request.getNeverExpires() != null && request.getNeverExpires())
                    .build();
        }

        CampaignSchedule schedule = CampaignSchedule.builder()
                .type(request.getScheduleType())
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .timeZone(timeZoneDisplay != null ? timeZoneDisplay : "UTC")
                .sendingConfig(sendingConfig)
                .recurring(recurringConfig)
                .build();

        campaign.setSchedule(schedule);
    }

    private String buildRecurringDescription(RecurringConfig recurring) {
        if (recurring == null || recurring.getFrequency() == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder("Every ");
        switch (recurring.getFrequency().toUpperCase()) {
            case "DAILY":
                sb.append("day");
                break;
            case "WEEKLY":
                sb.append("week");
                if (recurring.getDaysOfWeek() != null && !recurring.getDaysOfWeek().isEmpty()) {
                    sb.append(" on specified days");
                }
                break;
            case "MONTHLY":
                sb.append("month");
                if (recurring.getDayOfMonth() != null) {
                    sb.append(" on day ").append(recurring.getDayOfMonth());
                }
                break;
        }

        if (recurring.getTimeOfDay() != null) {
            sb.append(" at ").append(recurring.getTimeOfDay());
        }

        return sb.toString();
    }
}
