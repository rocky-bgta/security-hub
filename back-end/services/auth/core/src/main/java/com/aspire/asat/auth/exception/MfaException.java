package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for MFA-related errors
 */
public class MfaException extends ServiceException {

    private final String errorCode;
    private final Integer retryAfterSeconds;

    public MfaException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null, null);
    }

    public MfaException(String message, HttpStatus status) {
        this(message, status, null, null);
    }

    public MfaException(String message, HttpStatus status, String errorCode, Integer retryAfterSeconds) {
        super(message, status);
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
