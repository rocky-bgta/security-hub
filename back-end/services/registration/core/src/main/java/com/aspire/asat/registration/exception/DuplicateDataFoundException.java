package com.aspire.asat.registration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DuplicateDataFoundException extends RuntimeException{
    public DuplicateDataFoundException() {
        super("Duplicate data found.");
    }

    public DuplicateDataFoundException(String message) {
        super(message);
    }

    public DuplicateDataFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
