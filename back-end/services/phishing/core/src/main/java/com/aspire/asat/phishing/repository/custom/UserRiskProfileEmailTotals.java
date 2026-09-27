package com.aspire.asat.phishing.repository.custom;

/**
 * Aggregated email counters summed from {@code user_risk_profiles}.
 */
public record UserRiskProfileEmailTotals(
        long emailsReceived,
        long emailsOpened,
        long emailsReported,
        long linksClicked
) {
    public static UserRiskProfileEmailTotals empty() {
        return new UserRiskProfileEmailTotals(0, 0, 0, 0);
    }
}
