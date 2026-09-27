package com.aspire.asat.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedResourceException extends ServiceException {

    public UnauthorizedResourceException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
