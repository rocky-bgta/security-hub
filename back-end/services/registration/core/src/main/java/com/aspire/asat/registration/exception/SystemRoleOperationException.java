package com.aspire.asat.registration.exception;

/**
 * Exception thrown when attempting to perform operations on system roles
 * that are not allowed (create, update, delete).
 */
public class SystemRoleOperationException extends RuntimeException {
    
    public SystemRoleOperationException(String message) {
        super(message);
    }
    
    public SystemRoleOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
