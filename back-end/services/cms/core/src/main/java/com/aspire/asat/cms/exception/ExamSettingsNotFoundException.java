package com.aspire.asat.cms.exception;

/**
 * Exception thrown when client exam settings are not found
 */
public class ExamSettingsNotFoundException extends RuntimeException {
    
    public ExamSettingsNotFoundException(String message) {
        super(message);
    }
    
    public ExamSettingsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
