package com.aspire.asat.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * MFA configuration properties loaded from application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mfa")
public class MfaConfig {

    /**
     * Global flag to enable/disable MFA
     * If false, MFA is skipped entirely after username/password login
     */
    private Boolean enabled = false;

    /**
     * Enable SMS-based MFA
     */
    private Boolean smsEnabled = false;

    /**
     * Enable Email-based MFA
     */
    private Boolean emailEnabled = false;

    /**
     * Enable Authenticator app-based MFA (TOTP)
     */
    private Boolean authenticatorEnabled = false;

    /**
     * Enable Phone Call-based MFA
     */
    private Boolean phoneCallEnabled = false;

    /**
     * OTP code length (default: 6 digits)
     */
    private Integer otpLength = 6;

    /**
     * OTP validity period in seconds (default: 300 = 5 minutes)
     */
    private Integer otpValiditySeconds = 300;

    /**
     * Maximum number of failed verification attempts before locking (default: 5)
     */
    private Integer maxAttempts = 5;

    /**
     * Rate limit: Maximum OTP generate/resend requests per IP per hour (NAT-friendly ceiling)
     */
    private Integer rateLimitPerHour = 10;

    /**
     * Account/session lock duration after max failed verification attempts (default: 900 = 15 minutes)
     */
    private Integer lockDurationSeconds = 900;

    /**
     * Maximum additional OTP resend requests per user per hour. The initial generate does not count.
     */
    private Integer maxResendsPerHour = 5;

    /**
     * Progressive resend cooldowns in seconds: after initial, after 1st resend, after 2nd+ resend.
     */
    private List<Integer> resendCooldownSeconds = new ArrayList<>(List.of(60, 120, 300));

    /**
     * MFA temp token TTL in minutes. Must cover progressive resend cooldowns (default: 30).
     */
    private Integer tempTokenExpirationMinutes = 30;

    /**
     * Maximum OTP verification attempts per IP within one lock-duration window
     */
    private Integer ipVerifyLimit = 30;

    /**
     * Encryption key for encrypting TOTP secrets at rest
     * Should be set via environment variable in production
     */
    private String encryptionKey;

    /**
     * Cooldown to apply after {@code completedResends} resend requests (0 = after initial generate).
     */
    public int cooldownSecondsForCompletedResends(int completedResends) {
        if (resendCooldownSeconds == null || resendCooldownSeconds.isEmpty()) {
            return 60;
        }
        int index = Math.max(completedResends, 0);
        if (index >= resendCooldownSeconds.size()) {
            return resendCooldownSeconds.get(resendCooldownSeconds.size() - 1);
        }
        return resendCooldownSeconds.get(index);
    }

}

