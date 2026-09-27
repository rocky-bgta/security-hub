package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class AzureUploadException extends BillingServiceException {
    public AzureUploadException(String message) {
        super(message, HttpStatus.FAILED_DEPENDENCY); // 424 Failed Dependency
    }
}
