package com.aspire.asat.registration.exception;

import com.aspire.asat.common.exception.AspireException;

public class RegistrationServiceException extends AspireException {

    public RegistrationServiceException() {

    }

    public RegistrationServiceException(String message) {
        super(message);
    }

    public RegistrationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

