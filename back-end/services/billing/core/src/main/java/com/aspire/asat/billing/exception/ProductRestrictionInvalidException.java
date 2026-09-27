package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class ProductRestrictionInvalidException extends BillingServiceException {
  public ProductRestrictionInvalidException(String message) {
    super(message, HttpStatus.BAD_REQUEST); // 400 Bad Request
  }
}
