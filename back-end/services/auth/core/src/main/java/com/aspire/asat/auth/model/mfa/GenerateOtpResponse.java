package com.aspire.asat.auth.model.mfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for OTP generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpResponse {

    private UUID sessionId;
    private Integer expiresIn; // Expiration time in seconds
    /** Phone the OTP was sent to (SMS / PHONE_CALL only). */
    private String phoneNumber;
    /** Remaining seconds until Resend is enabled. 0 means the action is available. */
    private Integer resendCooldownSeconds;
    /** Remaining resend requests in the current hour (initial generate is not counted). */
    private Integer resendsRemaining;
    /** Verification attempts remaining for the newly issued OTP. */
    private Integer attemptsRemaining;

}

