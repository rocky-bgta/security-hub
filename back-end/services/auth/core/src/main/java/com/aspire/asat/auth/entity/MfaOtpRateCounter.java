package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Mongo fallback for Redis rate-limit counters (IP / session).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "mfa_otp_rate_counters")
public class MfaOtpRateCounter {

    @Id
    private String id;

    private Long count;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
