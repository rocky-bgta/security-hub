package com.aspire.asat.auth.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends ServiceException {
  public CourseNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
