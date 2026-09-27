package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.MfaOtpLifecycle;
import com.aspire.asat.auth.exception.OtpCooldownException;
import com.aspire.asat.auth.exception.OtpLockedException;
import com.aspire.asat.auth.exception.OtpRateLimitException;
import com.aspire.asat.auth.model.mfa.OtpIssueDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpSecurityServiceTest {

    private InMemoryOtpLifecycleStore store;
    private MfaConfig mfaConfig;
    private OtpSecurityService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        store = new InMemoryOtpLifecycleStore();
        mfaConfig = new MfaConfig();
        mfaConfig.setMaxAttempts(5);
        mfaConfig.setMaxResendsPerHour(5);
        mfaConfig.setLockDurationSeconds(900);
        mfaConfig.setRateLimitPerHour(10);
        mfaConfig.setIpVerifyLimit(30);
        mfaConfig.setResendCooldownSeconds(new ArrayList<>(List.of(60, 120, 300)));
        service = new OtpSecurityService(mfaConfig, store);
        userId = UUID.randomUUID();
    }

    @Test
    void authorizeIssue_InitialGenerate_DoesNotCountAsResend_Sets60sCooldown() {
        OtpIssueDecision decision = service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false);

        assertFalse(decision.isCountedAsResend());
        assertEquals(0, decision.getResendCount());
        assertEquals(5, decision.getResendsRemaining());
        assertEquals(60, decision.getResendCooldownSeconds());
        assertEquals(5, decision.getAttemptsRemaining());
    }

    @Test
    void authorizeIssue_SecondGenerate_AppliesResendPolicy() {
        service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false);
        MfaOtpLifecycle lifecycle = store.loadLifecycle(userId, "EMAIL").orElseThrow();
        lifecycle.setNextResendAt(Instant.now().minusSeconds(1));
        store.saveLifecycle(lifecycle);

        OtpIssueDecision decision = service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false);

        assertTrue(decision.isCountedAsResend());
        assertEquals(1, decision.getResendCount());
        assertEquals(4, decision.getResendsRemaining());
        assertEquals(120, decision.getResendCooldownSeconds());
    }

    @Test
    void progressiveCooldown_AfterSecondResend_IsFiveMinutes() {
        seedIssued(0, Instant.now().minusSeconds(1));
        service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", true);
        seedNextResendPast();
        service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", true);
        seedNextResendPast();

        OtpIssueDecision third = service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", true);

        assertEquals(3, third.getResendCount());
        assertEquals(300, third.getResendCooldownSeconds());
        assertEquals(2, third.getResendsRemaining());
    }

    @Test
    void resendDuringCooldown_ThrowsOtpCooldownException() {
        service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false);

        OtpCooldownException ex = assertThrows(OtpCooldownException.class,
                () -> service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", true));
        assertTrue(ex.getRetryAfterSeconds() > 0);
        assertEquals("OTP_COOLDOWN", ex.getErrorCode());
    }

    @Test
    void sixthResendInHour_ThrowsRateLimit_InitialNotCounted() {
        seedIssued(5, Instant.now().minusSeconds(1));

        OtpRateLimitException ex = assertThrows(OtpRateLimitException.class,
                () -> service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", true));
        assertEquals("OTP_RATE_LIMITED", ex.getErrorCode());
    }

    @Test
    void sessionResendCap_EnforcedIndependently() {
        seedIssued(0, Instant.now().minusSeconds(1));
        for (int i = 0; i < 5; i++) {
            store.increment("mfa:otp:resend:session:sess-1", Duration.ofHours(1));
        }

        assertThrows(OtpRateLimitException.class,
                () -> service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "sess-1", true));
    }

    @Test
    void ipSendCap_BlocksAfterRateLimitPerHour() {
        mfaConfig.setRateLimitPerHour(2);
        service.authorizeIssue(userId, MfaMethod.EMAIL, "9.9.9.9", "session-a", false);
        seedNextResendPast();
        service.authorizeIssue(userId, MfaMethod.EMAIL, "9.9.9.9", "session-a", true);

        assertThrows(OtpRateLimitException.class,
                () -> service.authorizeIssue(userId, MfaMethod.EMAIL, "9.9.9.9", "session-a", true));
    }

    @Test
    void lockUser_BlocksGenerateAndVerify() {
        service.lockUser(userId);

        assertThrows(OtpLockedException.class,
                () -> service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false));
        assertThrows(OtpLockedException.class, () -> service.assertVerifyAllowed(userId, "1.1.1.1"));
        assertTrue(service.remainingLockSeconds(userId).orElse(0) > 0);
    }

    @Test
    void recordSuccessfulVerify_ClearsLockAndResendState() {
        service.lockUser(userId);
        seedIssued(3, Instant.now().plusSeconds(60));

        service.recordSuccessfulVerify(userId, MfaMethod.EMAIL);

        assertTrue(service.remainingLockSeconds(userId).isEmpty());
        MfaOtpLifecycle lifecycle = store.loadLifecycle(userId, "EMAIL").orElseThrow();
        assertFalse(Boolean.TRUE.equals(lifecycle.getInitialIssued()));
        assertEquals(0, lifecycle.getResendCount());
    }

    @Test
    void assertVerifyAllowed_IpLimitExceeded_ThrowsRateLimit() {
        mfaConfig.setIpVerifyLimit(2);
        service.assertVerifyAllowed(userId, "8.8.8.8");
        service.assertVerifyAllowed(userId, "8.8.8.8");

        assertThrows(OtpRateLimitException.class, () -> service.assertVerifyAllowed(userId, "8.8.8.8"));
    }

    @Test
    void assertNotLocked_WhenNoLock_DoesNotThrow() {
        service.assertNotLocked(userId);
        service.assertVerifyAllowed(userId, " ");
        assertTrue(service.remainingLockSeconds(userId).isEmpty());
    }

    @Test
    void sessionKey_WithoutTempToken_UsesUserId() {
        String first = OtpSecurityService.sessionKey(null, userId);
        String second = OtpSecurityService.sessionKey("  ", userId);
        assertEquals(first, second);
    }

    @Test
    void sessionKey_HashesTempTokenConsistently() {
        String first = OtpSecurityService.sessionKey("temp-token", userId);
        String second = OtpSecurityService.sessionKey("temp-token", userId);
        assertEquals(first, second);
        assertEquals(64, first.length());
    }

    @Test
    void hourWindowReset_AllowsNewInitialGenerate() {
        MfaOtpLifecycle lifecycle = MfaOtpLifecycle.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .method("EMAIL")
                .initialIssued(true)
                .resendCount(5)
                .windowStartedAt(Instant.now().minus(Duration.ofHours(1)).minusSeconds(1))
                .nextResendAt(Instant.now().plusSeconds(300))
                .build();
        store.saveLifecycle(lifecycle);

        OtpIssueDecision decision = service.authorizeIssue(userId, MfaMethod.EMAIL, "1.1.1.1", "session-a", false);

        assertFalse(decision.isCountedAsResend());
        assertEquals(0, decision.getResendCount());
        assertEquals(5, decision.getResendsRemaining());
    }

    private void seedIssued(int resendCount, Instant nextResendAt) {
        store.saveLifecycle(MfaOtpLifecycle.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .method("EMAIL")
                .initialIssued(true)
                .resendCount(resendCount)
                .windowStartedAt(Instant.now())
                .nextResendAt(nextResendAt)
                .build());
    }

    private void seedNextResendPast() {
        MfaOtpLifecycle lifecycle = store.loadLifecycle(userId, "EMAIL").orElseThrow();
        lifecycle.setNextResendAt(Instant.now().minusSeconds(1));
        store.saveLifecycle(lifecycle);
    }

    private static final class InMemoryOtpLifecycleStore implements OtpLifecycleStore {
        private final Map<String, MfaOtpLifecycle> lifecycles = new HashMap<>();
        private final Map<UUID, Instant> locks = new HashMap<>();
        private final Map<String, Long> counters = new HashMap<>();

        @Override
        public Optional<MfaOtpLifecycle> loadLifecycle(UUID userId, String method) {
            return Optional.ofNullable(lifecycles.get(userId + ":" + method));
        }

        @Override
        public void saveLifecycle(MfaOtpLifecycle lifecycle) {
            lifecycles.put(lifecycle.getUserId() + ":" + lifecycle.getMethod(), lifecycle);
        }

        @Override
        public Optional<Instant> loadLock(UUID userId) {
            Instant until = locks.get(userId);
            if (until != null && until.isAfter(Instant.now())) {
                return Optional.of(until);
            }
            return Optional.empty();
        }

        @Override
        public void saveLock(UUID userId, Instant lockedUntil, Duration ttl) {
            locks.put(userId, lockedUntil);
        }

        @Override
        public void clearLock(UUID userId) {
            locks.remove(userId);
        }

        @Override
        public long increment(String counterKey, Duration ttl) {
            long next = counters.getOrDefault(counterKey, 0L) + 1L;
            counters.put(counterKey, next);
            return next;
        }
    }
}
