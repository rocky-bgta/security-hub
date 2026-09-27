package com.aspire.asat.gateway.dto;

import com.aspire.asat.gateway.entity.redis.RedisAccessToken;

import java.util.List;

/**
 * Immutable result of resolving a session from either Redis or the MongoDB fallback.
 *
 * @param accessToken    the resolved session payload (or {@code null} if not found in either store)
 * @param permissions    the resolved permission list (may be empty, never {@code null})
 * @param activeTokenId  the active tokenId from Redis' {@code user_tokens:{userId}} key;
 *                       {@code null} when resolution came from the MongoDB fallback
 *                       (single-session enforcement is skipped in that case)
 * @param fromFallback   {@code true} when data came from MongoDB, {@code false} when from Redis
 */
public record SessionResolution(
        RedisAccessToken accessToken,
        List<String> permissions,
        String activeTokenId,
        boolean fromFallback
) {
}
