package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.cms.CmsTopicFilterResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.*;
import com.aspire.asat.phishing.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for campaign management.
 */
public interface CampaignService {

    // --- CRUD Operations ---

    /**
     * Get campaigns with pagination and filters
     */
    List<CampaignDto> getCampaigns(int offset, int pageSize, String searchParam,
                                    CampaignStatus status, CampaignChannel channel,
                                    String sortBy, String sortOrder);

    long countCampaigns(String searchParam, CampaignStatus status, CampaignChannel channel);

    /**
     * Get campaign by ID
     */
    CampaignDto getCampaignById(String campaignId);

    /**
     * Create new campaign (Step 1)
     */
    CampaignDto createCampaign(CampaignCreateRequest request);

    /**
     * Delete draft campaign
     */
    void deleteCampaign(String campaignId);

    // --- Wizard Step Updates ---

    /**
     * Update Step 1: Campaign Setup
     */
    CampaignDto updateCampaignSetup(String campaignId, CampaignCreateRequest request);

    /**
     * Update Step 2: Email Template
     */
    CampaignDto updateEmailTemplate(String campaignId, CampaignEmailTemplateRequest request);

    /**
     * Update Step 3: Landing Page
     */
    CampaignDto updateLandingPage(String campaignId, CampaignLandingPageRequest request);

    /**
     * Update Step 4: Sender Profile
     */
    CampaignDto updateSenderProfile(String campaignId, CampaignSenderProfileRequest request);

    CampaignDto updateSmsServer(String campaignId, CampaignSmsServerRequest request);

    /**
     * Update Step 2 for voice campaigns: consent + voice clone from a new audio sample
     * or reuse of an existing completed clone ({@code voiceCloneId}).
     */
    CampaignDto updateVoiceSetup(String campaignId,
                                 CampaignVoiceSetupRequest request,
                                 MultipartFile audioSample,
                                 UUID voiceCloneId,
                                 VoiceCloneProvider provider,
                                 String language,
                                 String voiceName);

    CampaignDto updateScenario(String campaignId, CampaignScenarioRequest request);

    CampaignDto updateTelephony(String campaignId, CampaignTelephonyRequest request);

    /**
     * Update Step 5: Tags
     */
    CampaignDto updateTags(String campaignId, CampaignTagsRequest request);

    /**
     * Update Step 6: Audience
     */
    CampaignDto updateAudience(String campaignId, CampaignAudienceRequest request);

    /**
     * Update Step 7: Training
     */
    CampaignDto updateTraining(String campaignId, CampaignTrainingRequest request);

    /**
     * Update Step 8: Schedule
     */
    CampaignDto updateSchedule(String campaignId, CampaignScheduleRequest request);

    // --- Campaign Lifecycle ---

    /**
     * Launch campaign
     */
    CampaignDto launchCampaign(String campaignId);

    /**
     * Pause running campaign
     */
    CampaignDto pauseCampaign(String campaignId);

    /**
     * Resume paused campaign
     */
    CampaignDto resumeCampaign(String campaignId);

    /**
     * Cancel campaign
     */
    CampaignDto cancelCampaign(String campaignId);

    // --- Recipients ---

    /**
     * Get campaign recipients
     */
    List<CampaignRecipientDto> getRecipients(String campaignId, int offset, int pageSize,
                                              String searchParam, String sortBy, String sortOrder);

    /**
     * Count recipients
     */
    long countRecipients(String campaignId, String searchParam);

    /**
     * Preview email with personalization
     */
    String previewEmail(String campaignId, String recipientId);

    // --- Statistics ---

    /**
     * Get campaign statistics
     */
    CampaignStatsDto getCampaignStats(String campaignId);

    /**
     * Get top risk impact for campaigns with optional search and filters.
     *
     * @return page of risk-impact rows; {@code totalElements} matches the same filters
     */
    Page<CampaignRiskImpactDto> getCampaignRiskImpact(
            int offset,
            int pageSize,
            String searchParam,
            CampaignType type,
            CampaignStatus status,
            RiskLevel riskImpact,
            Instant startDate,
            Instant endDate,
            CampaignChannel channel);

    /**
     * Refresh campaign statistics (recalculate from recipients)
     */
    CampaignStatsDto refreshCampaignStats(String campaignId);

    /**
     * Aggregated campaign participation statistics for the authenticated end user (USER role),
     * scoped to the given simulation channel (null is treated as EMAIL).
     */
    UserCampaignStatisticsDto getUserCampaignStatistics(CampaignChannel channel);

    /**
     * Whether the authenticated end user has any campaign_recipients row (USER role),
     * plus per-channel enrollment flags.
     */
    EndUserPhishingVisibilityDto hasReceivedPhishingCampaign();

    /**
     * Whether this is the first campaign for the product package.
     * Client scope comes from auth context. When {@code campaignId} is set, that campaign
     * is excluded so a Step 1 draft does not make the check always false.
     */
    boolean isFirstCampaign(String productPackageId, String campaignId);

    /**
     * Recommend training topics based on campaign context, email template,
     * landing page, sender profile, tags, and client admin profile.
     */
    CmsTopicFilterResponseDto getRecommendedTopics(String campaignId, String productId,
                                                    String packageId, Integer page, Integer size);
}
