package com.aspire.asat.registration.exception;

public class InvalidContentNameException extends RegistationValidationException {

    public InvalidContentNameException(String nameCannotBeNull) {
        super(nameCannotBeNull);
    }
}
