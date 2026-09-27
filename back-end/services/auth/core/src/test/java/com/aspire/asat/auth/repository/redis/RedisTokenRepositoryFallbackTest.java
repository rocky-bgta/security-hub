package com.aspire.asat.auth.repository.redis;

import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that Redis read failures return {@code null} (enabling the MongoDB fallback)
 * and that write/delete failures are swallowed silently so that login/logout never break
 * because of Redis being unavailable.
 */
@ExtendWith(MockitoExtension.class)
class RedisTokenRepositoryFallbackTest {

    @Mock
    private RedisTemplate<String, String> template;

    @Mock
    private ValueOperations<String, String> valueOps;

    private RedisTokenRepository redisTokenRepository;

    @BeforeEach
    void setUp() throws Exception {
        redisTokenRepository = new RedisTokenRepository(template);
        Field jwtExpField = RedisTokenRepository.class.getDeclaredField("jwtExpiration");
        jwtExpField.setAccessible(true);
        jwtExpField.set(redisTokenRepository, 20L);
    }

    // -------------------------------------------------------------------------
    // Read failures return null (trigger fallback)
    // -------------------------------------------------------------------------

    @Test
    void getByTokenId_WhenRedisThrows_ReturnsNull() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenThrow(new RuntimeException("Redis unavailable"));

        RedisAccessToken result = redisTokenRepository.getByTokenId("t1");

        assertNull(result);
    }

    @Test
    void getActiveTokenIdForUser_WhenRedisThrows_ReturnsNull() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenThrow(new RuntimeException("Redis unavailable"));

        String result = redisTokenRepository.getActiveTokenIdForUser("u1");

        assertNull(result);
    }

    @Test
    void get_WhenRedisReturnsNull_ReturnsNullWithoutException() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);

        RedisAccessToken result = redisTokenRepository.get("t1");

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // Write / delete failures are non-fatal
    // -------------------------------------------------------------------------

    @Test
    void deleteByTokenId_WhenRedisThrows_DoesNotPropagateException() {
        doThrow(new RuntimeException("Redis unavailable")).when(template).delete(anyString());

        assertDoesNotThrow(() -> redisTokenRepository.deleteByTokenId("t1"));
    }

    @Test
    void deleteActiveTokenForUser_WhenRedisThrows_DoesNotPropagateException() {
        doThrow(new RuntimeException("Redis unavailable")).when(template).delete(anyString());

        assertDoesNotThrow(() -> redisTokenRepository.deleteActiveTokenForUser("u1"));
    }

    @Test
    void revokeAllPreviousTokensForUser_WhenRedisThrows_DoesNotPropagateException() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenThrow(new RuntimeException("Redis unavailable"));

        assertDoesNotThrow(() -> redisTokenRepository.revokeAllPreviousTokensForUser("u1"));
    }

    // -------------------------------------------------------------------------
    // Happy-path: reads return expected values
    // -------------------------------------------------------------------------

    @Test
    void getActiveTokenIdForUser_WhenRedisHealthy_ReturnsStoredTokenId() {
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("user_tokens:u1")).thenReturn("token-42");

        String result = redisTokenRepository.getActiveTokenIdForUser("u1");

        assertEquals("token-42", result);
    }

    @Test
    void saveRefreshToken_WhenRedisThrows_DoesNotPropagateException() {
        when(template.opsForValue()).thenReturn(valueOps);
        doThrow(new RuntimeException("Redis unavailable"))
                .when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        assertDoesNotThrow(() -> redisTokenRepository.saveRefreshToken("user@example.com", "rt.token"));
    }
}
