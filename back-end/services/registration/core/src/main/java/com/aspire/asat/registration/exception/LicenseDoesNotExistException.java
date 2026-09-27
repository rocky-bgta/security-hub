package com.aspire.asat.registration.exception;

public class LicenseDoesNotExistException extends RegistrationServiceException{

    public LicenseDoesNotExistException() {
    }

    public LicenseDoesNotExistException(String message) {
        super(message);
    }

    public LicenseDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
