package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller interface for dashboard endpoints.
 */
@Tag(name = "Dashboard", description = "APIs for dashboard and overview metrics")
@RequestMapping(value = WebApiUrlConstants.DASHBOARD_PATH)
public interface DashboardController {

    @Operation(summary = "Get dashboard overview", 
               description = "Retrieves comprehensive dashboard overview with all key metrics")
    @GetMapping("/overview")
    ResponseEntity<ApiResponseDto<DashboardOverviewDto>> getOverview(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get KPI metrics", 
               description = "Retrieves 7 core KPI cards: Attacks, Hacks, Reports, Campaigns, Templates, Groups, Landing Pages")
    @GetMapping("/kpi-metrics")
    ResponseEntity<ApiResponseDto<DashboardKpiDto>> getKpiMetrics(
            @Parameter(description = "Lookback window in days (7, 14, 30, 90, 365)")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get client asset counts",
               description = "Returns counts of email templates, landing pages, and sender profiles accessible to the current client admin (tenant + global assets)")
    @GetMapping("/asset-counts")
    ResponseEntity<ApiResponseDto<ClientDashboardAssetCountsDto>> getAssetCounts();

    @Operation(summary = "Get asset inventory counts",
               description = "Returns total counts of campaign presets, email templates, sending profiles, and landing pages from each collection.")
    @GetMapping("/asset-inventory-counts")
    ResponseEntity<ApiResponseDto<AssetInventoryCountsDto>> getAssetInventoryCounts();

    @Operation(summary = "Get phishing performance",
               description = "Returns reported, ignored/not opened, and clicked percentages from user risk profiles. "
                       + "Optional mspId scopes to that MSP's client admins; if omitted and caller is MSP, uses current userId.")
    @GetMapping("/phishing-performance")
    ResponseEntity<ApiResponseDto<PhishingPerformanceDto>> getPhishingPerformance(
            @Parameter(description = "MSP ID (optional; defaults to current userId when caller is MSP)")
            @RequestParam(value = "mspId", required = false) String mspId
    );

    @Operation(summary = "Get campaign performance", 
               description = "Retrieves campaign performance summary, optionally filtered by campaign type")
    @GetMapping("/campaigns")
    ResponseEntity<ApiResponseDto<List<CampaignPerformanceDto>>> getCampaignPerformance(
            @Parameter(description = "Number of campaigns to return") 
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Optional campaign type filter")
            @RequestParam(value = "campaignType", required = false) CampaignType campaignType,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get email statistics", 
               description = "Retrieves aggregated email metrics")
    @GetMapping("/email-stats")
    ResponseEntity<ApiResponseDto<EmailStatsDto>> getEmailStats(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get user risk distribution", 
               description = "Retrieves user risk level distribution")
    @GetMapping("/user-risk")
    ResponseEntity<ApiResponseDto<UserRiskDistributionDto>> getUserRiskDistribution(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get all dashboard trend series",
               description = "Retrieves open, click, submission, and report rate trends for the lookback window")
    @GetMapping("/trends/all")
    ResponseEntity<ApiResponseDto<DashboardTrendsDto>> getDashboardTrends(
            @Parameter(description = "Lookback window in days (7, 14, 30, 90, 365)")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get historical trends",
               description = "Retrieves historical trends for specified metric")
    @GetMapping("/trends")
    ResponseEntity<ApiResponseDto<List<TrendDataPointDto>>> getTrends(
            @Parameter(description = "Metric type: openRate, clickRate, compromiseRate, reportRate, phishProne")
            @RequestParam(defaultValue = "openRate") String metricType,
            @Parameter(description = "Lookback window in days (7, 14, 30, 90, 365)")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get breach summary", 
               description = "Retrieves breach detection summary")
    @GetMapping("/breach-summary")
    ResponseEntity<ApiResponseDto<BreachSummaryDto>> getBreachSummary(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Refresh dashboard stats", 
               description = "Refreshes and recalculates dashboard statistics")
    @PostMapping("/refresh")
    ResponseEntity<ApiResponseDto<String>> refreshStats();


    @Operation(summary = "Admin security risk dashboard (widgets)",
            description = "Returns humanRiskScore, phishProneUsers tier counts, informationSubmits, reportRate (current + trend), riskTrend; optional topRiskUsers when non-empty")
    @GetMapping("/phish-prone-data")
    ResponseEntity<ApiResponseDto<AdminDashboardUiDto>> getAdminUiData(
            @Parameter(description = "Lookback window in days (7, 14, 30, 90, 365)")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

}
