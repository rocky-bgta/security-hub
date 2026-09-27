package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ServiceException {
    public DatabaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}
