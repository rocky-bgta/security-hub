package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.CampaignTelephonyData;
import com.aspire.asat.phishing.dto.CampaignVoiceData;
import com.aspire.asat.phishing.dto.CampaignVoiceScenarioRef;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.LearningMode;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.request.CampaignExpireDateRequest;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.enums.VoiceResponseStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB entity for phishing campaigns.
 * Represents the complete campaign configuration from the 9-step wizard.
 */
@Document(collection = "campaigns")
@CompoundIndexes({
        @CompoundIndex(name = "status_expires_at_idx", def = "{'status': 1, 'expiresAt': 1}"),
        @CompoundIndex(name = "client_status_product_package_idx",
                def = "{'clientId': 1, 'status': 1, 'productPackageId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    private String id;

    @NotBlank
    private String clientId;

    // Step 1: Campaign Setup
    @NotBlank
    @Size(max = 50)
    private String campaignName;

    @NotNull
    private CampaignType campaignType;

    /**
     * ClientProduct assignment id ({@code ClientProduct.id}) for unique-user license accounting.
     * Copied from create/setup request or from {@link #trainingData} when training is saved.
     */
    private String productPackageId;

    private SubPackageAssignedFor assignedFor;

    /** Vishing-only: selected response stages from setup (CLICK/COMPROMISED). */
    @Builder.Default
    private List<VoiceResponseStage> responseStages = new ArrayList<>();

    private LearningMode learningMode;

    private CampaignExpireDateRequest expireDate;

    private Instant expiresAt;

    @NotNull
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @NotNull
    @Builder.Default
    private CampaignChannel channel = CampaignChannel.EMAIL;

    // Step 2: Template (email or SMS) / Voice setup
    private String emailTemplateId;
    private CampaignVoiceData voiceData;

    // Step 3: Landing Page / Scenario & Script
    private String landingPageId;
    private LandingPageType landingPageType;
    private CampaignVoiceScenarioRef voiceScenario;
    /** Optional override of landing page tracking domain for this campaign. */
    private String trackingDomainId;

    // Step 4: Mail Server (Sender Profile) or SMS/Voice Server
    private String senderProfileId;
    private String smsServerConfigurationId;
    private String voiceServerConfigurationId;
    private CampaignTelephonyData telephonyData;

    // Step 5: Tags
    @Builder.Default
    private List<String> campaignTags = new ArrayList<>();

    /** Per-campaign success keywords for compromise detection (vishing). */
    @Builder.Default
    private List<String> successKeywords = new ArrayList<>();

    // Step 6: Audience
    private CampaignAudience audience;

    // Step 7: Training (optional, based on campaignType)
    private CampaignTrainingData trainingData;

    // Step 8: Schedule
    private CampaignSchedule schedule;

    // Statistics
    @Builder.Default
    private CampaignStats stats = new CampaignStats();

    // Wizard progress tracking (1-9)
    @Builder.Default
    private int currentStep = 1;

    // Timestamps
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    // Execution tracking
    private Instant launchedAt;
    private Instant completedAt;
    private Instant pausedAt;
    private Instant cancelledAt;

    /** When all assigned training was observed complete (PHISHING_WITH_TRAINING campaigns). */
    private Instant trainingCompletedAt;

    /**
     * Check if campaign is editable (only DRAFT status)
     */
    public boolean isEditable() {
        return status == CampaignStatus.DRAFT;
    }

    /**
     * Check if campaign can be launched
     */
    public boolean canLaunch() {
        if (status != CampaignStatus.DRAFT || isExpired()) {
            return false;
        }
        if (isVoiceChannel()) {
            return currentStep >= 9
                    && voiceData != null
                    && voiceData.getVoiceCloneId() != null
                    && voiceScenario != null
                    && voiceScenario.getScenarioId() != null
                    && telephonyData != null
                    && telephonyData.getVoiceServerConfigurationId() != null
                    && audience != null
                    && schedule != null
                    && (!requiresTraining() || trainingData != null);
        }
        return currentStep >= 9;
    }

    /**
     * Check if campaign can be paused
     */
    public boolean canPause() {
        return status == CampaignStatus.RUNNING && !isExpired();
    }

    /**
     * Check if campaign can be resumed
     */
    public boolean canResume() {
        return status == CampaignStatus.PAUSED && !isExpired();
    }

    /**
     * Check if campaign can be cancelled
     */
    public boolean canCancel() {
        return !isExpired() && (status == CampaignStatus.RUNNING || status == CampaignStatus.PAUSED || status == CampaignStatus.SCHEDULED);
    }

    /**
     * Check if campaign is expired based on current time.
     */
    public boolean isExpired() {
        return isExpiredAt(Instant.now());
    }

    /**
     * Check if campaign is expired at the provided instant.
     */
    public boolean isExpiredAt(Instant now) {
        return expiresAt != null && now != null && !expiresAt.isAfter(now);
    }

    /**
     * Check if training step is required based on campaign type
     */
    public boolean requiresTraining() {
        return campaignType == CampaignType.PHISHING_WITH_TRAINING
                || campaignType == CampaignType.SMISHING_WITH_TRAINING
                || campaignType == CampaignType.VISHING_WITH_TRAINING;
    }

    /**
     * Whether this campaign uses the voice/vishing delivery channel.
     */
    public boolean isVoiceChannel() {
        return channel == CampaignChannel.VOICE;
    }
}
