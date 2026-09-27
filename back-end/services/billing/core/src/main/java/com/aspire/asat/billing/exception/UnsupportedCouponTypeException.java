package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedCouponTypeException extends BillingServiceException {
    public UnsupportedCouponTypeException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY); // 422 Unprocessable Entity
    }
}
