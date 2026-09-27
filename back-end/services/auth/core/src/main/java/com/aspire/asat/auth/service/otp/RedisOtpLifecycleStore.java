package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.entity.MfaOtpLifecycle;
import com.aspire.asat.auth.entity.MfaOtpLock;
import com.aspire.asat.auth.entity.MfaOtpRateCounter;
import com.aspire.asat.auth.repository.MfaOtpLifecycleRepository;
import com.aspire.asat.auth.repository.MfaOtpLockRepository;
import com.aspire.asat.auth.repository.MfaOtpRateCounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-primary OTP lifecycle store with MongoDB fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOtpLifecycleStore implements OtpLifecycleStore {

    static final String LIFECYCLE_KEY_PREFIX = "mfa:otp:lifecycle:user:";
    static final String LOCK_KEY_PREFIX = "mfa:otp:lock:user:";

    private static final Duration LIFECYCLE_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;
    private final MfaOtpLifecycleRepository lifecycleRepository;
    private final MfaOtpLockRepository lockRepository;
    private final MfaOtpRateCounterRepository rateCounterRepository;

    @Override
    public Optional<MfaOtpLifecycle> loadLifecycle(UUID userId, String method) {
        String key = lifecycleKey(userId, method);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            Optional<MfaOtpLifecycle> fromRedis = fromHash(entries);
            if (fromRedis.isPresent()) {
                return fromRedis;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP lifecycle read, using MongoDB fallback: {}", e.getMessage());
        }
        return lifecycleRepository.findByUserIdAndMethod(userId, method);
    }

    @Override
    public void saveLifecycle(MfaOtpLifecycle lifecycle) {
        lifecycle.setUpdatedAt(Instant.now());
        try {
            String key = lifecycleKey(lifecycle.getUserId(), lifecycle.getMethod());
            redisTemplate.opsForHash().putAll(key, toHash(lifecycle));
            redisTemplate.expire(key, LIFECYCLE_TTL);
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP lifecycle write, MongoDB fallback will be used: {}", e.getMessage());
        }
        try {
            lifecycleRepository.save(lifecycle);
        } catch (Exception e) {
            log.error("Failed to persist OTP lifecycle to MongoDB for user {}: {}", lifecycle.getUserId(), e.getMessage());
        }
    }

    @Override
    public Optional<Instant> loadLock(UUID userId) {
        Instant now = Instant.now();
        try {
            String value = redisTemplate.opsForValue().get(lockKey(userId));
            if (value != null) {
                Instant until = Instant.ofEpochMilli(Long.parseLong(value));
                if (until.isAfter(now)) {
                    return Optional.of(until);
                }
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP lock read, using MongoDB fallback: {}", e.getMessage());
        }
        return lockRepository.findById(userId)
                .map(MfaOtpLock::getLockedUntil)
                .filter(until -> until != null && until.isAfter(now));
    }

    @Override
    public void saveLock(UUID userId, Instant lockedUntil, Duration ttl) {
        Instant now = Instant.now();
        try {
            redisTemplate.opsForValue().set(lockKey(userId), String.valueOf(lockedUntil.toEpochMilli()),
                    Math.max(ttl.toSeconds(), 1), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP lock write, MongoDB fallback will be used: {}", e.getMessage());
        }
        try {
            lockRepository.save(MfaOtpLock.builder()
                    .userId(userId)
                    .lockedUntil(lockedUntil)
                    .updatedAt(now)
                    .build());
        } catch (Exception e) {
            log.error("Failed to persist OTP lock to MongoDB for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void clearLock(UUID userId) {
        try {
            redisTemplate.delete(lockKey(userId));
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP lock delete: {}", e.getMessage());
        }
        try {
            lockRepository.deleteById(userId);
        } catch (Exception e) {
            log.warn("Failed to delete OTP lock from MongoDB for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public long increment(String counterKey, Duration ttl) {
        try {
            Long count = redisTemplate.opsForValue().increment(counterKey);
            if (count != null && count == 1L) {
                redisTemplate.expire(counterKey, ttl);
            }
            if (count != null) {
                return count;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for OTP rate counter {}, using MongoDB fallback: {}", counterKey, e.getMessage());
        }
        return incrementMongo(counterKey, ttl);
    }

    private long incrementMongo(String counterKey, Duration ttl) {
        Instant now = Instant.now();
        MfaOtpRateCounter counter = rateCounterRepository.findById(counterKey).orElse(null);
        if (counter == null || counter.getExpiresAt() == null || !counter.getExpiresAt().isAfter(now)) {
            counter = MfaOtpRateCounter.builder()
                    .id(counterKey)
                    .count(1L)
                    .expiresAt(now.plus(ttl))
                    .build();
            rateCounterRepository.save(counter);
            return 1L;
        }
        long next = (counter.getCount() == null ? 0L : counter.getCount()) + 1L;
        counter.setCount(next);
        rateCounterRepository.save(counter);
        return next;
    }

    static Map<String, String> toHash(MfaOtpLifecycle lifecycle) {
        Map<String, String> map = new HashMap<>();
        put(map, "id", lifecycle.getId() == null ? null : lifecycle.getId().toString());
        put(map, "userId", lifecycle.getUserId() == null ? null : lifecycle.getUserId().toString());
        put(map, "method", lifecycle.getMethod());
        put(map, "initialIssued", String.valueOf(Boolean.TRUE.equals(lifecycle.getInitialIssued())));
        put(map, "resendCount", String.valueOf(lifecycle.getResendCount() == null ? 0 : lifecycle.getResendCount()));
        put(map, "windowStartedAt", epoch(lifecycle.getWindowStartedAt()));
        put(map, "lastSentAt", epoch(lifecycle.getLastSentAt()));
        put(map, "nextResendAt", epoch(lifecycle.getNextResendAt()));
        put(map, "activeSessionId", lifecycle.getActiveSessionId() == null ? null : lifecycle.getActiveSessionId().toString());
        put(map, "updatedAt", epoch(lifecycle.getUpdatedAt()));
        return map;
    }

    static Optional<MfaOtpLifecycle> fromHash(Map<Object, Object> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        String userId = str(entries.get("userId"));
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }
        return Optional.of(MfaOtpLifecycle.builder()
                .id(uuid(str(entries.get("id"))))
                .userId(UUID.fromString(userId))
                .method(str(entries.get("method")))
                .initialIssued(Boolean.parseBoolean(str(entries.get("initialIssued"))))
                .resendCount(parseInt(str(entries.get("resendCount"))))
                .windowStartedAt(instant(str(entries.get("windowStartedAt"))))
                .lastSentAt(instant(str(entries.get("lastSentAt"))))
                .nextResendAt(instant(str(entries.get("nextResendAt"))))
                .activeSessionId(uuid(str(entries.get("activeSessionId"))))
                .updatedAt(instant(str(entries.get("updatedAt"))))
                .build());
    }

    static String lifecycleKey(UUID userId, String method) {
        return LIFECYCLE_KEY_PREFIX + userId + ":" + method;
    }

    static String lockKey(UUID userId) {
        return LOCK_KEY_PREFIX + userId;
    }

    private static void put(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static String epoch(Instant instant) {
        return instant == null ? null : String.valueOf(instant.toEpochMilli());
    }

    private static Instant instant(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(value));
    }

    private static UUID uuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return UUID.fromString(value);
    }

    private static Integer parseInt(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }
}
