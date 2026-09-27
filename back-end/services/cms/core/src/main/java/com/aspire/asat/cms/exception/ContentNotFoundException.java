package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class ContentNotFoundException extends CmsServiceException {
    public ContentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
