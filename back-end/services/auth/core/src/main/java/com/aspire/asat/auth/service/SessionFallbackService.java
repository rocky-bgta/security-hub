package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AuthSession;
import com.aspire.asat.auth.entity.AuthTempToken;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import com.aspire.asat.auth.repository.AuthSessionRepository;
import com.aspire.asat.auth.repository.AuthTempTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Durable session store backed by MongoDB.
 *
 * <p>When {@code auth.fallback.mongo.enabled} is {@code true} (the default):
 * <ul>
 *   <li>Every login/refresh writes session + permissions to {@code auth_sessions}.</li>
 *   <li>Every logout/revoke deletes from {@code auth_sessions}.</li>
 *   <li>MFA temp tokens are mirrored in {@code auth_temp_tokens}.</li>
 * </ul>
 *
 * <p>Redis remains the primary read path (fast path). This service is only
 * consulted by the Gateway when Redis is unavailable.
 *
 * <p>Setting {@code auth.fallback.mongo.enabled=false} turns this into a no-op,
 * restoring pre-fallback Redis-only behavior with zero code-path difference.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionFallbackService {

    private final AuthSessionRepository authSessionRepository;
    private final AuthTempTokenRepository authTempTokenRepository;

    @Value("${auth.fallback.mongo.enabled:true}")
    private boolean fallbackEnabled;

    // -------------------------------------------------------------------------
    // Session operations
    // -------------------------------------------------------------------------

    /**
     * Persists a new session document to MongoDB.
     *
     * @param redisAccessToken the session payload (same object stored in Redis)
     * @param permissions      flattened permission strings for this session
     * @param jwtExpirationMinutes  JWT / Redis TTL in minutes (used to set expiresAt)
     */
    public void saveSession(RedisAccessToken redisAccessToken, List<String> permissions, long jwtExpirationMinutes) {
        if (!fallbackEnabled) {
            return;
        }
        try {
            AuthSession session = new AuthSession()
                    .setTokenId(redisAccessToken.getTokenId())
                    .setUserId(redisAccessToken.getUserId())
                    .setClientAdminId(redisAccessToken.getClientAdminId())
                    .setMspId(redisAccessToken.getMspId())
                    .setCountryId(redisAccessToken.getCountryId())
                    .setEmail(redisAccessToken.getEmail())
                    .setPhoneNumber(redisAccessToken.getPhoneNumber())
                    .setAccessTokenChecksum(redisAccessToken.getAccessToken())
                    .setRefreshTokenChecksum(redisAccessToken.getRefreshToken())
                    .setUserType(redisAccessToken.getUserType())
                    .setUsername(redisAccessToken.getUsername())
                    .setUserStatus(redisAccessToken.getUserStatus())
                    .setFullName(redisAccessToken.getFullName())
                    .setClientAdminEmail(redisAccessToken.getClientAdminEmail())
                    .setClientAdminFullName(redisAccessToken.getClientAdminFullName())
                    .setCoRelationId(redisAccessToken.getCoRelationId())
                    .setScope(redisAccessToken.getScope())
                    .setRoles(redisAccessToken.getRoles())
                    .setPermissions(permissions)
                    .setExpiresAt(Instant.now().plusSeconds(jwtExpirationMinutes * 60));

            authSessionRepository.save(session);
            log.debug("Session saved to MongoDB fallback store: tokenId={}", redisAccessToken.getTokenId());
        } catch (Exception e) {
            log.error("Failed to save session to MongoDB fallback store: tokenId={}, error={}",
                    redisAccessToken.getTokenId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes all sessions for the given user (called on new login and during revocation).
     */
    public void revokeAllSessionsForUser(String userId) {
        if (!fallbackEnabled) {
            return;
        }
        try {
            authSessionRepository.deleteAllByUserId(userId);
            log.debug("All MongoDB sessions revoked for userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to revoke MongoDB sessions for userId={}: {}", userId, e.getMessage());
        }
    }

    /**
     * Deletes a single session by tokenId (called on logout).
     */
    public void revokeSessionByTokenId(String tokenId) {
        if (!fallbackEnabled) {
            return;
        }
        try {
            authSessionRepository.deleteById(tokenId);
            log.debug("MongoDB session revoked: tokenId={}", tokenId);
        } catch (Exception e) {
            log.warn("Failed to revoke MongoDB session for tokenId={}: {}", tokenId, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // MFA temp token operations
    // -------------------------------------------------------------------------

    /**
     * Persists an MFA temporary token to MongoDB.
     *
     * @param token               full temp-token JWT string
     * @param userId              owning user's UUID
     * @param expirationMinutes   how long the token is valid (matches Redis TTL)
     */
    public void saveTempToken(String token, UUID userId, int expirationMinutes) {
        if (!fallbackEnabled) {
            return;
        }
        try {
            AuthTempToken tempToken = new AuthTempToken()
                    .setToken(token)
                    .setUserId(userId.toString())
                    .setExpiresAt(Instant.now().plusSeconds((long) expirationMinutes * 60));

            authTempTokenRepository.save(tempToken);
            log.debug("MFA temp token saved to MongoDB fallback store for userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to save MFA temp token to MongoDB fallback store for userId={}: {}",
                    userId, e.getMessage());
        }
    }

    /**
     * Checks whether the given temp token exists and is valid for {@code expectedUserId}.
     * Validates expiry manually to guard against the MongoDB TTL cleanup window.
     *
     * @return {@code true} if the token is present, unexpired, and belongs to expectedUserId
     */
    public boolean validateTempToken(String token, String expectedUserId) {
        if (!fallbackEnabled) {
            return false;
        }
        try {
            return authTempTokenRepository.findById(token)
                    .filter(t -> expectedUserId.equals(t.getUserId()))
                    .filter(t -> t.getExpiresAt() != null && Instant.now().isBefore(t.getExpiresAt()))
                    .isPresent();
        } catch (Exception e) {
            log.warn("Failed to validate MFA temp token from MongoDB fallback store: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an MFA temporary token from MongoDB after successful verification.
     */
    public void deleteTempToken(String token) {
        if (!fallbackEnabled) {
            return;
        }
        try {
            authTempTokenRepository.deleteById(token);
            log.debug("MFA temp token deleted from MongoDB fallback store");
        } catch (Exception e) {
            log.warn("Failed to delete MFA temp token from MongoDB fallback store: {}", e.getMessage());
        }
    }
}
