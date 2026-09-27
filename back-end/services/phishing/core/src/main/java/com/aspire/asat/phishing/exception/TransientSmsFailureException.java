package com.aspire.asat.phishing.exception;

public class TransientSmsFailureException extends RuntimeException {

    public TransientSmsFailureException(String message) {
        super(message);
    }

    public TransientSmsFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
