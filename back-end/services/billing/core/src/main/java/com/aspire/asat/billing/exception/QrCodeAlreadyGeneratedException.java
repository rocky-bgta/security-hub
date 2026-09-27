package com.aspire.asat.billing.exception;

import org.springframework.http.HttpStatus;

public class QrCodeAlreadyGeneratedException extends BillingServiceException {

  public QrCodeAlreadyGeneratedException(String message) {
    super(message, HttpStatus.CONFLICT); // 409
  }
}
