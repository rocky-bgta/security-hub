package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class PackageExpiredException extends ServiceException {
  public PackageExpiredException(String message) {
    super(message, HttpStatus.GONE); // 410
  }
}
