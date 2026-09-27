package com.aspire.asat.registration.exception;

public class InvalidContentTypeException extends RegistationValidationException {
    public InvalidContentTypeException(String typeCannotBeNull) {
        super(typeCannotBeNull);
    }
}
