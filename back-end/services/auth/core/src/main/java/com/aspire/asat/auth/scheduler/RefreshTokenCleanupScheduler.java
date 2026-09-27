package com.aspire.asat.auth.scheduler;

import com.aspire.asat.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * Clean up expired refresh tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            Instant now = Instant.now();
            var expiredTokens = refreshTokenRepository.findExpiredTokens(now);
            
            if (!expiredTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(expiredTokens);
                log.info("Cleaned up {} expired refresh tokens", expiredTokens.size());
            }
        } catch (Exception e) {
            log.error("Error during refresh token cleanup", e);
        }
    }
}
