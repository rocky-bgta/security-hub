package com.aspire.asat.auth.exception;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.OtpErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when OTP is invalid
 */
public class OtpInvalidException extends MfaException {

    public OtpInvalidException() {
        this(OtpMessages.INVALID);
    }

    public OtpInvalidException(String message) {
        super(message, HttpStatus.BAD_REQUEST, OtpErrorCode.INVALID, null);
    }
}
