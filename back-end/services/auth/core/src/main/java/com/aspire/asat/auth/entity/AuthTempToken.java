package com.aspire.asat.auth.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB-backed store for MFA temporary tokens. Mirrors the Redis
 * {@code temp_token:{token}} key so that MFA login can proceed even when Redis
 * is unavailable.
 *
 * The {@code expiresAt} TTL index purges expired documents automatically.
 */
@Data
@Accessors(chain = true)
@Document(collection = "auth_temp_tokens")
public class AuthTempToken {

    /** The full temp-token JWT string used as the document identifier. */
    @Id
    private String token;

    @Indexed
    private String userId;

    /** TTL index — MongoDB removes the document when this instant is in the past. */
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
