package com.aspire.asat.auth.dto.enums;

/**
 * Machine-readable OTP error codes returned to the frontend.
 */
public final class OtpErrorCode {

    public static final String INVALID = "OTP_INVALID";
    public static final String EXPIRED = "OTP_EXPIRED";
    public static final String LOCKED = "OTP_LOCKED";
    public static final String RATE_LIMITED = "OTP_RATE_LIMITED";
    public static final String COOLDOWN = "OTP_COOLDOWN";

    private OtpErrorCode() {
    }
}
