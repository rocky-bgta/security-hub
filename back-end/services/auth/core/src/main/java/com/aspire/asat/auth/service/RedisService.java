package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import com.aspire.asat.auth.repository.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {
    final RedisTokenRepository redisTokenRepository;
    final RedisTemplate<String, String> redisTemplate;


    public void delete(String key) {
        redisTemplate.delete(key);
    }


    public void saveToken(RedisAccessToken redisAccessToken) {
        redisTokenRepository.save(redisAccessToken);
    }
    
    public void saveRefreshToken(String username, String refreshToken) {
        redisTokenRepository.saveRefreshToken(username, refreshToken);
    }

    public void savePermission(AccessTokenResponse response, List<String> permissions) {
        redisTokenRepository.savePermission(response, permissions);
    }

    public RedisAccessToken getToken(String userIdentity) {
        return redisTokenRepository.get(userIdentity);
    }


    public Boolean deleteToken(String userIdentity) {
        var redisToken = redisTokenRepository.get(userIdentity);
        if (redisToken != null) {
            redisTokenRepository.delete(userIdentity);
            return true;
        }

        return false;
    }


    /**
     * Delete token by tokenId (UUID-based primary method)
     */
    public Boolean deleteTokenByTokenId(String tokenId) {
        var redisToken = redisTokenRepository.getByTokenId(tokenId);
        if (redisToken != null) {
            redisTokenRepository.deleteByTokenId(tokenId);
            return true;
        }
        return false;
    }

    /**
     * Get active tokenId for a user (for single active token enforcement)
     */
    public String getActiveTokenIdForUser(String userId) {
        return redisTokenRepository.getActiveTokenIdForUser(userId);
    }

    /**
     * Revoke all previous tokens for a user (single active token enforcement)
     */
    public void revokeAllPreviousTokensForUser(String userId) {
        redisTokenRepository.revokeAllPreviousTokensForUser(userId);
    }

}
