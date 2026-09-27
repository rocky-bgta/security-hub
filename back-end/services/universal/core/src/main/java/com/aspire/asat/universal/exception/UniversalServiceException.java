package com.aspire.asat.universal.exception;

import com.aspire.asat.common.exception.AspireException;

public class UniversalServiceException extends AspireException {

    public UniversalServiceException() {

    }

    public UniversalServiceException(String message) {
        super(message);
    }

    public UniversalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

