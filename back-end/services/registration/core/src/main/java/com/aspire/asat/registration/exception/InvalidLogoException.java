package com.aspire.asat.registration.exception;

public class InvalidLogoException extends RegistationValidationException {

    public InvalidLogoException(String nameCannotBeNull) {
        super(nameCannotBeNull);
    }

}
