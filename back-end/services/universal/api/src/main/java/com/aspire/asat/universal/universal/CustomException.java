package com.aspire.asat.universal.universal;


public class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message); // This stores the message and makes it accessible via getMessage()
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause); // Allows you to pass a root cause if needed
    }
}