package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.OrganizationDashboardController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.dashboard.ProductTopicSalesProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TimeFrame;
import com.aspire.asat.cms.dto.organizationDashboard.OrganizationDashboardResponseDto;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.model.OrganizationDashboard;
import com.aspire.asat.cms.service.OrganizationDashboardService;
import com.aspire.asat.cms.service.dashboard.ProductTopicSalesProgressService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrganizationDashboardControllerImpl implements OrganizationDashboardController {

    private final OrganizationDashboardService organizationDashboardService;
    private final ProductTopicSalesProgressService productTopicSalesProgressService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationDashboardResponseDto>> getOrganizationDashboard() {
        try {
            log.info("Retrieving consolidated organization dashboard (summed across all dashboards)");
            OrganizationDashboard dashboard = organizationDashboardService.getConsolidatedOrganizationDashboard();
            OrganizationDashboardResponseDto responseDto = convertToResponseDto(dashboard);
            return ResponseEntity.ok(new ApiResponseDto<>("Organization dashboard retrieved successfully", 200, responseDto));
        } catch (Exception e) {
            log.error("Error retrieving consolidated organization dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve organization dashboard: " + e.getMessage(), 500, null));
        }
    }

    /**
     * Convert OrganizationDashboard model to OrganizationDashboardResponseDto
     * @param dashboard the OrganizationDashboard model
     * @return OrganizationDashboardResponseDto
     */
    private OrganizationDashboardResponseDto convertToResponseDto(OrganizationDashboard dashboard) {
        return OrganizationDashboardResponseDto.builder()
                .id(dashboard.getId())
                .organizationAdminId(dashboard.getOrganizationAdminId())
                .totalProduct(dashboard.getTotalProduct())
                .totalPackage(dashboard.getTotalPackage())
                .totalLicense(dashboard.getTotalLicense())
                .totalClient(dashboard.getTotalClient())
                .totalMsp(dashboard.getTotalMsp())
                .createdAt(dashboard.getCreatedAt())
                .updatedAt(dashboard.getUpdatedAt())
                .createdBy(dashboard.getCreatedBy())
                .updatedBy(dashboard.getUpdatedBy())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProductTopicSalesProgressResponseDto>> getProductTopicSalesProgress(TimeFrame timeFrame) {
        log.info("Request received to fetch product-topic sales progress with timeFrame: {}", timeFrame);
        try {
            // Handle null timeFrame (default to YEARLY)
            if (timeFrame == null) {
                timeFrame = TimeFrame.YEARLY;
            }
            
            ProductTopicSalesProgressResponseDto response = productTopicSalesProgressService.getProductTopicSalesProgress(timeFrame);
            ApiResponseDto<ProductTopicSalesProgressResponseDto> apiResponse = 
                    new ApiResponseDto<>("Sales progress data retrieved successfully", 200, response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching product-topic sales progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve sales progress data: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProductTopicSalesProgressResponseDto>> getMspProductTopicSalesProgress(
            TimeFrame timeFrame, String mspId) {
        try {
            String resolvedMspId = resolveMspId(mspId);
            if (timeFrame == null) {
                timeFrame = TimeFrame.YEARLY;
            }

            log.info("Request received to fetch MSP product-topic sales progress for mspId={} timeFrame={}",
                    resolvedMspId, timeFrame);

            ProductTopicSalesProgressResponseDto response =
                    productTopicSalesProgressService.getMspProductTopicSalesProgress(timeFrame, resolvedMspId);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "MSP sales progress data retrieved successfully", 200, response));
        } catch (CmsServiceException e) {
            log.error("Error fetching MSP product-topic sales progress: {}", e.getMessage());
            HttpStatus status = e.getStatus() != null ? e.getStatus() : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(new ApiResponseDto<>(e.getMessage(), status.value(), null));
        } catch (Exception e) {
            log.error("Error fetching MSP product-topic sales progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve MSP sales progress data: " + e.getMessage(), 500, null));
        }
    }

    private String resolveMspId(String requestMspId) {
        if (requestMspId != null && !requestMspId.isBlank()) {
            return requestMspId;
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && userContext.getUserId() != null && !userContext.getUserId().isBlank()) {
            return userContext.getUserId();
        }

        throw new CmsServiceException("mspId is required", HttpStatus.BAD_REQUEST);
    }
}

