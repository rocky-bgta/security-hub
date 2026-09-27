package com.aspire.asat.billing.exception;

import com.aspire.asat.common.exception.AspireException;
import org.springframework.http.HttpStatus;

public class BillingServiceException extends AspireException {

    private final HttpStatus status;

    public BillingServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BillingServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
