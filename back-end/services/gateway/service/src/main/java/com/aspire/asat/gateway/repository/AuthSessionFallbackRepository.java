package com.aspire.asat.gateway.repository;

import com.aspire.asat.gateway.entity.AuthSession;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Reactive MongoDB repository for reading {@link AuthSession} documents.
 *
 * <p>The Gateway uses this only on the Redis-fallback path; normal traffic is
 * served entirely from Redis and the Caffeine in-process permission cache.
 */
@Repository
public interface AuthSessionFallbackRepository extends ReactiveMongoRepository<AuthSession, String> {
}
