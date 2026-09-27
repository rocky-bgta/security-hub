package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded minimal top-risk row for dashboard table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopRiskUserSnapshot {
    private String userId;
    private String email;
    private double phishingRiskScore;
    private RiskLevel riskLevel;
}
