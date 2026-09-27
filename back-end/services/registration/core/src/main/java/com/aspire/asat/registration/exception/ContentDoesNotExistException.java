package com.aspire.asat.registration.exception;

public class ContentDoesNotExistException extends RegistrationServiceException {

    public ContentDoesNotExistException() {
    }

    public ContentDoesNotExistException(String message) {
        super(message);
    }

    public ContentDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
