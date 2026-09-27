package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.*;
import com.aspire.asat.phishing.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for campaign management endpoints.
 */
@Tag(name = "Campaigns", description = "APIs for managing phishing campaigns")
@RequestMapping(value = WebApiUrlConstants.CAMPAIGNS_PATH)
public interface CampaignController {

    // --- CRUD Operations ---

    @Operation(summary = "Get all campaigns", 
               description = "Retrieves paginated list of campaigns with optional search and filter")
    @GetMapping
    ResponseEntity<AllResponseDto<List<CampaignDto>>> getCampaigns(
            @Parameter(description = "Pagination offset (page index)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String searchParam,
            @Parameter(description = "Filter by status") @RequestParam(required = false) CampaignStatus status,
            @Parameter(description = "Filter by channel (EMAIL, SMS, VOICE)") @RequestParam(required = false) CampaignChannel channel,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "Alias for sortOrder (frontend compatibility)") @RequestParam(required = false) String sortDirection
    );

    @Operation(summary = "Check if this is the first campaign for a product package",
            description = "Returns true when the authenticated client has no other campaigns "
                    + "(any status) with the given productPackageId. Pass campaignId after Step 1 "
                    + "so the current draft is excluded. Client scope comes from auth context.")
    @GetMapping("/is-first")
    ResponseEntity<ApiResponseDto<Boolean>> isFirstCampaign(
            @Parameter(description = "ClientProduct.id (product package assignment)", required = true)
            @RequestParam String productPackageId,
            @Parameter(description = "Current campaign id to exclude (use after Step 1 create)")
            @RequestParam(required = false) String campaignId
    );

    @Operation(summary = "Get campaign by ID", description = "Retrieves a specific campaign by its ID")
    @GetMapping("/{campaignId}")
    ResponseEntity<ApiResponseDto<CampaignDto>> getCampaignById(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId
    );

    @Operation(summary = "Create campaign", description = "Creates a new campaign (Step 1 of wizard)")
    @PostMapping
    ResponseEntity<ApiResponseDto<CampaignDto>> createCampaign(
            @Valid @RequestBody CampaignCreateRequest request
    );

    @Operation(summary = "Delete campaign", description = "Deletes a draft campaign")
    @DeleteMapping("/{campaignId}")
    ResponseEntity<ApiResponseDto<String>> deleteCampaign(
            @Parameter(description = "Campaign ID") @PathVariable String campaignId
    );

    // --- Wizard Step Updates ---

    @Operation(summary = "Update campaign setup (Step 1)")
    @PutMapping("/{campaignId}/step/1")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateCampaignSetup(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignCreateRequest request
    );

    @Operation(summary = "Update email template (Step 2)")
    @PutMapping("/{campaignId}/step/2")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateEmailTemplate(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignEmailTemplateRequest request
    );

    @Operation(summary = "Update landing page (Step 3)")
    @PutMapping("/{campaignId}/step/3")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateLandingPage(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignLandingPageRequest request
    );

    @Operation(summary = "Update sender profile (Step 4)")
    @PutMapping("/{campaignId}/step/4")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateSenderProfile(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignSenderProfileRequest request
    );

    @Operation(summary = "Update SMS server configuration (Step 4 for SMS campaigns)")
    @PutMapping("/{campaignId}/step/4/sms-server")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateSmsServer(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignSmsServerRequest request
    );

    @Operation(summary = "Update voice setup (Step 2 for voice campaigns)",
               description = "Either upload a new audio sample (file) to create a one-shot voice clone, "
                       + "or reuse an existing completed clone via voiceCloneId from GET /vishing-voices. "
                       + "Provide exactly one of file or voiceCloneId. "
                       + "voiceName is optional when uploading a new sample. "
                       + "Pass provider and language as query parameters when uploading "
                       + "(defaults: provider=ELEVENLABS). Consent must be confirmed.")
    @PutMapping(value = "/{campaignId}/step/2/voice-setup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponseDto<CampaignDto>> updateVoiceSetup(
            @PathVariable String campaignId,
            @Valid @ModelAttribute CampaignVoiceSetupRequest request,
            @RequestParam(value = "file", required = false) MultipartFile audioSample,
            @RequestParam(required = false) UUID voiceCloneId,
            @RequestParam(required = false, defaultValue = "ELEVENLABS") VoiceCloneProvider provider,
            @RequestParam(required = false) String language,
            @Parameter(description = "Optional display name for the new clone. "
                    + "If the field is sent more than once, only the first value is used.",
                    schema = @Schema(type = "string"))
            @RequestParam(required = false) String[] voiceName
    );

