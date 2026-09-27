package com.aspire.asat.auth.dto.enums;

/**
 * Enum representing the risk group classification for users.
 * CRITICAL_RISK is only exposed when userType is USER (see UserMapper).
 */
public enum RiskGroup {
    HIGH_RISK,
    MEDIUM_RISK,
    LOW_RISK,
    CRITICAL_RISK
}
