package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Account-level OTP verification lock. Survives resend and re-login until {@code lockedUntil}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "mfa_otp_locks")
public class MfaOtpLock {

    @Id
    private UUID userId;

    private Instant lockedUntil;

    private Instant updatedAt;
}
