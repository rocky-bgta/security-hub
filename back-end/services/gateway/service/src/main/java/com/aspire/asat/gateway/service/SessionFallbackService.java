package com.aspire.asat.gateway.service;

import com.aspire.asat.gateway.entity.AuthSession;
import com.aspire.asat.gateway.repository.AuthSessionFallbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Reactive service that reads session data from MongoDB when Redis is unavailable.
 *
 * <p>Documents are explicitly validated against {@code expiresAt} to prevent serving
 * expired sessions during the MongoDB TTL cleanup window (up to 60 s after expiry).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionFallbackService {

    private final AuthSessionFallbackRepository repository;

    /**
     * Finds an unexpired {@link AuthSession} by token ID.
     *
     * @param tokenId JWT subject / Redis key suffix
     * @return a {@link Mono} emitting the session if found and not expired, or empty
     */
    public Mono<AuthSession> findSession(String tokenId) {
        return repository.findById(tokenId)
                .filter(session -> {
                    if (session.getExpiresAt() == null) {
                        log.warn("AuthSession for tokenId={} has no expiresAt — treating as expired", tokenId);
                        return false;
                    }
                    boolean valid = Instant.now().isBefore(session.getExpiresAt());
                    if (!valid) {
                        log.debug("AuthSession for tokenId={} is expired (expiresAt={})", tokenId, session.getExpiresAt());
                    }
                    return valid;
                })
                .doOnNext(s -> log.info("Session resolved from MongoDB fallback: tokenId={}, userId={}", tokenId, s.getUserId()))
                .onErrorResume(e -> {
                    log.error("MongoDB fallback lookup failed for tokenId={}: {}", tokenId, e.getMessage());
                    return Mono.empty();
                });
    }
}
