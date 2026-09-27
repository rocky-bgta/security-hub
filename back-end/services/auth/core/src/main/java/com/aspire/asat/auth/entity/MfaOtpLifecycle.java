package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Durable per-user+method OTP lifecycle state (cooldown, hourly resend count).
 * Redis is the primary store; this collection is the fallback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "mfa_otp_lifecycle")
@CompoundIndex(name = "otp_lifecycle_user_method_unique", def = "{'userId': 1, 'method': 1}", unique = true)
public class MfaOtpLifecycle {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    private String method;

    private Boolean initialIssued;

    private Integer resendCount;

    private Instant windowStartedAt;

    private Instant lastSentAt;

    private Instant nextResendAt;

    private UUID activeSessionId;

    private Instant updatedAt;
}
