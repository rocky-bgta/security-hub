package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransactionAmountException extends BillingServiceException {
    public InvalidTransactionAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
