package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class InvalidCurrencyException extends BillingServiceException {
    public InvalidCurrencyException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }
}
