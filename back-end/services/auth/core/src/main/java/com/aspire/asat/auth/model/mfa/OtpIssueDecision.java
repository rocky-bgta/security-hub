package com.aspire.asat.auth.model.mfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of authorizing an OTP generate or resend against lock, cooldown, and rate limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpIssueDecision {

    private int resendCooldownSeconds;
    private int resendsRemaining;
    private int attemptsRemaining;
    private boolean countedAsResend;
    private int resendCount;
}
