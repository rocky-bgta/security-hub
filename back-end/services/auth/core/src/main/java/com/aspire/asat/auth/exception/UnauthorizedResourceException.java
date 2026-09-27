package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedResourceException extends ServiceException {

    public UnauthorizedResourceException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
