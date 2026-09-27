package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class PackageExpiredException extends CmsServiceException {
  public PackageExpiredException(String message) {
    super(message, HttpStatus.GONE); // 410
  }
}
