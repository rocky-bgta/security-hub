package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.PhishingRiskScore;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.RiskTrend;
import com.aspire.asat.phishing.model.TopRiskUserSnapshot;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared cohort-level metrics calculation for admin dashboard widgets.
 */
public final class CohortMetricsCalculator {

    private CohortMetricsCalculator() {
    }

    @Builder
    public record UserActivityCounters(
            String userId,
            String email,
            int delivered,
            int opened,
            int clicked,
            int submits,
            int reported,
            double phishingRiskScore) {
    }

    public record CohortMetricsResult(
            int totalUsers,
            double averagePhishingRiskScore,
            RiskLevel cohortRiskLevel,
            int phishProneCriticalCount,
            int phishProneHighCount,
            int informationSubmitCount,
            double reportRatePercent,
            String reportRateDirection,
            String riskTrendStatus,
            String riskTrendDirection,
            List<TopRiskUserSnapshot> topRiskUsers) {
    }

    public static CohortMetricsResult compute(
            List<UserActivityCounters> users,
            double previousAvgPhishingRisk,
            double previousReportRatePercent) {

        if (users == null || users.isEmpty()) {
            return emptyResult();
        }

        int totalUsers = users.size();
        double avgPhishingRisk = users.stream()
                .mapToDouble(UserActivityCounters::phishingRiskScore)
                .average()
                .orElse(0.0);

        long criticalCount = 0;
        long highCount = 0;
        long informationSubmitCount = 0;
        long totalReported = 0;
        long totalDelivered = 0;

        for (UserActivityCounters user : users) {
            int delivered = Math.max(0, user.delivered());
            int clicked = Math.max(0, user.clicked());
            int submits = Math.max(0, user.submits());
            int reports = Math.max(0, user.reported());

            double failureRate = delivered > 0 ? (double) (clicked + submits) / delivered : 0.0;
            if (failureRate > 0.8 || submits > 0) {
                criticalCount++;
            } else if (failureRate >= 0.5) {
                highCount++;
            }

            informationSubmitCount += submits;
            totalReported += reports;
            totalDelivered += delivered;
        }

        double reportRate = totalDelivered > 0 ? truncate1((double) totalReported / totalDelivered * 100.0) : 0.0;
        String reportDirection = trendDirection(reportRate, previousReportRatePercent);

        double delta = previousAvgPhishingRisk == 0.0
                ? 0.0
                : ((avgPhishingRisk - previousAvgPhishingRisk) / previousAvgPhishingRisk) * 100.0;
        RiskTrend riskTrend = RiskTrend.fromDelta(delta);

        List<TopRiskUserSnapshot> topRiskUsers = users.stream()
                .sorted(Comparator.comparing(UserActivityCounters::phishingRiskScore, Comparator.reverseOrder()))
                .limit(100)
                .map(user -> TopRiskUserSnapshot.builder()
                        .userId(user.userId())
                        .email(user.email())
                        .phishingRiskScore(round1(user.phishingRiskScore()))
                        .riskLevel(RiskScoreUtils.fromScore(user.phishingRiskScore()))
                        .build())
                .toList();

        return new CohortMetricsResult(
                totalUsers,
                round1(avgPhishingRisk),
                RiskScoreUtils.fromScore(avgPhishingRisk),
                (int) criticalCount,
                (int) highCount,
                (int) informationSubmitCount,
                reportRate,
                reportDirection,
                riskTrend.getStatus(),
                riskTrend.getDirection(),
                topRiskUsers);
    }

    /**
     * Derives worst-case phishing risk score from activity counts within a window.
     */
    public static double phishingRiskScoreFromActivityCounts(int opened, int clicked, int submits, int reported) {
        if (submits > 0) {
            return PhishingRiskScore.DATA_SUBMITTED.getPoints();
        }
        if (clicked > 0) {
            return PhishingRiskScore.CLICKED.getPoints();
        }
        if (reported > 0) {
            return opened > 0
                    ? PhishingRiskScore.OPENED_BUT_REPORTED.getPoints()
                    : PhishingRiskScore.REPORTED_WITHOUT_OPEN.getPoints();
        }
        if (opened > 0) {
            return PhishingRiskScore.OPENED.getPoints();
        }
        return PhishingRiskScore.NONE.getPoints();
    }

    public static UserActivityCounters fromUserRiskProfile(com.aspire.asat.phishing.model.UserRiskProfile profile) {
        double score = profile.getPhishingRiskScore() == null ? 0.0 : profile.getPhishingRiskScore();
        return UserActivityCounters.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .delivered(Math.max(0, profile.getEmailsReceived()))
                .opened(Math.max(0, profile.getEmailsOpened()))
                .clicked(Math.max(0, profile.getLinksClicked()))
                .submits(Math.max(0, profile.getDataSubmissions()))
                .reported(Math.max(0, profile.getEmailsReported()))
                .phishingRiskScore(score)
                .build();
    }

    public static UserActivityCounters fromWindowMetrics(
            RecipientWindowActivityMetrics row,
            String userId) {
        double score = phishingRiskScoreFromActivityCounts(
                row.opened(), row.clicked(), row.submits(), row.reported());
        return UserActivityCounters.builder()
                .userId(userId)
                .email(row.recipientEmail())
                .delivered(row.delivered())
                .opened(row.opened())
                .clicked(row.clicked())
                .submits(row.submits())
                .reported(row.reported())
                .phishingRiskScore(score)
                .build();
    }

    private static CohortMetricsResult emptyResult() {
        return new CohortMetricsResult(
                0, 0.0, RiskLevel.LOW, 0, 0, 0, 0.0,
                "FLAT", RiskTrend.STABLE.getStatus(), RiskTrend.STABLE.getDirection(),
                new ArrayList<>());
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double truncate1(double value) {
        return Math.floor(value * 10.0) / 10.0;
    }

    private static String trendDirection(double current, double previous) {
        if (current > previous) {
            return "UP";
        }
        if (current < previous) {
            return "DOWN";
        }
        return "FLAT";
    }
}
