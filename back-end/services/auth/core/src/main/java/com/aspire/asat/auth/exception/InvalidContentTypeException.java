package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class InvalidContentTypeException extends ServiceException {
    public InvalidContentTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
