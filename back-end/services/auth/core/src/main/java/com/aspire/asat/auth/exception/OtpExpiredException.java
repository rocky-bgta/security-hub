package com.aspire.asat.auth.exception;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.OtpErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when OTP has expired
 */
public class OtpExpiredException extends MfaException {

    public OtpExpiredException() {
        this(OtpMessages.EXPIRED);
    }

    public OtpExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST, OtpErrorCode.EXPIRED, null);
    }
}
