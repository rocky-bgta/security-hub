package com.aspire.asat.auth.repository.redis;


import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import com.aspire.asat.auth.exception.DatabaseException;
import com.aspire.asat.auth.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Low-level Redis repository for session, permission and single-active-token data.
 *
 * <p>All Redis operations are wrapped in try/catch so that a Redis outage never
 * propagates exceptions to the caller. Write failures are logged as warnings;
 * read failures return {@code null} (which triggers the MongoDB fallback at the
 * service layer). This makes Redis a best-effort cache rather than a hard
 * dependency.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {
    private static final String FOLDER = "token:";
    private static final String PERMISSION_FOLDER = "permission:";
    private static final String REFRESH_TOKEN_FOLDER = "refresh_token:user-";
    private static final String USER_TOKENS_FOLDER = "user_tokens:";

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private final RedisTemplate<String, String> template;

    public void save(RedisAccessToken redisAccessToken) {
        var val = JacksonUtil.objectToJson(redisAccessToken);
        if (val == null) {
            throw new DatabaseException(ResponseMessage.INVALID_REQUEST_DATA.getResponseMessage());
        }
        try {
            template.opsForValue().set(FOLDER.concat(redisAccessToken.getTokenId()), val, jwtExpiration, TimeUnit.MINUTES);
            String userTokensKey = USER_TOKENS_FOLDER.concat(redisAccessToken.getUserId());
            template.opsForValue().set(userTokensKey, redisAccessToken.getTokenId(), jwtExpiration, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis unavailable — session not cached (tokenId={}): {}", redisAccessToken.getTokenId(), e.getMessage());
        }
    }

    public void savePermission(AccessTokenResponse response, List<String> permissions) {
        String json = JacksonUtil.objectToJson(permissions);
        if (json == null) {
            throw new DatabaseException(ResponseMessage.INVALID_REQUEST_DATA.getResponseMessage());
        }
        try {
            template.opsForValue().set(PERMISSION_FOLDER.concat(response.getTokenId()), json, jwtExpiration, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis unavailable — permissions not cached (tokenId={}): {}", response.getTokenId(), e.getMessage());
        }
    }

    public void saveRefreshToken(String username, String refreshToken) {
        try {
            template.opsForValue().set(REFRESH_TOKEN_FOLDER.concat(username), refreshToken, jwtExpiration, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis unavailable — refresh token not cached (username={}): {}", username, e.getMessage());
        }
    }

    public RedisAccessToken get(String userIdentity) {
        try {
            return JacksonUtil.jsonToInstance(template.opsForValue().get(FOLDER.concat(userIdentity)), RedisAccessToken.class);
        } catch (Exception e) {
            log.warn("Redis unavailable — session not readable (key={}): {}", userIdentity, e.getMessage());
            return null;
        }
    }

    public void delete(String userIdentity) {
        try {
            template.delete(FOLDER.concat(userIdentity));
        } catch (Exception e) {
            log.warn("Redis unavailable — session not deleted (key={}): {}", userIdentity, e.getMessage());
        }
    }

    public RedisAccessToken getByTokenId(String tokenId) {
        try {
            return JacksonUtil.jsonToInstance(template.opsForValue().get(FOLDER.concat(tokenId)), RedisAccessToken.class);
        } catch (Exception e) {
            log.warn("Redis unavailable — session not readable (tokenId={}): {}", tokenId, e.getMessage());
            return null;
        }
    }

    public void deleteByTokenId(String tokenId) {
        try {
            template.delete(FOLDER.concat(tokenId));
        } catch (Exception e) {
            log.warn("Redis unavailable — session not deleted (tokenId={}): {}", tokenId, e.getMessage());
        }
    }

    public String getActiveTokenIdForUser(String userId) {
        try {
            return template.opsForValue().get(USER_TOKENS_FOLDER.concat(userId));
        } catch (Exception e) {
            log.warn("Redis unavailable — active token mapping not readable (userId={}): {}", userId, e.getMessage());
            return null;
        }
    }

    public void deleteActiveTokenForUser(String userId) {
        try {
            template.delete(USER_TOKENS_FOLDER.concat(userId));
        } catch (Exception e) {
            log.warn("Redis unavailable — active token mapping not deleted (userId={}): {}", userId, e.getMessage());
        }
    }

    public void revokeAllPreviousTokensForUser(String userId) {
        try {
            String activeTokenId = getActiveTokenIdForUser(userId);
            if (activeTokenId != null) {
                deleteByTokenId(activeTokenId);
                try {
                    template.delete(PERMISSION_FOLDER.concat(activeTokenId));
                } catch (Exception e) {
                    log.warn("Redis unavailable — permission not deleted (tokenId={}): {}", activeTokenId, e.getMessage());
                }
            }
            deleteActiveTokenForUser(userId);
        } catch (Exception e) {
            log.warn("Redis unavailable — could not revoke previous tokens (userId={}): {}", userId, e.getMessage());
        }
    }
}
