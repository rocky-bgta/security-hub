package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a specific MFA method is not enabled
 */
public class MfaMethodNotEnabledException extends MfaException {

    public MfaMethodNotEnabledException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

