package com.aspire.asat.auth.exception;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.OtpErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when OTP generate/resend/verify rate limits are exceeded.
 */
public class OtpRateLimitException extends MfaException {

    public OtpRateLimitException() {
        this(null);
    }

    public OtpRateLimitException(Integer retryAfterSeconds) {
        super(OtpMessages.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, OtpErrorCode.RATE_LIMITED, retryAfterSeconds);
    }
}
