package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.MfaOtpLifecycle;
import com.aspire.asat.auth.exception.OtpCooldownException;
import com.aspire.asat.auth.exception.OtpLockedException;
import com.aspire.asat.auth.exception.OtpRateLimitException;
import com.aspire.asat.auth.model.mfa.OtpIssueDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Enforces OTP lockout, progressive resend cooldown, and user/IP/session rate limits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpSecurityService {

    static final String IP_RESEND_PREFIX = "mfa:otp:resend:ip:";
    static final String IP_VERIFY_PREFIX = "mfa:otp:verify:ip:";
    static final String SESSION_RESEND_PREFIX = "mfa:otp:resend:session:";

    private static final Duration HOUR = Duration.ofHours(1);

    private final MfaConfig mfaConfig;
    private final OtpLifecycleStore store;

    public void assertNotLocked(UUID userId) {
        remainingLockSeconds(userId).ifPresent(seconds -> {
            throw new OtpLockedException(seconds);
        });
    }

    public void assertVerifyAllowed(UUID userId, String ip) {
        assertNotLocked(userId);
        if (StringUtils.hasText(ip)) {
            long count = store.increment(IP_VERIFY_PREFIX + ip, Duration.ofSeconds(lockDurationSeconds()));
            if (count > ipVerifyLimit()) {
                throw new OtpRateLimitException((int) Duration.ofSeconds(lockDurationSeconds()).toSeconds());
            }
        }
    }

    /**
     * Authorize an OTP send. The first send in the hour window is the initial generate
     * (does not count toward the 5/hour resend cap). Subsequent sends, including extra
     * generate-otp calls, follow resend cooldown and hourly caps.
     */
    public OtpIssueDecision authorizeIssue(UUID userId, MfaMethod method, String ip, String sessionKey,
                                           boolean forceResend) {
        Instant now = Instant.now();
        assertNotLocked(userId);
        enforceIpSendLimit(ip);

        MfaOtpLifecycle lifecycle = loadOrCreate(userId, method, now);
        resetWindowIfExpired(lifecycle, now);

        boolean treatAsResend = forceResend || Boolean.TRUE.equals(lifecycle.getInitialIssued());
        if (treatAsResend) {
            enforceCooldown(lifecycle, now);
            enforceUserResendCap(lifecycle);
            enforceSessionResendCap(sessionKey);
            int nextCount = nvl(lifecycle.getResendCount()) + 1;
            lifecycle.setResendCount(nextCount);
            lifecycle.setNextResendAt(now.plusSeconds(mfaConfig.cooldownSecondsForCompletedResends(nextCount)));
        } else {
            lifecycle.setInitialIssued(true);
            lifecycle.setResendCount(0);
            if (lifecycle.getWindowStartedAt() == null) {
                lifecycle.setWindowStartedAt(now);
            }
            lifecycle.setNextResendAt(now.plusSeconds(mfaConfig.cooldownSecondsForCompletedResends(0)));
        }

        lifecycle.setLastSentAt(now);
        store.saveLifecycle(lifecycle);
        return toDecision(lifecycle, now, treatAsResend);
    }

    public void recordActiveSession(UUID userId, MfaMethod method, UUID sessionId) {
        Instant now = Instant.now();
        MfaOtpLifecycle lifecycle = loadOrCreate(userId, method, now);
        lifecycle.setActiveSessionId(sessionId);
        store.saveLifecycle(lifecycle);
    }

    public void lockUser(UUID userId) {
        Instant until = Instant.now().plusSeconds(lockDurationSeconds());
        store.saveLock(userId, until, Duration.ofSeconds(lockDurationSeconds()));
        log.warn("OTP verification locked for user {} until {}", userId, until);
    }

    public void recordSuccessfulVerify(UUID userId, MfaMethod method) {
        store.clearLock(userId);
        Instant now = Instant.now();
        MfaOtpLifecycle lifecycle = loadOrCreate(userId, method, now);
        lifecycle.setInitialIssued(false);
        lifecycle.setResendCount(0);
        lifecycle.setNextResendAt(null);
        lifecycle.setActiveSessionId(null);
        lifecycle.setWindowStartedAt(now);
        lifecycle.setLastSentAt(null);
        store.saveLifecycle(lifecycle);
    }

    public Optional<Integer> remainingLockSeconds(UUID userId) {
        return store.loadLock(userId)
                .map(until -> secondsUntil(Instant.now(), until))
                .filter(seconds -> seconds > 0);
    }

    public static String sessionKey(String tempToken, UUID userId) {
        String raw = StringUtils.hasText(tempToken) ? tempToken.trim() : "user:" + userId;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    private void enforceIpSendLimit(String ip) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        long count = store.increment(IP_RESEND_PREFIX + ip, HOUR);
        if (count > nvl(mfaConfig.getRateLimitPerHour(), 10)) {
            throw new OtpRateLimitException((int) HOUR.toSeconds());
        }
    }

    private void enforceSessionResendCap(String sessionKey) {
        if (!StringUtils.hasText(sessionKey)) {
            return;
        }
        long count = store.increment(SESSION_RESEND_PREFIX + sessionKey, HOUR);
        if (count > nvl(mfaConfig.getMaxResendsPerHour(), 5)) {
            throw new OtpRateLimitException((int) HOUR.toSeconds());
        }
    }

    private void enforceUserResendCap(MfaOtpLifecycle lifecycle) {
        if (nvl(lifecycle.getResendCount()) >= nvl(mfaConfig.getMaxResendsPerHour(), 5)) {
            Instant windowEnd = lifecycle.getWindowStartedAt() == null
                    ? Instant.now().plus(HOUR)
                    : lifecycle.getWindowStartedAt().plus(HOUR);
            throw new OtpRateLimitException(secondsUntil(Instant.now(), windowEnd));
        }
    }

    private void enforceCooldown(MfaOtpLifecycle lifecycle, Instant now) {
        Instant next = lifecycle.getNextResendAt();
        if (next != null && next.isAfter(now)) {
            throw new OtpCooldownException(secondsUntil(now, next));
        }
    }

    private void resetWindowIfExpired(MfaOtpLifecycle lifecycle, Instant now) {
        Instant started = lifecycle.getWindowStartedAt();
        if (started == null || !started.plus(HOUR).isAfter(now)) {
            lifecycle.setWindowStartedAt(now);
            lifecycle.setResendCount(0);
            lifecycle.setInitialIssued(false);
            lifecycle.setNextResendAt(null);
        }
    }

    private MfaOtpLifecycle loadOrCreate(UUID userId, MfaMethod method, Instant now) {
        return store.loadLifecycle(userId, method.name())
                .orElseGet(() -> MfaOtpLifecycle.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .method(method.name())
                        .initialIssued(false)
                        .resendCount(0)
                        .windowStartedAt(now)
                        .build());
    }

    private OtpIssueDecision toDecision(MfaOtpLifecycle lifecycle, Instant now, boolean countedAsResend) {
        int remaining = Math.max(0, nvl(mfaConfig.getMaxResendsPerHour(), 5) - nvl(lifecycle.getResendCount()));
        return OtpIssueDecision.builder()
                .resendCooldownSeconds(secondsUntil(now, lifecycle.getNextResendAt()))
                .resendsRemaining(remaining)
                .attemptsRemaining(nvl(mfaConfig.getMaxAttempts(), 5))
                .countedAsResend(countedAsResend)
                .resendCount(nvl(lifecycle.getResendCount()))
                .build();
    }

    private int lockDurationSeconds() {
        return nvl(mfaConfig.getLockDurationSeconds(), 900);
    }

    private int ipVerifyLimit() {
        return nvl(mfaConfig.getIpVerifyLimit(), 30);
    }

    static int secondsUntil(Instant now, Instant until) {
        if (until == null || !until.isAfter(now)) {
            return 0;
        }
        long seconds = Duration.between(now, until).getSeconds();
        return (int) Math.max(1, seconds);
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static int nvl(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
