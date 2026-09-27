package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class PackageNotFoundException extends ServiceException {
    public PackageNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
