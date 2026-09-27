package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class InvalidContentTypeException extends CmsServiceException {
    public InvalidContentTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
