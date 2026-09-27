package com.aspire.asat.registration.exception;

public class LessonDoesNotExistException extends RegistrationServiceException{

    public LessonDoesNotExistException() {
    }

    public LessonDoesNotExistException(String message) {
        super(message);
    }

    public LessonDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
