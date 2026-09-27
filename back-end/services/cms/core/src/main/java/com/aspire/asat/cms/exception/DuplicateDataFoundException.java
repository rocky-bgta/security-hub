package com.aspire.asat.cms.exception;

public class DuplicateDataFoundException extends RuntimeException{
    public DuplicateDataFoundException() {
        super("Duplicate data found.");
    }

    public DuplicateDataFoundException(String message) {
        super(message);
    }

    public DuplicateDataFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
