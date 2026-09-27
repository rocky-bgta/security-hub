package com.aspire.asat.cms.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends CmsServiceException {
  public CourseNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
