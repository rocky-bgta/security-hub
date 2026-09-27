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
 * Entity for storing MFA OTP codes (hashed)
 * Never stores plain OTP codes - always hashed using BCrypt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "mfa_codes")
public class MfaCode {

    @Id
    private UUID id;

    private UUID userId; // User ID who requested the OTP
    private String codeHash; // BCrypt hashed OTP code - NEVER store plain code
    private Instant expiresAt; // When the OTP expires
    private Boolean isUsed; // Whether this OTP has been used
    private UUID sessionId; // Session ID for tracking
    private String generatedBy; // SMS or EMAIL
    private Integer attemptCount; // Number of verification attempts
    private Instant createdAt; // When the OTP was generated
    /** Phone used to send this OTP; copied onto UserMfaMethod on successful SMS/PHONE_CALL verify. */
    private String phoneNumber;

    /** ACTIVE, USED, INVALIDATED, EXPIRED */
    private String status;

    private Instant invalidatedAt;

}

