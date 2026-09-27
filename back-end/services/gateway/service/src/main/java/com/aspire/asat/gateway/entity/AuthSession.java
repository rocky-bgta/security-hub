package com.aspire.asat.gateway.entity;

import com.aspire.asat.gateway.dto.RoleData;
import com.aspire.asat.gateway.dto.enums.UserStatus;
import com.aspire.asat.gateway.entity.redis.RedisAccessToken;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Read-only view of the {@code auth_sessions} collection written by the Auth service.
 * The Gateway reads this document only when Redis is unavailable (fallback path).
 *
 * <p>Field names mirror {@code com.aspire.asat.auth.entity.AuthSession} exactly so that
 * documents are interchangeable between the two modules without any transformation.
 */
@Data
@Accessors(chain = true)
@Document(collection = "auth_sessions")
public class AuthSession {

    @Id
    private String tokenId;

    @Indexed(name = "userId_1")
    private String userId;

    private String clientAdminId;
    private String mspId;
    private String countryId;
    private String email;
    private String phoneNumber;

    /** SHA-256 checksum of the access token JWT. Stored as {@code accessToken} in MongoDB. */
    private String accessTokenChecksum;

    private String userType;
    private String username;
    private UserStatus userStatus;
    private String fullName;
    private String clientAdminEmail;
    private String clientAdminFullName;
    private String coRelationId;
    private List<String> scope;
    private List<RoleData> roles;

    /** Flattened permission strings for the Gateway RBAC check. */
    private List<String> permissions;

    /** MongoDB TTL — documents with an expired {@code expiresAt} are auto-deleted. */
    @Indexed(name = "expiresAt_1", expireAfterSeconds = 0)
    private Instant expiresAt;

    /**
     * Converts this MongoDB document into the {@link RedisAccessToken} shape that
     * {@code JwtAuthFilter} expects, mapping {@code accessTokenChecksum} → {@code accessToken}.
     */
    public RedisAccessToken toRedisAccessToken() {
        RedisAccessToken token = new RedisAccessToken();
        token.setTokenId(this.tokenId);
        token.setUserId(this.userId);
        token.setClientAdminId(this.clientAdminId);
        token.setMspId(this.mspId);
        token.setCountryId(this.countryId);
        token.setEmail(this.email);
        token.setPhoneNumber(this.phoneNumber);
        token.setAccessToken(this.accessTokenChecksum);
        token.setUserType(this.userType);
        token.setUsername(this.username);
        token.setUserStatus(this.userStatus);
        token.setFullName(this.fullName);
        token.setClientAdminEmail(this.clientAdminEmail);
        token.setClientAdminFullName(this.clientAdminFullName);
        token.setCoRelationId(this.coRelationId);
        token.setScope(this.scope);
        token.setRoles(this.roles);
        return token;
    }
}
