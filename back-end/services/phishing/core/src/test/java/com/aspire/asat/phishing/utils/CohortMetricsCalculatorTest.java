package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.CohortMetricsResult;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.UserActivityCounters;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CohortMetricsCalculatorTest {

    @Test
    void computeShouldMatchDashboardAggregationTierRules() {
        UserActivityCounters critical = UserActivityCounters.builder()
                .userId("u1")
                .email("u1@asat.com")
                .delivered(10)
                .clicked(6)
                .submits(3)
                .reported(2)
                .phishingRiskScore(60.0)
                .build();
        UserActivityCounters high = UserActivityCounters.builder()
                .userId("u2")
                .email("u2@asat.com")
                .delivered(10)
                .clicked(4)
                .submits(0)
                .reported(1)
                .phishingRiskScore(40.0)
                .build();

        CohortMetricsResult result = CohortMetricsCalculator.compute(List.of(critical, high), 55.0, 20.0);

        assertEquals(2, result.totalUsers());
        assertEquals(50.0, result.averagePhishingRiskScore());
        assertEquals(RiskLevel.MEDIUM, result.cohortRiskLevel());
        assertEquals(1, result.phishProneCriticalCount());
        assertEquals(0, result.phishProneHighCount());
        assertEquals(3, result.informationSubmitCount());
        assertEquals(15.0, result.reportRatePercent());
        assertEquals("DOWN", result.reportRateDirection());
        assertEquals("Improving", result.riskTrendStatus());
        assertEquals("UP", result.riskTrendDirection());
        assertEquals("u1", result.topRiskUsers().get(0).getUserId());
    }

    @Test
    void computeShouldHandleZeroDivisionCases() {
        UserActivityCounters user = UserActivityCounters.builder()
                .userId("u1")
                .email("u1@asat.com")
                .delivered(0)
                .clicked(0)
                .submits(0)
                .reported(0)
                .phishingRiskScore(0.0)
                .build();

        CohortMetricsResult result = CohortMetricsCalculator.compute(List.of(user), 0.0, 0.0);

        assertEquals(0.0, result.reportRatePercent());
        assertEquals("FLAT", result.reportRateDirection());
        assertEquals("Stable", result.riskTrendStatus());
        assertEquals("FLAT", result.riskTrendDirection());
    }

    @Test
    void phishingRiskScoreFromActivityCountsShouldUseWorstSeverity() {
        assertEquals(100.0, CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(1, 1, 1, 0));
        assertEquals(75.0, CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(1, 1, 0, 0));
        assertEquals(10.0, CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(1, 0, 0, 1));
        assertEquals(0.0, CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(0, 0, 0, 1));
        assertEquals(20.0, CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(1, 0, 0, 0));
    }
}
