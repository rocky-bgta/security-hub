package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.phishing.controller.CampaignController;
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
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.CampaignService;
import com.aspire.asat.phishing.service.PhishingUserLicenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Controller implementation for campaign management.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CampaignControllerImpl implements CampaignController {

    private final CampaignService campaignService;
    private final PhishingUserLicenceService phishingUserLicenceService;
    private final MessageService messageService;
    private final ToastMessageResolver toastMessageResolver;

    // --- CRUD Operations ---

    @Override
    public ResponseEntity<AllResponseDto<List<CampaignDto>>> getCampaigns(
            int offset, int pageSize, String searchParam, CampaignStatus status, CampaignChannel channel,
            String sortBy, String sortOrder, String sortDirection) {
        try {
            String effectiveSortOrder = (sortDirection != null && !sortDirection.isBlank())
                    ? sortDirection.trim()
                    : sortOrder;
            List<CampaignDto> campaigns = campaignService.getCampaigns(
                    offset, pageSize, searchParam, status, channel, sortBy, effectiveSortOrder);
            long totalCount = campaignService.countCampaigns(searchParam, status, channel);
            
            return ResponseEntity.ok(AllResponseDto.<List<CampaignDto>>builder()
                    .items(campaigns)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting campaigns", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<CampaignDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> isFirstCampaign(String productPackageId, String campaignId) {
        try {
            boolean isFirst = campaignService.isFirstCampaign(productPackageId, campaignId);
            return buildApiResponse(HttpStatus.OK, "First campaign check completed successfully", isFirst);
        } catch (RuntimeException e) {
            log.error("Error checking first campaign for productPackageId={}, campaignId={}: {}",
                    productPackageId, campaignId, e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> getCampaignById(String campaignId) {
        try {
            CampaignDto campaign = campaignService.getCampaignById(campaignId);
            return buildApiResponse(HttpStatus.OK, "Campaign retrieved successfully", campaign);
        } catch (RuntimeException e) {
            log.error("Error getting campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> createCampaign(CampaignCreateRequest request) {
        try {
            CampaignDto campaign = campaignService.createCampaign(request);
            return buildApiResponse(HttpStatus.CREATED, messageService.get(MessageKeys.CAMPAIGN_SAVED_DRAFT), campaign);
        } catch (RuntimeException e) {
            log.error("Error creating campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteCampaign(String campaignId) {
        try {
            campaignService.deleteCampaign(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.CAMPAIGN_DELETED), campaignId);
        } catch (RuntimeException e) {
            log.error("Error deleting campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    // --- Wizard Step Updates ---

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateCampaignSetup(
            String campaignId, CampaignCreateRequest request) {
        return handleStepUpdate(() -> campaignService.updateCampaignSetup(campaignId, request),
                "Campaign setup updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateEmailTemplate(
            String campaignId, CampaignEmailTemplateRequest request) {
        return handleStepUpdate(() -> campaignService.updateEmailTemplate(campaignId, request),
                "Campaign email template updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateLandingPage(
            String campaignId, CampaignLandingPageRequest request) {
        return handleStepUpdate(() -> campaignService.updateLandingPage(campaignId, request),
                "Campaign landing page updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateSenderProfile(
            String campaignId, CampaignSenderProfileRequest request) {
        return handleStepUpdate(() -> campaignService.updateSenderProfile(campaignId, request),
                "Campaign sender profile updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateSmsServer(
            String campaignId, CampaignSmsServerRequest request) {
        return handleStepUpdate(() -> campaignService.updateSmsServer(campaignId, request),
                "SMS server configuration updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateVoiceSetup(
            String campaignId,
            CampaignVoiceSetupRequest request,
            MultipartFile audioSample,
            UUID voiceCloneId,
            VoiceCloneProvider provider,
            String language,
            String[] voiceName) {
        return handleStepUpdate(
                () -> campaignService.updateVoiceSetup(
                        campaignId, request, audioSample, voiceCloneId, provider, language,
                        firstRequestParam(voiceName)),
                "Voice setup updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateScenario(
            String campaignId, CampaignScenarioRequest request) {
        return handleStepUpdate(() -> campaignService.updateScenario(campaignId, request),
                "Vishing scenario updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateTelephony(
            String campaignId, CampaignTelephonyRequest request) {
        return handleStepUpdate(() -> campaignService.updateTelephony(campaignId, request),
                "Telephony configuration updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateTags(
            String campaignId, CampaignTagsRequest request) {
        return handleStepUpdate(() -> campaignService.updateTags(campaignId, request),
                "Campaign tags updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateAudience(
            String campaignId, CampaignAudienceRequest request) {
        return handleStepUpdate(() -> campaignService.updateAudience(campaignId, request),
                "Campaign audience updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllocateLicenceResponseDto>> allocateLicence(
            String campaignId, AllocateLicenceRequest request) {
        try {
            AllocateLicenceResponseDto result =
                    phishingUserLicenceService.allocateLicence(campaignId, request);
            String message = result.isRequiresConfirmation()
                    ? result.getConfirmationMessage()
                    : "License allocation completed successfully";
            return buildApiResponse(HttpStatus.OK, message, result);
        } catch (RuntimeException e) {
            log.error("Error allocating licenses for campaign {}: {}", campaignId, e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<LicensedUserDto>>>> getLicensedUsers(
            String campaignId, String search, List<String> departments, List<RiskGroup> riskGroups,
            int offset, int pageSize) {
        try {
            AllResponseDto<List<LicensedUserDto>> page = phishingUserLicenceService.listLicensedUsers(
                    campaignId, search, departments, riskGroups, offset, pageSize);
            return buildApiResponse(HttpStatus.OK, "Licensed users retrieved successfully", page);
        } catch (RuntimeException e) {
            log.error("Error listing licensed users for campaign {}: {}", campaignId, e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<LicensedUserDepartmentCountDto>>> getLicensedUserDepartmentCounts(
            String campaignId) {
        try {
            List<LicensedUserDepartmentCountDto> counts =
                    phishingUserLicenceService.getLicensedUserDepartmentCounts(campaignId);
            return buildApiResponse(HttpStatus.OK, "Licensed user department counts retrieved successfully", counts);
        } catch (RuntimeException e) {
            log.error("Error getting licensed user department counts for campaign {}: {}",
                    campaignId, e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<LicensedUserGroupCountDto>>> getLicensedUserGroupCounts(
            String campaignId) {
        try {
            List<LicensedUserGroupCountDto> counts =
                    phishingUserLicenceService.getLicensedUserGroupCounts(campaignId);
            return buildApiResponse(HttpStatus.OK, "Risk group user counts retrieved successfully", counts);
        } catch (RuntimeException e) {
            log.error("Error getting licensed user risk group counts for campaign {}: {}",
                    campaignId, e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateTraining(
            String campaignId, CampaignTrainingRequest request) {
        return handleStepUpdate(() -> campaignService.updateTraining(campaignId, request),
                "Campaign training updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> updateSchedule(
            String campaignId, CampaignScheduleRequest request) {
        return handleStepUpdate(() -> campaignService.updateSchedule(campaignId, request),
                "Campaign schedule updated successfully");
    }

    // --- Campaign Lifecycle ---

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> launchCampaign(String campaignId) {
        try {
            CampaignDto campaign = campaignService.launchCampaign(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.CAMPAIGN_LAUNCHED), campaign);
        } catch (RuntimeException e) {
            log.error("Error launching campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> pauseCampaign(String campaignId) {
        try {
            CampaignDto campaign = campaignService.pauseCampaign(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.CAMPAIGN_PAUSED), campaign);
        } catch (RuntimeException e) {
            log.error("Error pausing campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> resumeCampaign(String campaignId) {
        try {
            CampaignDto campaign = campaignService.resumeCampaign(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.CAMPAIGN_RESUMED), campaign);
        } catch (RuntimeException e) {
            log.error("Error resuming campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignDto>> cancelCampaign(String campaignId) {
        try {
            CampaignDto campaign = campaignService.cancelCampaign(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.CAMPAIGN_CANCELLED), campaign);
        } catch (RuntimeException e) {
            log.error("Error cancelling campaign: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    // --- Recipients ---

    @Override
    public ResponseEntity<AllResponseDto<List<CampaignRecipientDto>>> getRecipients(
            String campaignId, int offset, int pageSize, String searchParam,
            String sortBy, String sortOrder, String sortDirection) {
        try {
            String effectiveSortOrder = (sortDirection != null && !sortDirection.isBlank())
                    ? sortDirection.trim()
                    : sortOrder;
            List<CampaignRecipientDto> recipients = campaignService.getRecipients(
                    campaignId, offset, pageSize, searchParam, sortBy, effectiveSortOrder);
            long totalCount = campaignService.countRecipients(campaignId, searchParam);

            return ResponseEntity.ok(AllResponseDto.<List<CampaignRecipientDto>>builder()
                    .items(recipients)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (RuntimeException e) {
            log.error("Error getting recipients: {}", e.getMessage());
            return ResponseEntity.status(resolveHttpStatus(e))
                    .body(AllResponseDto.<List<CampaignRecipientDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .offset(offset)
                            .pageSize(pageSize)
                            .build());
        }
    }

    // --- Statistics ---

    @Override
    public ResponseEntity<ApiResponseDto<UserCampaignStatisticsDto>> getUserCampaignStatistics(CampaignChannel channel) {
        try {
            UserCampaignStatisticsDto stats = campaignService.getUserCampaignStatistics(channel);
            return buildApiResponse(HttpStatus.OK, "User campaign statistics retrieved successfully", stats);
        } catch (RuntimeException e) {
            log.error("Error getting user campaign statistics: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EndUserPhishingVisibilityDto>> hasReceivedPhishingCampaign() {
        try {
            EndUserPhishingVisibilityDto visibility = campaignService.hasReceivedPhishingCampaign();
            return buildApiResponse(HttpStatus.OK, "Phishing campaign enrollment status retrieved successfully", visibility);
        } catch (RuntimeException e) {
            log.error("Error checking phishing campaign enrollment: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignStatsDto>> getCampaignStats(String campaignId) {
        try {
            CampaignStatsDto stats = campaignService.getCampaignStats(campaignId);
            return buildApiResponse(HttpStatus.OK, "Statistics retrieved successfully", stats);
        } catch (RuntimeException e) {
            log.error("Error getting campaign stats: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    @Override
    public ResponseEntity<AllResponseDto<List<CampaignRiskImpactDto>>> getCampaignRiskImpact(
            int offset,
            int pageSize,
            String searchParam,
            CampaignType type,
            CampaignStatus status,
            RiskLevel riskImpact,
            Instant startDate,
            Instant endDate,
            CampaignChannel channel) {
        try {
            Page<CampaignRiskImpactDto> page = campaignService.getCampaignRiskImpact(
                    offset, pageSize, searchParam, type, status, riskImpact, startDate, endDate, channel);
            return ResponseEntity.ok(AllResponseDto.<List<CampaignRiskImpactDto>>builder()
                    .items(page.getContent())
                    .total(page.getTotalElements())
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (RuntimeException e) {
            log.error("Error getting campaign risk impact: {}", e.getMessage());
            return ResponseEntity.status(resolveHttpStatus(e))
                    .body(AllResponseDto.<List<CampaignRiskImpactDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .offset(offset)
                            .pageSize(pageSize)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignStatsDto>> refreshCampaignStats(String campaignId) {
        try {
            CampaignStatsDto stats = campaignService.refreshCampaignStats(campaignId);
            return buildApiResponse(HttpStatus.OK, messageService.get(MessageKeys.ANALYTICS_STATISTICS_REFRESHED), stats);
        } catch (RuntimeException e) {
            log.error("Error refreshing campaign stats: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    // --- Email Preview ---

    @Override
    public ResponseEntity<ApiResponseDto<String>> previewEmail(String campaignId, String recipientId) {
        try {
            String preview = campaignService.previewEmail(campaignId, recipientId);
            return buildApiResponse(HttpStatus.OK, "Campaign email preview generated successfully", preview);
        } catch (RuntimeException e) {
            log.error("Error previewing email: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    // --- Topic Recommendation ---

    @Override
    public ResponseEntity<ApiResponseDto<CmsTopicFilterResponseDto>> getRecommendedTopics(
            String campaignId, String productId, String packageId, Integer page, Integer size) {
        try {
            log.info("Getting recommended topics for campaign: {}, page: {}, size: {}",
                    campaignId, page, size);

            CmsTopicFilterResponseDto response = campaignService.getRecommendedTopics(
                    campaignId, productId, packageId, page, size);

            return ResponseEntity.ok(new ApiResponseDto<>("Recommended topics retrieved successfully", 200, response));
        } catch (RuntimeException e) {
            log.error("Error getting recommended topics: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return ResponseEntity.status(status)
                    .body(new ApiResponseDto<>(resolveErrorMessage(e), status.value(), null));
        }
    }

    // --- Helper Methods ---

    private ResponseEntity<ApiResponseDto<CampaignDto>> handleStepUpdate(
            java.util.function.Supplier<CampaignDto> updateAction,
            String successMessage) {
        try {
            CampaignDto campaign = updateAction.get();
            return buildApiResponse(HttpStatus.OK, successMessage, campaign);
        } catch (RuntimeException e) {
            log.error("Error updating campaign step: {}", e.getMessage());
            HttpStatus status = resolveHttpStatus(e);
            return buildApiResponse(status, resolveErrorMessage(e), null);
        }
    }

    private <T> ResponseEntity<ApiResponseDto<T>> buildApiResponse(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(ApiResponseDto.<T>builder()
                .statusCode(status.value())
                .message(message)
                .data(data)
                .build());
    }

    private HttpStatus resolveHttpStatus(RuntimeException e) {
        if (e instanceof ServiceException serviceException && serviceException.getStatus() != null) {
            return serviceException.getStatus();
        }
        if (e instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        String message = e != null && e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (message.contains("already exists")
                || message.contains("cannot modify")
                || message.contains("cannot be launched")
                || message.contains("cannot be cancelled")
                || message.contains("only running")
                || message.contains("only paused")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String resolveErrorMessage(RuntimeException e) {
        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            return "Campaign request could not be processed";
        }
        return toastMessageResolver.resolve(e.getMessage());
    }

    /**
     * Spring joins repeated multipart form fields into a String[]; take the first non-blank value.
     */
    private static String firstRequestParam(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
