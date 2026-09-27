package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class MinPurchaseAmountNotMetException extends BillingServiceException {
    public MinPurchaseAmountNotMetException(String message) {
        super(message, HttpStatus.PRECONDITION_FAILED); // 412 Precondition Failed
    }
}
