package com.aspire.asat.billing.exception;

import com.aspire.asat.common.exception.AspireException;
import org.springframework.http.HttpStatus;

public class CouponValidationException extends AspireException {
    private final HttpStatus status;

    public CouponValidationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public CouponValidationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
