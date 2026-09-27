package com.aspire.asat.auth.dto.enums;

/**
 * Lifecycle status for a stored MFA OTP code.
 */
public enum MfaCodeStatus {
    ACTIVE,
    USED,
    INVALIDATED,
    EXPIRED
}
