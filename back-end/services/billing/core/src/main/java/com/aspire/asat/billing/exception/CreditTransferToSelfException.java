package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CreditTransferToSelfException extends BillingServiceException {

    public CreditTransferToSelfException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // 400
    }
}
