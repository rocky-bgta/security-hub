package com.aspire.asat.registration.exception;

public class PackagesDoesNotExistException extends RegistrationServiceException {

    public PackagesDoesNotExistException() {
    }

    public PackagesDoesNotExistException(String message) {
        super(message);
    }

    public PackagesDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
