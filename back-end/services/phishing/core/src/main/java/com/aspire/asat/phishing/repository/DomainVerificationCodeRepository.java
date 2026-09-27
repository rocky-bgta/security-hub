package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.DomainVerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for DomainVerificationCode entity operations.
 */
@Repository
public interface DomainVerificationCodeRepository extends MongoRepository<DomainVerificationCode, String> {
    
    /**
     * Find valid (not used, not expired) verification code for a domain and email
     */
    Optional<DomainVerificationCode> findByDomainAndEmailAddressAndIsUsedFalseAndExpiresAtAfter(
            String domain, String emailAddress, Instant now);
    
    /**
     * Count verification requests for a domain within a time window
     * Used for rate limiting (max 5 per hour - BR-07)
     */
    long countByDomainAndCreatedAtAfter(String domain, Instant after);
    
    /**
     * Find all codes for a domain (for cleanup)
     */
    void deleteByDomainAndExpiresAtBefore(String domain, Instant expiryTime);
    
    /**
     * Find the latest code for a domain
     */
    Optional<DomainVerificationCode> findFirstByDomainAndClientIdOrderByCreatedAtDesc(
            String domain, String clientId);
}
