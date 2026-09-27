package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Embedded model for cohort-level widget metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortMetrics {

    @Builder.Default
    private int totalUsers = 0;

    @Builder.Default
    private double averagePhishingRiskScore = 0.0;

    @Builder.Default
    private RiskLevel cohortRiskLevel = RiskLevel.LOW;

    @Builder.Default
    private int phishProneCriticalCount = 0;

    @Builder.Default
    private int phishProneHighCount = 0;

    @Builder.Default
    private int informationSubmitCount = 0;

    @Builder.Default
    private double reportRatePercent = 0.0;

    /** Report-rate micro-trend vs previous period: UP, DOWN, or FLAT */
    @Builder.Default
    private String reportRateDirection = "FLAT";

    @Builder.Default
    private String riskTrendStatus = "Stable";

    /** Risk trend vs previous avg HRS: UP, DOWN, or FLAT (lower score is better) */
    @Builder.Default
    private String riskTrendDirection = "FLAT";

    @Builder.Default
    private double currentAvgHrs = 0.0;

    @Builder.Default
    private double previousAvgHrs = 0.0;

    private Instant rollingWindowStart;

    private Instant rollingWindowEnd;
}
