package com.example.contextdemo.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JwtRevocationService {

    private static final String KEY_PREFIX = "revoked-jwt:";

    private final StringRedisTemplate redisTemplate;

    public JwtRevocationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void revoke(String jwtId, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + jwtId,
                "revoked",
                Duration.ofSeconds(ttlSeconds));
    }

    public boolean isRevoked(String jwtId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(KEY_PREFIX + jwtId));
    }
}
