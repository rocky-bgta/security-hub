package com.aspire.asat.cms.exception;

/**
 * Exception thrown when client exam settings already exist for a client
 */
public class ExamSettingsAlreadyExistsException extends RuntimeException {
    
    public ExamSettingsAlreadyExistsException(String message) {
        super(message);
    }
    
    public ExamSettingsAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
