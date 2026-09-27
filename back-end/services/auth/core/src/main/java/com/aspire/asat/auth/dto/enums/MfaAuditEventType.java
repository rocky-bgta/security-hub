package com.aspire.asat.auth.dto.enums;

/**
 * Security audit events for the OTP lifecycle. Never include the OTP value.
 */
public enum MfaAuditEventType {
    OTP_GENERATED,
    OTP_RESENT,
    OTP_INVALIDATED,
    OTP_VERIFY_FAILED,
    OTP_VERIFY_SUCCESS,
    OTP_EXPIRED_REJECTED,
    OTP_LOCKED,
    OTP_COOLDOWN_BLOCKED,
    OTP_RATE_LIMITED
}
