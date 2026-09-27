package com.aspire.asat.registration.exception;

public class ClientDoesNotExistException extends RegistrationServiceException {

    public ClientDoesNotExistException() {
    }

    public ClientDoesNotExistException(String message) {
        super(message);
    }

    public ClientDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}

