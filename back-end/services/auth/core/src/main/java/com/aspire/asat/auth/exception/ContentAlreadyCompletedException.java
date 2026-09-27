package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class ContentAlreadyCompletedException extends ServiceException {
    public ContentAlreadyCompletedException(String message) {
        super(message, HttpStatus.CONFLICT); // 409
    }
}
