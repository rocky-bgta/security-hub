package com.aspire.asat.registration.exception;

public class PackageNotFoundException extends RuntimeException {

    public PackageNotFoundException() {
        super("Package not found.");
    }

    public PackageNotFoundException(String message) {
        super(message);
    }

    public PackageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
