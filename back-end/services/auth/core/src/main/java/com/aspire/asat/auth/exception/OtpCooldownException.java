package com.aspire.asat.auth.exception;

import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.OtpErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an OTP resend is requested before the cooldown elapses.
 */
public class OtpCooldownException extends MfaException {

    public OtpCooldownException(Integer retryAfterSeconds) {
        super(OtpMessages.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, OtpErrorCode.COOLDOWN, retryAfterSeconds);
    }
}
