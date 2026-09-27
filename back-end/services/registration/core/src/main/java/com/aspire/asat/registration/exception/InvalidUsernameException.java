package com.aspire.asat.registration.exception;

public class InvalidUsernameException extends RegistationValidationException {

    public InvalidUsernameException(String nameCannotBeNull) {
        super(nameCannotBeNull);
    }

}
