package com.aspire.asat.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidContentTypeException extends ServiceException {
    public InvalidContentTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
