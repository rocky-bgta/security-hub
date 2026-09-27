package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class InvalidCouponException extends BillingServiceException {

    public InvalidCouponException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // 400
    }
}
