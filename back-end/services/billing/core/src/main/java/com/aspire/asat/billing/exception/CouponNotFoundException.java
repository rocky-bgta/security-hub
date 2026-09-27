package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CouponNotFoundException extends BillingServiceException {

    public CouponNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND); // 404
    }
}
