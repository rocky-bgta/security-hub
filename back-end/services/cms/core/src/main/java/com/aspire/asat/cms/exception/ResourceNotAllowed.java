package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotAllowed extends CmsServiceException {
    public ResourceNotAllowed(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}
