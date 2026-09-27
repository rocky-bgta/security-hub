package com.aspire.asat.auth.exception;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.OtpErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when OTP verification is locked due to too many failed attempts
 */
public class OtpLockedException extends MfaException {

    public OtpLockedException() {
        this(OtpMessages.LOCKED, null);
    }

    public OtpLockedException(String message) {
        this(message, null);
    }

    public OtpLockedException(Integer retryAfterSeconds) {
        this(OtpMessages.LOCKED, retryAfterSeconds);
    }

    public OtpLockedException(String message, Integer retryAfterSeconds) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, OtpErrorCode.LOCKED, retryAfterSeconds);
    }
}
