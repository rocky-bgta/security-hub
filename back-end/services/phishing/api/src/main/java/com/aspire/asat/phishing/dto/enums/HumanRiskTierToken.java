package com.aspire.asat.phishing.dto.enums;

/**
 * Human risk score tier tokens exposed to the admin dashboard UI (SRS bands).
 */
public enum HumanRiskTierToken {
    LOW_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    CRITICAL_RISK;

    public static HumanRiskTierToken fromRiskLevel(RiskLevel level) {
        if (level == null) {
            return LOW_RISK;
        }
        return switch (level) {
            case LOW -> LOW_RISK;
            case MEDIUM -> MEDIUM_RISK;
            case HIGH -> HIGH_RISK;
            case CRITICAL -> CRITICAL_RISK;
        };
    }
}
