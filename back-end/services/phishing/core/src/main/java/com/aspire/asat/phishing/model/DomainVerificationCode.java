package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity for storing one-time verification codes sent via email.
 * Codes expire after 15 minutes (as per BR-06).
 * Based on BRD Use Case 2.1.1.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "domain_verification_codes")
public class DomainVerificationCode {
    
    @Id
    private String id;
    
    /**
     * Multi-tenant identifier
     */
    @Indexed
    private String clientId;
    
    /**
     * Domain being verified (e.g., "aspiretech.com")
     */
    @Indexed
    private String domain;
    
    /**
     * Email address to which the code was sent
     */
    private String emailAddress;
    
    /**
     * Verification code (6-8 digits) - stored hashed
     */
    private String verificationCode;
    
    /**
     * Expiration time for the code (createdAt + 15 minutes)
     */
    @Indexed
    private Instant expiresAt;
    
    /**
     * Whether this code has already been used
     */
    private boolean isUsed;
    
    @CreatedDate
    private Instant createdAt;
}
