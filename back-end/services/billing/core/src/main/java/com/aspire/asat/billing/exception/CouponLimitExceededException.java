package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class CouponLimitExceededException extends BillingServiceException {
    public CouponLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS); // 429 Too Many Requests
    }
}
