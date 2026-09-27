package com.aspire.asat.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends ServiceException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }


}
