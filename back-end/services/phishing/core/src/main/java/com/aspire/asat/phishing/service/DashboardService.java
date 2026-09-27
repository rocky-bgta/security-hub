package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.response.*;
import java.util.List;

/**
 * Service interface for dashboard operations.
 */
public interface DashboardService {

    /**
     * Get dashboard overview with all key metrics
     */
    DashboardOverviewDto getOverview();

    DashboardOverviewDto getOverview(CampaignChannel channel);

    /**
     * Get dedicated admin dashboard UI payload from aggregate data.
     */
    AdminDashboardUiDto getAdminUiData(int days);

    AdminDashboardUiDto getAdminUiData(int days, CampaignChannel channel);

    /**
     * Get 7 core KPI metrics (from BRD Use Case 2.1.0.1)
     */
    DashboardKpiDto getKpiMetrics(int days);

    DashboardKpiDto getKpiMetrics(int days, CampaignChannel channel);

    /**
     * Get email template, landing page, and sender profile counts for the client dashboard.
     */
    ClientDashboardAssetCountsDto getAssetCounts();

    /**
     * Get total asset inventory counts from campaigns, email templates,
     * sending profiles, and landing pages collections.
     */
    AssetInventoryCountsDto getAssetInventoryCounts();

    /**
     * Get phishing performance percentages from user risk profiles.
     * When {@code mspId} resolves (explicit param, or MSP caller with null param),
     * aggregates only profiles for that MSP's client admins; otherwise platform-wide.
     */
    PhishingPerformanceDto getPhishingPerformance(String mspId);

    /**
     * Get campaign performance summary, optionally filtered by campaign type.
     */
    List<CampaignPerformanceDto> getCampaignPerformance(int limit, CampaignType campaignType);

    List<CampaignPerformanceDto> getCampaignPerformance(int limit, CampaignType campaignType, CampaignChannel channel);

    /**
     * Get email statistics
     */
    EmailStatsDto getEmailStats();

    EmailStatsDto getEmailStats(CampaignChannel channel);

    /**
     * Get user risk distribution
     */
    UserRiskDistributionDto getUserRiskDistribution();

    UserRiskDistributionDto getUserRiskDistribution(CampaignChannel channel);

    /**
     * Get all dashboard trend series for the lookback window.
     */
    DashboardTrendsDto getDashboardTrends(int days);

    DashboardTrendsDto getDashboardTrends(int days, CampaignChannel channel);

    /**
     * Get historical trends for a single metric (legacy).
     */
    List<TrendDataPointDto> getTrends(String metricType, int days);

    List<TrendDataPointDto> getTrends(String metricType, int days, CampaignChannel channel);

    /**
     * Get breach summary
     */
    BreachSummaryDto getBreachSummary();

    BreachSummaryDto getBreachSummary(CampaignChannel channel);

    /**
     * Refresh dashboard statistics
     */
    void refreshStats();
}
