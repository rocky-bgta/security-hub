package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class ContentAlreadyCompletedException extends CmsServiceException {
    public ContentAlreadyCompletedException(String message) {
        super(message, HttpStatus.CONFLICT); // 409
    }
}
