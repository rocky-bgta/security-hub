package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class ContentNotFoundException extends ServiceException {
    public ContentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
