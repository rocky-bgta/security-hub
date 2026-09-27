package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.util.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages temporary tokens used during the MFA verification flow.
 *
 * <p>Tokens are stored in Redis as the primary (fast) store and mirrored in
 * MongoDB via {@link SessionFallbackService}. If Redis is unavailable the
 * validation falls back to MongoDB so that MFA login can still complete.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryTokenService {

    private static final String TEMP_TOKEN_PREFIX = "temp_token:";
    private static final String TEMP_TOKEN_USER_PREFIX = "temp_token_user:";
    private static final int DEFAULT_TEMP_TOKEN_EXPIRATION_MINUTES = 30;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RedisTemplate<String, String> redisTemplate;
    private final MfaConfig mfaConfig;

    @Autowired(required = false)
    private SessionFallbackService sessionFallbackService;

    /**
     * Generates a short-lived JWT for the MFA verification step and stores it
     * in both Redis (best-effort) and MongoDB (durable fallback).
     */
    public String generateTemporaryToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("username", username);
        claims.put("type", "temp_mfa");

        int expirationMinutes = tempTokenExpirationMinutes();
        String tempToken = JWTUtils.generateToken(claims, userId.toString(),
                String.valueOf(expirationMinutes), jwtSecret);

        // Best-effort Redis write — failures are swallowed so MFA initiation never breaks.
        try {
            String tempTokenKey = TEMP_TOKEN_PREFIX + tempToken;
            String userKey = TEMP_TOKEN_USER_PREFIX + userId;
            redisTemplate.opsForValue().set(tempTokenKey, userId.toString(),
                    expirationMinutes, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(userKey, tempToken,
                    expirationMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis unavailable for MFA temp token storage, MongoDB fallback will be used: {}", e.getMessage());
        }

        // Durable MongoDB write — ensures MFA can proceed even when Redis is down.
        if (sessionFallbackService != null) {
            sessionFallbackService.saveTempToken(tempToken, userId, expirationMinutes);
        }

        log.info("Generated temporary token for user {} (expires in {} minutes)", userId, expirationMinutes);
        return tempToken;
    }

    private int tempTokenExpirationMinutes() {
        Integer configured = mfaConfig != null ? mfaConfig.getTempTokenExpirationMinutes() : null;
        if (configured == null || configured <= 0) {
            return DEFAULT_TEMP_TOKEN_EXPIRATION_MINUTES;
        }
        return configured;
    }

    /**
     * Validates the temporary token and returns the associated user ID.
     * Falls back to MongoDB when the Redis lookup returns nothing.
     *
     * @throws UnauthorizedResourceException if the token is absent, expired, or invalid
     */
    public UUID validateAndExtractUserId(String tempToken) {
        if (tempToken == null || tempToken.trim().isEmpty()) {
            throw new UnauthorizedResourceException("Temporary token is required");
        }

        try {
            if (JWTUtils.isTokenExpired(tempToken, jwtSecret)) {
                throw new UnauthorizedResourceException("Temporary token has expired");
            }

            String userIdStr = JWTUtils.extractClaimByKey(tempToken, jwtSecret, "userId", String.class);
            if (userIdStr == null) {
                throw new UnauthorizedResourceException("Invalid temporary token format");
            }

            String tokenType = JWTUtils.extractClaimByKey(tempToken, jwtSecret, "type", String.class);
            if (!"temp_mfa".equals(tokenType)) {
                throw new UnauthorizedResourceException("Invalid token type");
            }

            // Try Redis first.
            String storedUserId = null;
            try {
                String tempTokenKey = TEMP_TOKEN_PREFIX + tempToken;
                storedUserId = redisTemplate.opsForValue().get(tempTokenKey);
            } catch (Exception e) {
                log.warn("Redis unavailable for MFA temp token validation, trying MongoDB fallback: {}", e.getMessage());
            }

            if (storedUserId != null && storedUserId.equals(userIdStr)) {
                return UUID.fromString(userIdStr);
            }

            // Redis miss or outage — fall back to MongoDB.
            if (sessionFallbackService != null && sessionFallbackService.validateTempToken(tempToken, userIdStr)) {
                log.debug("MFA temp token validated via MongoDB fallback for userId={}", userIdStr);
                return UUID.fromString(userIdStr);
            }

            throw new UnauthorizedResourceException("Temporary token not found or invalid");

        } catch (IllegalArgumentException e) {
            log.error("Error parsing user ID from temporary token: {}", e.getMessage());
            throw new UnauthorizedResourceException(
                    "Your verification session has expired for security reasons. " +
                    "Please sign in again using your username and password to request a new verification code.");
        } catch (UnauthorizedResourceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating temporary token: {}", e.getMessage());
            throw new UnauthorizedResourceException(
                    "Your verification session has expired for security reasons. " +
                    "Please sign in again using your username and password to request a new verification code.");
        }
    }

    /**
     * Revokes the temporary token after a successful MFA verification.
     * Deletes from both Redis and MongoDB; errors are swallowed since the
     * token may already have expired.
     */
    public void revokeTemporaryToken(String tempToken) {
        try {
            UUID userId = validateAndExtractUserId(tempToken);

            try {
                String tempTokenKey = TEMP_TOKEN_PREFIX + tempToken;
                String userKey = TEMP_TOKEN_USER_PREFIX + userId;
                redisTemplate.delete(tempTokenKey);
                redisTemplate.delete(userKey);
            } catch (Exception e) {
                log.warn("Redis unavailable during MFA temp token revocation: {}", e.getMessage());
            }

            if (sessionFallbackService != null) {
                sessionFallbackService.deleteTempToken(tempToken);
            }

            log.info("Revoked temporary token for user {}", userId);
        } catch (Exception e) {
            log.warn("Error revoking temporary token (may already be expired): {}", e.getMessage());
        }
    }
}
