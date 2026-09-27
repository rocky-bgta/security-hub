package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when MFA is disabled globally
 */
public class MfaDisabledException extends MfaException {

    public MfaDisabledException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

