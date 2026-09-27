package com.aspire.asat.auth.entity;

import com.aspire.asat.auth.dto.enums.UserStatus;
import com.aspire.asat.common.dto.files.RoleData;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB-backed session store. Written on every login/refresh and read by the
 * Gateway when Redis is unavailable. Acts as the durable source-of-truth for
 * active sessions; Redis is a best-effort cache layered on top.
 *
 * The {@code expiresAt} field carries a MongoDB TTL index so documents are
 * automatically purged once the session expires (same lifetime as the Redis key).
 */
@Data
@Accessors(chain = true)
@Document(collection = "auth_sessions")
public class AuthSession {

    /** Same UUID used as JWT subject and Redis key suffix (token:{tokenId}). */
    @Id
    private String tokenId;

    @Indexed(name = "userId_1")
    private String userId;

    private String clientAdminId;
    private String mspId;
    private String countryId;
    private String email;
    private String phoneNumber;

    /** SHA-256 checksum of the access token JWT (never the raw JWT). */
    private String accessTokenChecksum;

    /** SHA-256 checksum of the refresh token JWT. */
    private String refreshTokenChecksum;

    private String userType;
    private String username;
    private UserStatus userStatus;
    private String fullName;
    private String clientAdminEmail;
    private String clientAdminFullName;
    private String coRelationId;
    private List<String> scope;
    private List<RoleData> roles;

    /** Flattened permission strings loaded from role_permissions at login time. */
    private List<String> permissions;

    /** TTL index — MongoDB removes the document when this instant is in the past. */
    @Indexed(name = "expiresAt_1", expireAfterSeconds = 0)
    private Instant expiresAt;
}
