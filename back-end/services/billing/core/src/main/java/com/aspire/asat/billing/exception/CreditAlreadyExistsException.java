package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CreditAlreadyExistsException extends BillingServiceException {

    public CreditAlreadyExistsException(String clientId) {
        super("Credit already exists for client ID: " + clientId, HttpStatus.CONFLICT);
    }
}
