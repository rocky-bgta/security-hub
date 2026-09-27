package com.aspire.asat.common.exception;

import org.springframework.http.HttpStatus;

public class ContentNotFoundException extends ServiceException {
    public ContentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
