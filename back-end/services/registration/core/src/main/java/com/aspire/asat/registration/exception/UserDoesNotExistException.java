package com.aspire.asat.registration.exception;

public class UserDoesNotExistException extends RuntimeException {
    public UserDoesNotExistException() {
    }

    public UserDoesNotExistException(String message) {
        super(message);
    }

    public UserDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
