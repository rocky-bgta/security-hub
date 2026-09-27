package com.aspire.asat.auth.constant;

/**
 * i18n keys for OTP lifecycle messages. Resolved via {@code message_en.properties}.
 */
public final class OtpMessages {

    public static final String INVALID = "mfa.otp.invalid";
    public static final String EXPIRED = "mfa.otp.expired";
    public static final String LOCKED = "mfa.otp.locked";
    public static final String RATE_LIMITED = "mfa.otp.rate.limited";

    private OtpMessages() {
    }
}
