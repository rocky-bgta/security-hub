package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class PackageNotFoundException extends CmsServiceException {
    public PackageNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
