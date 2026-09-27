package com.aspire.asat.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Creates TTL and lookup indexes for the Redis-fallback collections only.
 *
 * <p>Index creation is intentionally decoupled from {@code MongoTemplate} startup so that
 * legacy data issues in unrelated collections (e.g. duplicate {@code refresh_tokens.token}
 * values) cannot prevent the auth service from starting.
 */
@Component
public class AuthFallbackMongoIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(AuthFallbackMongoIndexInitializer.class);

    private final MongoTemplate mongoTemplate;

    public AuthFallbackMongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureFallbackIndexes() {
        ensureLookupIndex("auth_sessions", "userId");
        ensureTtlIndex("auth_sessions", "expiresAt");
        ensureLookupIndex("auth_temp_tokens", "userId");
        ensureTtlIndex("auth_temp_tokens", "expiresAt");
    }

    private void ensureLookupIndex(String collection, String field) {
        try {
            mongoTemplate.indexOps(collection).ensureIndex(new Index().on(field, Sort.Direction.ASC));
            log.info("Ensured lookup index on {}.{}", collection, field);
        } catch (Exception e) {
            log.warn("Could not ensure lookup index on {}.{}: {}", collection, field, e.getMessage());
        }
    }

    private void ensureTtlIndex(String collection, String field) {
        try {
            mongoTemplate.indexOps(collection)
                    .ensureIndex(new Index().on(field, Sort.Direction.ASC).expire(0));
            log.info("Ensured TTL index on {}.{}", collection, field);
        } catch (Exception e) {
            log.warn("Could not ensure TTL index on {}.{}: {}", collection, field, e.getMessage());
        }
    }
}
