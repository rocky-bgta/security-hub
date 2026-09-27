package com.aspire.asat.phishing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;

    public ServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}

