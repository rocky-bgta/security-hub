package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class QrCodeGenerationException extends BillingServiceException {
    public QrCodeGenerationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }
}
