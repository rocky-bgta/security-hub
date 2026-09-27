package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseServiceException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BaseServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
