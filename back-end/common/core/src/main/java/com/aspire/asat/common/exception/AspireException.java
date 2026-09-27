package com.aspire.asat.common.exception;

public class AspireException extends RuntimeException {

    public AspireException() {

    }

    public AspireException(String message) {
        super(message);
    }

    public AspireException(String message, Throwable cause) {
        super(message, cause);
    }
}

