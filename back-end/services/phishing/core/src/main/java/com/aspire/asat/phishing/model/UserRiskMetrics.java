package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded model for user risk metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskMetrics {

    @Builder.Default
    private int totalUsers = 0;

    @Builder.Default
    private int highRiskUsers = 0;

    @Builder.Default
    private int mediumRiskUsers = 0;

    @Builder.Default
    private int lowRiskUsers = 0;

    @Builder.Default
    private int criticalRiskUsers = 0;

    @Builder.Default
    private int repeatOffenders = 0;  // Clicked 3+ times

    @Builder.Default
    private int compromisedUsers = 0;  // Submitted data at least once
}
