package com.aspire.asat.cms.exception;

import com.aspire.asat.common.exception.AspireException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CmsServiceException extends AspireException {

    private final HttpStatus status;

    public CmsServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CmsServiceException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR; // Default to 500
    }

    public CmsServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}
