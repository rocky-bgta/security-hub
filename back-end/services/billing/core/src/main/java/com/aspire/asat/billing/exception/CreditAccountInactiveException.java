package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CreditAccountInactiveException extends BillingServiceException {

    public CreditAccountInactiveException(String message) {
        super(message, HttpStatus.FORBIDDEN); // 403
    }
}
