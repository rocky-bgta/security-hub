package com.aspire.asat.registration.exception;

/**
 * Thrown when a client admin cannot be activated due to invalid conditions or errors.
 */
public class ClientActivationException extends RuntimeException {

    public ClientActivationException() {
        super("Client admin could not be activated.");
    }

    public ClientActivationException(String message) {
        super(message);
    }

    public ClientActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
