package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.MfaCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaCodeRepository extends MongoRepository<MfaCode, UUID> {

    /**
     * Find active OTP code by user ID and session ID
     * Active means: not used, not expired
     */
    Optional<MfaCode> findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
            UUID userId, UUID sessionId, Instant now);

    /**
     * Find unused OTP code by user ID and session ID (including expired codes)
     */
    Optional<MfaCode> findByUserIdAndSessionIdAndIsUsedFalse(UUID userId, UUID sessionId);

    /**
     * Find all OTP codes for a user (for cleanup/audit)
     */
    List<MfaCode> findByUserId(UUID userId);

    /**
     * Find OTP codes by user ID and generation method
     */
    List<MfaCode> findByUserIdAndGeneratedBy(UUID userId, String generatedBy);

    /**
     * Find all unused (not expired) OTP codes for a user by generation method
     * Used for invalidating old OTPs when resending
     */
    List<MfaCode> findByUserIdAndGeneratedByAndIsUsedFalseAndExpiresAtAfter(
            UUID userId, String generatedBy, Instant now);

    /**
     * Find all unused OTP codes for a user by generation method, including expired ones.
     */
    List<MfaCode> findByUserIdAndGeneratedByAndIsUsedFalse(UUID userId, String generatedBy);

    /**
     * Delete expired OTP codes (for cleanup)
     */
    void deleteByExpiresAtBefore(Instant now);

}

