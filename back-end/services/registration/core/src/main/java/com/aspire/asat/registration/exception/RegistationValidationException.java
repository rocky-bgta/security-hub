package com.aspire.asat.registration.exception;

import com.aspire.asat.common.exception.AspireValidationException;

import java.util.LinkedList;
import java.util.List;

public class RegistationValidationException extends RegistrationServiceException implements AspireValidationException {

    private final List<AspireValidationException> aspireValidationExceptions = new LinkedList<>();

    public RegistationValidationException() {
    }

    public RegistationValidationException(String message) {
        super(message);
    }

    public RegistationValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public void addValidationException(AspireValidationException exceptionsToAdd) {
        aspireValidationExceptions.add(exceptionsToAdd);
    }

    @Override
    public List<? extends AspireValidationException> getAllValidationException() {
        return aspireValidationExceptions;
    }

}
