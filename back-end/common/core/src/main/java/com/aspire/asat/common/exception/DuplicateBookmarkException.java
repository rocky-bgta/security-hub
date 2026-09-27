package com.aspire.asat.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateBookmarkException extends ServiceException {
  public DuplicateBookmarkException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
