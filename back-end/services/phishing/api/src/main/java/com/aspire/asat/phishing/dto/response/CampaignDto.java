package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.CampaignTelephonyData;
import com.aspire.asat.phishing.dto.CampaignVoiceData;
import com.aspire.asat.phishing.dto.CampaignVoiceScenarioRef;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.enums.LearningMode;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.enums.VoiceResponseStage;
import com.aspire.asat.phishing.dto.request.CampaignExpireDateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for campaign data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {

    private String campaignId;
    private String campaignName;
    private CampaignType campaignType;
    private CampaignChannel channel;
    /** ClientProduct assignment id used for unique-user license accounting. */
    private String productPackageId;
    private SubPackageAssignedFor assignedFor;
    private List<VoiceResponseStage> responseStages;
    private LearningMode learningMode;
    private CampaignStatus status;

    // Step 2: Email Template / Voice setup
    private String emailTemplateId;
    private String emailTemplateName;
    private CampaignVoiceData voiceData;

    // Step 3: Landing Page / Scenario
    private String landingPageId;
    private String landingPageName;
    private LandingPageType landingPageType;
    private CampaignVoiceScenarioRef voiceScenario;
    private String trackingDomainId;

    // Step 4: Sender Profile / SMS / Voice Server
    private String senderProfileId;
    private String senderProfileName;
    private String smsServerConfigurationId;
    private String smsServerConfigurationName;
    private String voiceServerConfigurationId;
    private String voiceServerConfigurationName;
    private CampaignTelephonyData telephonyData;

    // Step 5: Tags
    private List<String> campaignTags;

    // Step 6: Audience
    private CampaignAudienceDto audience;

    // Step 7: Training
    private CampaignTrainingData trainingData;

    // Step 8: Schedule
    private CampaignScheduleDto schedule;

    // Statistics
    private CampaignStatsDto stats;

    // Wizard progress
    private int currentStep;
    private int totalSteps;
    private boolean isComplete;

    // Permissions
    private boolean canEdit;
    private boolean canLaunch;
    private boolean canPause;
    private boolean canResume;
    private boolean canCancel;
    private boolean canDelete;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant launchedAt;
    private Instant completedAt;
    private CampaignExpireDateRequest expireDate;
    private Instant expiresAt;
    private String createdBy;
}
