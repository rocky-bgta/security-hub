package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.entity.MfaOtpLifecycle;
import com.aspire.asat.auth.entity.MfaOtpLock;
import com.aspire.asat.auth.entity.MfaOtpRateCounter;
import com.aspire.asat.auth.repository.MfaOtpLifecycleRepository;
import com.aspire.asat.auth.repository.MfaOtpLockRepository;
import com.aspire.asat.auth.repository.MfaOtpRateCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisOtpLifecycleStoreTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private MfaOtpLifecycleRepository lifecycleRepository;
    @Mock
    private MfaOtpLockRepository lockRepository;
    @Mock
    private MfaOtpRateCounterRepository rateCounterRepository;

    private RedisOtpLifecycleStore store;
    private UUID userId;

    @BeforeEach
    void setUp() {
        store = new RedisOtpLifecycleStore(
                redisTemplate, lifecycleRepository, lockRepository, rateCounterRepository);
        userId = UUID.randomUUID();
        org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void loadLifecycle_PrefersRedis() {
        MfaOtpLifecycle lifecycle = MfaOtpLifecycle.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .method("EMAIL")
                .resendCount(2)
                .build();
        when(hashOperations.entries(RedisOtpLifecycleStore.lifecycleKey(userId, "EMAIL")))
                .thenReturn(Map.copyOf(RedisOtpLifecycleStore.toHash(lifecycle)));

        Optional<MfaOtpLifecycle> loaded = store.loadLifecycle(userId, "EMAIL");

        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().getResendCount());
        verify(lifecycleRepository, never()).findByUserIdAndMethod(any(), any());
    }

    @Test
    void loadLifecycle_FallsBackToMongoWhenRedisFails() {
        when(redisTemplate.opsForHash()).thenThrow(new RuntimeException("redis down"));
        MfaOtpLifecycle lifecycle = MfaOtpLifecycle.builder().userId(userId).method("EMAIL").build();
        when(lifecycleRepository.findByUserIdAndMethod(userId, "EMAIL")).thenReturn(Optional.of(lifecycle));

        Optional<MfaOtpLifecycle> loaded = store.loadLifecycle(userId, "EMAIL");

        assertTrue(loaded.isPresent());
        assertEquals(userId, loaded.get().getUserId());
    }

    @Test
    void saveLock_WritesRedisAndMongo() {
        Instant until = Instant.now().plusSeconds(900);

        store.saveLock(userId, until, Duration.ofSeconds(900));

        verify(valueOperations).set(eq(RedisOtpLifecycleStore.lockKey(userId)), eq(String.valueOf(until.toEpochMilli())),
                eq(900L), eq(TimeUnit.SECONDS));
        ArgumentCaptor<MfaOtpLock> captor = ArgumentCaptor.forClass(MfaOtpLock.class);
        verify(lockRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(until, captor.getValue().getLockedUntil());
    }

    @Test
    void loadLock_UsesMongoFallback() {
        when(valueOperations.get(anyString())).thenReturn(null);
        Instant until = Instant.now().plusSeconds(60);
        when(lockRepository.findById(userId)).thenReturn(Optional.of(
                MfaOtpLock.builder().userId(userId).lockedUntil(until).build()));

        Optional<Instant> loaded = store.loadLock(userId);

        assertTrue(loaded.isPresent());
        assertEquals(until, loaded.get());
    }

    @Test
    void increment_SetsExpireOnFirstHit() {
        when(valueOperations.increment("mfa:otp:resend:ip:1.1.1.1")).thenReturn(1L);

        long count = store.increment("mfa:otp:resend:ip:1.1.1.1", Duration.ofHours(1));

        assertEquals(1L, count);
        verify(redisTemplate).expire(eq("mfa:otp:resend:ip:1.1.1.1"), eq(Duration.ofHours(1)));
    }

    @Test
    void increment_FallsBackToMongoWhenRedisFails() {
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("redis down"));
        when(rateCounterRepository.findById("counter-1")).thenReturn(Optional.empty());
        when(rateCounterRepository.save(any(MfaOtpRateCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        long count = store.increment("counter-1", Duration.ofHours(1));

        assertEquals(1L, count);
        verify(rateCounterRepository).save(any(MfaOtpRateCounter.class));
    }

    @Test
    void increment_MongoIncrementsExistingUnexpiredCounter() {
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("redis down"));
        MfaOtpRateCounter existing = MfaOtpRateCounter.builder()
                .id("counter-1")
                .count(4L)
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        when(rateCounterRepository.findById("counter-1")).thenReturn(Optional.of(existing));
        when(rateCounterRepository.save(any(MfaOtpRateCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        long count = store.increment("counter-1", Duration.ofHours(1));

        assertEquals(5L, count);
        assertEquals(5L, existing.getCount());
    }

    @Test
    void saveLifecycle_WritesRedisAndMongo() {
        MfaOtpLifecycle lifecycle = MfaOtpLifecycle.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .method("SMS")
                .resendCount(1)
                .build();
        when(lifecycleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        store.saveLifecycle(lifecycle);

        verify(hashOperations).putAll(eq(RedisOtpLifecycleStore.lifecycleKey(userId, "SMS")), anyMap());
        verify(redisTemplate).expire(eq(RedisOtpLifecycleStore.lifecycleKey(userId, "SMS")), eq(Duration.ofHours(2)));
        verify(lifecycleRepository).save(lifecycle);
    }

    @Test
    void clearLock_DeletesRedisAndMongo() {
        store.clearLock(userId);
        verify(redisTemplate).delete(RedisOtpLifecycleStore.lockKey(userId));
        verify(lockRepository).deleteById(userId);
    }

    @Test
    void loadLock_PrefersUnexpiredRedisValue() {
        Instant until = Instant.now().plusSeconds(120);
        when(valueOperations.get(RedisOtpLifecycleStore.lockKey(userId)))
                .thenReturn(String.valueOf(until.toEpochMilli()));

        Optional<Instant> loaded = store.loadLock(userId);

        assertTrue(loaded.isPresent());
        assertEquals(until.toEpochMilli(), loaded.get().toEpochMilli());
        verify(lockRepository, never()).findById(any());
    }
}
