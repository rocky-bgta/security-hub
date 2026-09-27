package com.aspire.asat.gateway.service;

import com.aspire.asat.gateway.entity.redis.RedisAccessToken;
import com.aspire.asat.gateway.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin wrapper around {@link RedisTemplate} that provides both:
 * <ul>
 *   <li><b>Standard methods</b> — used on the normal (Redis-healthy) path;
 *       these may throw if Redis is unreachable.</li>
 *   <li><b>Safe methods</b> ({@code *Safe} suffix) — catch all exceptions and
 *       return {@code null} / empty so that the caller can fall back to MongoDB
 *       without disrupting the reactive pipeline.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String TOKEN_FOLDER = "token:";
    private static final String PERMISSION_FOLDER = "permission:";
    private static final String USER_TOKENS_FOLDER = "user_tokens:";

    final RedisTemplate<String, String> redisTemplate;

    // -------------------------------------------------------------------------
    // Standard reads (may throw; used internally only when already on a
    // boundedElastic thread via the safe wrappers below)
    // -------------------------------------------------------------------------

    public RedisAccessToken accessToken(String tokenId) {
        return JacksonUtil.jsonToInstance(
                redisTemplate.opsForValue().get(TOKEN_FOLDER.concat(tokenId)),
                RedisAccessToken.class);
    }

    @Cacheable(value = "permissions", key = "#tokenId")
    public List<String> getPermissions(String tokenId) {
        String json = redisTemplate.opsForValue().get(PERMISSION_FOLDER.concat(tokenId));
        if (json == null) {
            return List.of();
        }
        return JacksonUtil.jsonToInstance(json, new TypeReference<List<String>>() {});
    }

    public String getActiveTokenIdForUser(String userId) {
        return redisTemplate.opsForValue().get(USER_TOKENS_FOLDER.concat(userId));
    }

    // -------------------------------------------------------------------------
    // Safe reads — return null / empty List on any exception.
    // Called from JwtAuthFilter inside Mono.fromCallable().subscribeOn(boundedElastic).
    // -------------------------------------------------------------------------

    /**
     * Returns the session from Redis, or {@code null} if Redis is unavailable or
     * the key does not exist.
     */
    public RedisAccessToken accessTokenSafe(String tokenId) {
        try {
            return accessToken(tokenId);
        } catch (Exception e) {
            log.warn("Redis unavailable for session lookup (tokenId={}): {}", tokenId, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the active tokenId for the given userId, or {@code null} on error.
     */
    public String getActiveTokenIdForUserSafe(String userId) {
        try {
            return getActiveTokenIdForUser(userId);
        } catch (Exception e) {
            log.warn("Redis unavailable for active-token lookup (userId={}): {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Returns permissions for the given tokenId, or an empty list on error.
     * Note: this bypasses the Caffeine {@code @Cacheable} wrapper intentionally —
     * use {@link #getPermissions(String)} on the normal path.
     */
    public List<String> getPermissionsSafe(String tokenId) {
        try {
            return getPermissions(tokenId);
        } catch (Exception e) {
            log.warn("Redis unavailable for permissions lookup (tokenId={}): {}", tokenId, e.getMessage());
            return List.of();
        }
    }
}
