package com.aspire.asat.phishing.utils;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskScoreUtilsTest {

    @Test
    void fromScoreShouldMapBoundaryBands() {
        assertEquals(RiskLevel.LOW, RiskScoreUtils.fromScore(0));
        assertEquals(RiskLevel.LOW, RiskScoreUtils.fromScore(20));
        assertEquals(RiskLevel.MEDIUM, RiskScoreUtils.fromScore(21));
        assertEquals(RiskLevel.MEDIUM, RiskScoreUtils.fromScore(50));
        assertEquals(RiskLevel.HIGH, RiskScoreUtils.fromScore(51));
        assertEquals(RiskLevel.HIGH, RiskScoreUtils.fromScore(70));
        assertEquals(RiskLevel.CRITICAL, RiskScoreUtils.fromScore(71));
        assertEquals(RiskLevel.CRITICAL, RiskScoreUtils.fromScore(100));
    }

    @Test
    void roundToTwoDecimalsShouldRoundHalfUpStyleViaMathRound() {
        assertEquals(37.45, RiskScoreUtils.roundToTwoDecimals(37.452779276848354));
        assertEquals(31.75, RiskScoreUtils.roundToTwoDecimals(31.75));
        assertEquals(16.79, RiskScoreUtils.roundToTwoDecimals(16.794642857142858));
        assertEquals(null, RiskScoreUtils.roundToTwoDecimals((Double) null));
    }
}
