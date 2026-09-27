package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.HumanRiskTierToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cohort-level human risk score widget (average phishing risk for the org).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanRiskScoreUiDto {
    /** Average phishing risk score, 0–100 (integer for UI). */
    private int score;
    /**
     * ASAT tier token: LOW_RISK, MEDIUM_RISK, HIGH_RISK, CRITICAL_RISK.
     */
    private HumanRiskTierToken tier;
}
