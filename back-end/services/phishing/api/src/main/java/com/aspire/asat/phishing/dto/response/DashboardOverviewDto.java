package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for dashboard overview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {

    // Campaign Summary
    private int totalCampaigns;
    private int activeCampaigns;
    private int completedCampaigns;
    private int draftCampaigns;

    // Email Metrics
    private int totalEmailsSent;
    private double avgOpenRate;
    private double avgClickRate;
    private double phishPronePercentage;

    // User Risk
    private int totalUsers;
    private int highRiskUsers;
    private int criticalRiskUsers;
    private int repeatOffenders;
    private double averagePhishingRiskScore;
    private String cohortRiskLevel;
    private String phishProneUsers;
    private int informationSubmits;
    private String reportRateWithTrend;
    private String riskTrend;

    // Breach Detection
    private int breachesDetected;
    private int affectedUsers;

    // Trends (last 30 days)
    @Builder.Default
    private List<TrendDataPointDto> openRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> clickRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> submissionRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> reportRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TopRiskUserDto> topRiskUsers = new ArrayList<>();
}
