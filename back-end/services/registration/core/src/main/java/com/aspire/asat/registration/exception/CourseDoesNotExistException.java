package com.aspire.asat.registration.exception;

public class CourseDoesNotExistException extends RegistrationServiceException {

    public CourseDoesNotExistException() {
    }

    public CourseDoesNotExistException(String message) {
        super(message);
    }

    public CourseDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
