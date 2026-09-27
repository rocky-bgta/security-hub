package com.aspire.asat.registration.exception;

public class CompanyDoesNotExsistException extends RuntimeException {
    public CompanyDoesNotExsistException() {
    }

    public CompanyDoesNotExsistException(String message) {
        super(message);
    }

    public CompanyDoesNotExsistException(String message, Throwable cause) {
        super(message, cause);
    }
}
