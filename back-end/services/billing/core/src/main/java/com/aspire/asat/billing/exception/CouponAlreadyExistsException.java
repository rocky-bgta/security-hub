package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CouponAlreadyExistsException extends BillingServiceException {

    public CouponAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT); // 409
    }
}
