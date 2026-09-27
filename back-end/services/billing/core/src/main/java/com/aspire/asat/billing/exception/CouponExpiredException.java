package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CouponExpiredException extends BillingServiceException {
    public CouponExpiredException(String message) {
        super(message, HttpStatus.GONE); // 410 Gone
    }
}
