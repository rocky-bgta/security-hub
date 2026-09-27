package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.DashboardController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for dashboard endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DashboardControllerImpl implements DashboardController {

    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<ApiResponseDto<DashboardOverviewDto>> getOverview(CampaignChannel channel) {
        try {
            DashboardOverviewDto overview = dashboardService.getOverview(channel);
            return ResponseEntity.ok(ApiResponseDto.<DashboardOverviewDto>builder()
                    .data(overview)
                    .message("Dashboard overview retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting dashboard overview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<DashboardOverviewDto>builder()
                            .message("Failed to retrieve dashboard overview: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AdminDashboardUiDto>> getAdminUiData(int days, CampaignChannel channel) {
        try {
            AdminDashboardUiDto uiData = dashboardService.getAdminUiData(days, channel);
            return ResponseEntity.ok(ApiResponseDto.<AdminDashboardUiDto>builder()
                    .data(uiData)
                    .message("Admin dashboard UI data retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting admin dashboard UI data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AdminDashboardUiDto>builder()
                            .message("Failed to retrieve admin dashboard UI data: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DashboardKpiDto>> getKpiMetrics(int days, CampaignChannel channel) {
        try {
            DashboardKpiDto kpis = dashboardService.getKpiMetrics(days, channel);
            return ResponseEntity.ok(ApiResponseDto.<DashboardKpiDto>builder()
                    .data(kpis)
                    .message("KPI metrics retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting KPI metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<DashboardKpiDto>builder()
                            .message("Failed to retrieve KPI metrics: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientDashboardAssetCountsDto>> getAssetCounts() {
        try {
            ClientDashboardAssetCountsDto counts = dashboardService.getAssetCounts();
            return ResponseEntity.ok(ApiResponseDto.<ClientDashboardAssetCountsDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .data(counts)
                    .message("Client asset counts retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting client asset counts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<ClientDashboardAssetCountsDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve client asset counts: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AssetInventoryCountsDto>> getAssetInventoryCounts() {
        try {
            AssetInventoryCountsDto counts = dashboardService.getAssetInventoryCounts();
            return ResponseEntity.ok(ApiResponseDto.<AssetInventoryCountsDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .data(counts)
                    .message("Asset inventory counts retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting asset inventory counts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<AssetInventoryCountsDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve asset inventory counts: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PhishingPerformanceDto>> getPhishingPerformance(String mspId) {
        try {
            PhishingPerformanceDto performance = dashboardService.getPhishingPerformance(mspId);
            return ResponseEntity.ok(ApiResponseDto.<PhishingPerformanceDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .data(performance)
                    .message("Phishing performance retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting phishing performance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<PhishingPerformanceDto>builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve phishing performance: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<CampaignPerformanceDto>>> getCampaignPerformance(
            int limit, CampaignType campaignType, CampaignChannel channel) {
        try {
            List<CampaignPerformanceDto> performance =
                    dashboardService.getCampaignPerformance(limit, campaignType, channel);
            return ResponseEntity.ok(ApiResponseDto.<List<CampaignPerformanceDto>>builder()
                    .data(performance)
                    .message("Campaign performance retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting campaign performance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<List<CampaignPerformanceDto>>builder()
                            .message("Failed to retrieve campaign performance: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailStatsDto>> getEmailStats(CampaignChannel channel) {
        try {
            EmailStatsDto stats = dashboardService.getEmailStats(channel);
            return ResponseEntity.ok(ApiResponseDto.<EmailStatsDto>builder()
                    .data(stats)
                    .message("Email statistics retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting email stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<EmailStatsDto>builder()
                            .message("Failed to retrieve email statistics: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskDistributionDto>> getUserRiskDistribution(CampaignChannel channel) {
        try {
            UserRiskDistributionDto distribution = dashboardService.getUserRiskDistribution(channel);
            return ResponseEntity.ok(ApiResponseDto.<UserRiskDistributionDto>builder()
                    .data(distribution)
                    .message("User risk distribution retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting user risk distribution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<UserRiskDistributionDto>builder()
                            .message("Failed to retrieve user risk distribution: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DashboardTrendsDto>> getDashboardTrends(int days, CampaignChannel channel) {
        try {
            DashboardTrendsDto trends = dashboardService.getDashboardTrends(days, channel);
            return ResponseEntity.ok(ApiResponseDto.<DashboardTrendsDto>builder()
                    .data(trends)
                    .message("trends retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting dashboard trends", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<DashboardTrendsDto>builder()
                            .message("Failed to retrieve trends: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<TrendDataPointDto>>> getTrends(
            String metricType, int days, CampaignChannel channel) {
        try {
            List<TrendDataPointDto> trends = dashboardService.getTrends(metricType, days, channel);
            return ResponseEntity.ok(ApiResponseDto.<List<TrendDataPointDto>>builder()
                    .data(trends)
                    .message("Trends retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting trends", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<List<TrendDataPointDto>>builder()
                            .message("Failed to retrieve trends: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachSummaryDto>> getBreachSummary(CampaignChannel channel) {
        try {
            BreachSummaryDto summary = dashboardService.getBreachSummary(channel);
            return ResponseEntity.ok(ApiResponseDto.<BreachSummaryDto>builder()
                    .data(summary)
                    .message("Breach summary retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachSummaryDto>builder()
                            .message("Failed to retrieve breach summary: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> refreshStats() {
        try {
            dashboardService.refreshStats();
            return ResponseEntity.ok(ApiResponseDto.<String>builder()
                    .data("success")
                    .message("Dashboard statistics refreshed successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error refreshing stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<String>builder()
                            .message("Failed to refresh statistics: " + e.getMessage())
                            .build());
        }
    }
}