    @Operation(summary = "Update vishing scenario (Step 3 for voice campaigns)")
    @PutMapping("/{campaignId}/step/3/scenario")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateScenario(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignScenarioRequest request
    );

    @Operation(summary = "Update telephony configuration (Step 4 for voice campaigns)")
    @PutMapping("/{campaignId}/step/4/telephony")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateTelephony(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignTelephonyRequest request
    );

    @Operation(summary = "Update tags (Step 5)")
    @PutMapping("/{campaignId}/step/5")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateTags(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignTagsRequest request
    );

    @Operation(summary = "Update audience (Step 6)",
            description = "Selects campaign recipients from active phishing_user_licence rows for this "
                    + "campaign's productPackageId (ALL_USERS / DEPARTMENTS / GROUPS / INDIVIDUAL). "
                    + "Does not load users from Registration. Response is the updated CampaignDto.")
    @PutMapping("/{campaignId}/step/6")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateAudience(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignAudienceRequest request
    );

    @Operation(summary = "Allocate phishing licenses for selected audience",
            description = "Resolves audience users and allocates unique licenses under the campaign "
                    + "productPackageId (ClientProduct.id). Existing licensed users consume 0 seats. "
                    + "When new users exceed remaining seats and confirm=false, returns requiresConfirmation "
                    + "with a message and proposed userIds without inserting. When confirm=true, inserts "
                    + "only remaining new users. Does not create campaign_recipients (use Step 6).")
    @PostMapping("/{campaignId}/allocate-licence")
    ResponseEntity<ApiResponseDto<AllocateLicenceResponseDto>> allocateLicence(
            @PathVariable String campaignId,
            @Valid @RequestBody AllocateLicenceRequest request
    );

