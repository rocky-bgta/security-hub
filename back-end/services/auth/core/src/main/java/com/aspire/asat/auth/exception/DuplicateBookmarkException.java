package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class DuplicateBookmarkException extends ServiceException {
  public DuplicateBookmarkException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
