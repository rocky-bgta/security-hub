package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.entity.MfaOtpLifecycle;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for OTP lock, lifecycle, and rate-limit counters.
 * Redis is the primary store; MongoDB is the durable fallback.
 */
public interface OtpLifecycleStore {

    Optional<MfaOtpLifecycle> loadLifecycle(UUID userId, String method);

    void saveLifecycle(MfaOtpLifecycle lifecycle);

    Optional<Instant> loadLock(UUID userId);

    void saveLock(UUID userId, Instant lockedUntil, Duration ttl);

    void clearLock(UUID userId);

    /**
     * Atomically increment a rate-limit counter and apply TTL on first increment.
     *
     * @return the value after increment
     */
    long increment(String counterKey, Duration ttl);
}
