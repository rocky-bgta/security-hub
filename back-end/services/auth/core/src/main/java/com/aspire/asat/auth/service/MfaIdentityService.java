package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.util.JWTUtils;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Resolves the MFA actor from tempToken (login challenge), gateway CurrentContext,
 * or a Bearer access JWT on public MFA routes that skip gateway auth.
 */
@Service
@RequiredArgsConstructor
public class MfaIdentityService extends BaseService {

    static final String AUTH_REQUIRED_MESSAGE = "Either tempToken or Authorization header is required";

    private final TemporaryTokenService temporaryTokenService;
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * tempToken wins when present so login MFA is unchanged.
     * Otherwise CurrentContext (JWT-gated routes), then Authorization Bearer (logged-in add-method).
     */
    public UUID resolveUserId(String tempToken) {
        if (StringUtils.hasText(tempToken)) {
            return temporaryTokenService.validateAndExtractUserId(tempToken);
        }
        try {
            CurrentUserContext currentUser = getCurrentUserContext();
            return UUID.fromString(currentUser.getUserId());
        } catch (Exception ignored) {
            // Public MFA routes do not receive CurrentContext from the gateway.
        }
        return resolveFromAccessToken();
    }

    UUID resolveFromAccessToken() {
        String authorization = getHeaderValue("Authorization").orElse(null);
        if (!StringUtils.hasText(authorization)) {
            throw new UnauthorizedResourceException(AUTH_REQUIRED_MESSAGE);
        }
        String token = stripBearer(authorization);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedResourceException(AUTH_REQUIRED_MESSAGE);
        }
        try {
            UUID claimed = parseAccessTokenUserId(token);
            AspireUser user = userRepository.findByUserId(claimed)
                    .or(() -> userRepository.findById(claimed))
                    .orElseThrow(() -> new UnauthorizedResourceException("User not found"));
            return user.getId() != null ? user.getId() : claimed;
        } catch (UnauthorizedResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedResourceException(AUTH_REQUIRED_MESSAGE);
        }
    }

    /**
     * Validate a session access JWT (not temp_mfa / refresh) and return the userId claim.
     */
    UUID parseAccessTokenUserId(String token) {
        if (JWTUtils.isTokenExpired(token, jwtSecret)) {
            throw new UnauthorizedResourceException("Access token has expired");
        }
        if (JWTUtils.isRefreshToken(token, jwtSecret)) {
            throw new UnauthorizedResourceException("Invalid token type");
        }
        String tokenType = JWTUtils.extractClaimByKey(token, jwtSecret, "type", String.class);
        if ("temp_mfa".equals(tokenType)) {
            throw new UnauthorizedResourceException("Invalid token type");
        }
        String userIdStr = JWTUtils.extractClaimByKey(token, jwtSecret, "userId", String.class);
        if (!StringUtils.hasText(userIdStr)) {
            throw new UnauthorizedResourceException("Invalid access token");
        }
        return UUID.fromString(userIdStr);
    }

    private static String stripBearer(String authorization) {
        String trimmed = authorization.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return JWTUtils.trimToken(trimmed, "Bearer");
    }
}