    @Operation(summary = "List licensed users for campaign",
            description = "Paginated users already allocated in phishing_user_licence for this campaign's "
                    + "clientAdminId + productPackageId. offset is page index (skip = offset * pageSize). "
                    + "Response fields align with Registration end-user for FE table reuse.")
    @GetMapping("/{campaignId}/licensed-users")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<LicensedUserDto>>>> getLicensedUsers(
            @PathVariable String campaignId,
            @Parameter(description = "Search firstName, lastName, email") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by departmentName (repeatable)") @RequestParam(required = false) List<String> departments,
            @Parameter(description = "Risk group(s); repeat query param for multiple (e.g. riskGroup=HIGH_RISK&riskGroup=LOW_RISK)")
            @RequestParam(name = "riskGroup", required = false) List<RiskGroup> riskGroups,
            @Parameter(description = "Pagination offset (page index)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(summary = "Licensed user counts by department",
            description = "Aggregates phishing_user_licence rows for the campaign product package by departmentName.")
    @GetMapping("/{campaignId}/licensed-users/department-counts")
    ResponseEntity<ApiResponseDto<List<LicensedUserDepartmentCountDto>>> getLicensedUserDepartmentCounts(
            @PathVariable String campaignId
    );

    @Operation(summary = "Licensed user counts by risk group",
            description = "Aggregates phishing_user_licence rows for the campaign product package by "
                    + "snapshotted riskGroup. Response shape matches Registration "
                    + "GET /departments/risk-group-user-counts (riskGroup and userCount).")
    @GetMapping("/{campaignId}/licensed-users/group-counts")
    ResponseEntity<ApiResponseDto<List<LicensedUserGroupCountDto>>> getLicensedUserGroupCounts(
            @PathVariable String campaignId
    );

    @Operation(summary = "Update training (Step 7)")
    @PutMapping("/{campaignId}/step/7")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateTraining(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignTrainingRequest request
    );

    @Operation(summary = "Update schedule (Step 8)")
    @PutMapping("/{campaignId}/step/8")
    ResponseEntity<ApiResponseDto<CampaignDto>> updateSchedule(
            @PathVariable String campaignId,
            @Valid @RequestBody CampaignScheduleRequest request
    );

    // --- Campaign Lifecycle ---

    @Operation(summary = "Launch campaign", description = "Launches a completed campaign")
    @PostMapping("/{campaignId}/launch")
    ResponseEntity<ApiResponseDto<CampaignDto>> launchCampaign(
            @PathVariable String campaignId
    );

    @Operation(summary = "Pause campaign", description = "Pauses a running campaign")
    @PostMapping("/{campaignId}/pause")
    ResponseEntity<ApiResponseDto<CampaignDto>> pauseCampaign(
            @PathVariable String campaignId
    );

    @Operation(summary = "Resume campaign", description = "Resumes a paused campaign")
    @PostMapping("/{campaignId}/resume")
    ResponseEntity<ApiResponseDto<CampaignDto>> resumeCampaign(
            @PathVariable String campaignId
    );

    @Operation(summary = "Cancel campaign", description = "Cancels a campaign")
    @PostMapping("/{campaignId}/cancel")
    ResponseEntity<ApiResponseDto<CampaignDto>> cancelCampaign(
            @PathVariable String campaignId
    );

    // --- Recipients ---

    @Operation(summary = "Get campaign recipients",
            description = "Retrieves paginated recipients for a campaign. offset is page index "
                    + "(0 = first page, 1 = second page), same as campaign list.")
    @GetMapping("/{campaignId}/recipients")
    ResponseEntity<AllResponseDto<List<CampaignRecipientDto>>> getRecipients(
            @PathVariable String campaignId,
            @Parameter(description = "Pagination offset (page index)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String searchParam,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "email") String sortBy,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(defaultValue = "asc") String sortOrder,
            @Parameter(description = "Alias for sortOrder (frontend compatibility)")
            @RequestParam(required = false) String sortDirection
    );

    // --- Statistics ---

    @Operation(summary = "Get end-user campaign statistics",
            description = "Returns participation metrics for the authenticated USER, scoped to a simulation channel. "
                    + "totalCampaigns, open, click, compromise, and report counts come from campaign_recipients "
                    + "enrolled in campaigns of that channel.")
    @GetMapping("/me/statistics")
    ResponseEntity<ApiResponseDto<UserCampaignStatisticsDto>> getUserCampaignStatistics(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Check end-user phishing campaign enrollment",
            description = "Returns whether the authenticated USER has any campaign_recipients row, "
                    + "plus per-channel flags (EMAIL, SMS, VOICE). "
                    + "Use to show or hide phishing statistics sections on the end-user dashboard.")
    @GetMapping("/me/has-received-campaign")
    ResponseEntity<ApiResponseDto<EndUserPhishingVisibilityDto>> hasReceivedPhishingCampaign();

    @Operation(summary = "Get campaign statistics")
    @GetMapping("/{campaignId}/stats")
    ResponseEntity<ApiResponseDto<CampaignStatsDto>> getCampaignStats(
            @PathVariable String campaignId
    );

    @Operation(summary = "Get campaign risk impact",
            description = "Returns top risk impact with AI rating for each campaign. "
                    + "Draft campaigns are excluded by default. Pass status to filter, including DRAFT. "
                    + "searchParam matches campaign name or type; optional filters: type, status, riskImpact, startDate, endDate.")
    @GetMapping("/risk-impact")
    ResponseEntity<AllResponseDto<List<CampaignRiskImpactDto>>> getCampaignRiskImpact(
            @Parameter(description = "Pagination offset (page index)") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Search campaign name or type") @RequestParam(required = false) String searchParam,
            @Parameter(description = "Filter by campaign type") @RequestParam(required = false) CampaignType type,
            @Parameter(description = "Filter by status. When omitted, DRAFT campaigns are excluded.") @RequestParam(required = false) CampaignStatus status,
            @Parameter(description = "Filter by computed risk impact") @RequestParam(required = false) RiskLevel riskImpact,
            @Parameter(description = "Filter campaigns created on/after this instant") @RequestParam(required = false) Instant startDate,
            @Parameter(description = "Filter campaigns created on/before this instant") @RequestParam(required = false) Instant endDate,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Refresh campaign statistics", description = "Recalculates stats from recipients")
    @PostMapping("/{campaignId}/stats/refresh")
    ResponseEntity<ApiResponseDto<CampaignStatsDto>> refreshCampaignStats(
            @PathVariable String campaignId
    );

    // --- Email Preview ---

    @Operation(summary = "Preview email", description = "Preview personalized email for a recipient")
    @GetMapping("/{campaignId}/preview-email")
    ResponseEntity<ApiResponseDto<String>> previewEmail(
            @PathVariable String campaignId,
            @RequestParam(required = false) String recipientId
    );

    // --- Topic Recommendation ---

    @Operation(summary = "Get recommended training topics",
               description = "Recommends topics based on campaign context, email template, landing page, sender profile, tags, and client admin profile. "
                       + "productId and packageId are optional; when omitted, values from campaign training data are used, otherwise topics are filtered without product/package scope.")
    @GetMapping("/{campaignId}/recommended-topics")
    ResponseEntity<ApiResponseDto<CmsTopicFilterResponseDto>> getRecommendedTopics(
            @PathVariable String campaignId,
            @Parameter(description = "Product ID to scope topic filtering") @RequestParam(required = false) String productId,
            @Parameter(description = "Package ID to scope topic filtering") @RequestParam(required = false) String packageId,
            @Parameter(description = "Page number (1-based, same as CMS filter)") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") Integer size
    );
}
