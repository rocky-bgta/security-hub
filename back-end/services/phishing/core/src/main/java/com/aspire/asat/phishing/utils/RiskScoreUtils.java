package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.RiskLevel;

/**
 * Maps numeric risk score (0–100) to the 4-class risk classification and computes overall score.
 * Low (0–20), Medium (21–50), High (51–70), Critical (71–100).
 */
public final class RiskScoreUtils {

    private RiskScoreUtils() {}

    private static final double LOW_MAX = 20.0;
    private static final double MEDIUM_MAX = 50.0;
    private static final double HIGH_MAX = 70.0;

    /**
     * Derives risk level from numeric risk score.
     *
     * @param riskScore score in range 0–100 (values outside are clamped)
     * @return LOW (0–20), MEDIUM (21–50), HIGH (51–70), CRITICAL (71–100)
     */
    public static RiskLevel fromScore(double riskScore) {
        if (Double.isNaN(riskScore) || riskScore <= LOW_MAX) {
            return RiskLevel.LOW;
        }
        if (riskScore <= MEDIUM_MAX) {
            return RiskLevel.MEDIUM;
        }
        if (riskScore <= HIGH_MAX) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.CRITICAL;
    }

    /**
     * Scenario C: map training 0–100 to 0–25.
     * Spec: Completed=0, Ongoing=15, Not Completed=20, Overdue>30d=25.
     * CMS sends: 0, 40, 70, 100 → we map to 0, 15, 20, 25.
     */
    public static double trainingPointsTo25(double trainingRiskScore) {
        if (trainingRiskScore <= 20) return 0;   // 0 (completed) → 0
        if (trainingRiskScore <= 50) return 15;  // 40 (ongoing) → 15
        if (trainingRiskScore <= 85) return 20;  // 70 (not completed) → 20
        return 25;                               // 100 (overdue) → 25
    }

    /**
     * Scenario C: map phishing 0–100 to 0–75.
     * Spec: none/reported=0, opened+reported=10, opened=30, clicked=60, submitted=75.
     * Raw (Scenario B): 0, 0, 10, 20, 75, 100 → we map to 0, 10, 30, 60, 75.
     */
    public static double phishingPointsTo75(double phishingRiskScore) {
        if (phishingRiskScore <= 0) return 0;
        if (phishingRiskScore <= 15) return 10;  // 10 → 10
        if (phishingRiskScore <= 50) return 30;   // 20 → 30
        if (phishingRiskScore <= 87) return 60;  // 75 → 60
        return 75;                               // 100 → 75
    }

    /**
     * Computes overall risk score: Scenario A (training only), B (phishing only), or C (both 25/75).
     */
    public static double computeOverallRiskScore(double trainingRiskScore, double phishingRiskScore,
                                                 Boolean isTrainingEnabled, Boolean isPhishingEnabled, Double trainingWeight, Double phishingWeight) {
        boolean trainingOn = Boolean.TRUE.equals(isTrainingEnabled);
        boolean phishingOn = Boolean.TRUE.equals(isPhishingEnabled);
        if (trainingOn && phishingOn) {
            return trainingWeight*trainingRiskScore + phishingWeight*phishingRiskScore;
        }
        if (trainingOn) return trainingRiskScore;
        if (phishingOn) return phishingRiskScore;
        return trainingRiskScore != 0 ? trainingRiskScore : phishingRiskScore;
    }

    /**
     * Rounds a risk score to 2 decimal places for API/export responses.
     * Returns {@code null} unchanged; NaN is returned as-is.
     */
    public static Double roundToTwoDecimals(Double value) {
        if (value == null || Double.isNaN(value)) {
            return value;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Rounds a risk score to 2 decimal places for primitive scores.
     */
    public static double roundToTwoDecimals(double value) {
        if (Double.isNaN(value)) {
            return value;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
