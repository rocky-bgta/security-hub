package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.dto.enums.UserStatus;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.RefreshToken;
import com.aspire.asat.auth.entity.UserLoginHistory;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.mapper.UserMapper;
import com.aspire.asat.auth.model.token.*;
import com.aspire.asat.auth.repository.RefreshTokenRepository;
import com.aspire.asat.auth.repository.RolePermissionRepository;
import com.aspire.asat.auth.repository.UserLoginHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.impl.PasswordResetServiceImpl;
import com.aspire.asat.auth.util.ChecksumUtil;
import com.aspire.asat.auth.util.JWTUtils;
import com.aspire.asat.common.client.ActivityLogClient;
import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.files.RoleData;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.TokenActionType;
import com.aspire.asat.common.enums.UserType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService extends BaseService {
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;
    private final UserMapper userMapper;
    private final ActivityLogClient activityLogClient;
    private final PasswordResetServiceImpl passwordResetService;
    private final Environment environment;

    @Autowired(required = false)
    private MfaService mfaService;

    @Autowired(required = false)
    private TemporaryTokenService temporaryTokenService;

    @Autowired(required = false)
    private SessionFallbackService sessionFallbackService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration:1440}")
    private Long jwtRefreshExpiration;

    @Value("${auth.local-superadmin.email:superadmin01@yopmail.com}")
    private String localSuperadminEmail;

    @Value("${auth.local-superadmin.password:123456789}")
    private String localSuperadminPassword;

    private static final Set<String> RESTRICTIVE_STATUSES = Set.of(
            "INACTIVE",
            "SUSPEND",
            "SUSPENDED",
            "TEMPORARY_BLOCKED",
            "BLOCKED",
            "BLOCK"
    );

    public TokenShortResponse loginWithUsernamePassword(@Valid LoginWithPasswordRequest request) {
        AspireUser user = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(
                        () -> new ResourceNotFoundException(ResponseMessage.USER_NOT_FOUND.getResponseMessage()));

        passwordValidation(request, user);

        /*
         *  CheckList for User Login
         *  1. Check where the customer password is expired or not
         *  2. Device Binding required or not
         *  3. If the password is expired, then redirect to change password page
         *  4. If Device Binding is required, then redirect to the device binding page
         *  5. Check if MFA is required (global config + user state)
         *  6. Check if trial period has expired
         *  7. Check if user status is restrictive (INACTIVE, SUSPEND, blocked)
         *
         */

        assertUserStatusAllowsAuthentication(user);

        // Check trial expiration
        if (Boolean.TRUE.equals(user.getIsTrial()) && user.getTrialEndDate() != null) {
            if (Instant.now().isAfter(user.getTrialEndDate())) {
                throw new UnauthorizedResourceException(ResponseMessage.TRIAL_PERIOD_EXPIRED.getResponseMessage());
            }
        }

        // Save login history before MFA checks (token will be null initially, updated later if login succeeds)
        saveUserLoginHistory(user, null, request.getDeviceInfo());

        // Check MFA requirements
        if (mfaService != null && mfaService.isMfaEnabled()) {
            // If user needs MFA setup (mfa_secret is null), indicate setup required
            if (mfaService.needsMfaSetup(user)) {
                String tempToken = generateTemporaryTokenIfAvailable(user);
                return TokenShortResponse.builder()
                        .mfaSetupRequired(true)
                        .mfaMethod(null) // User needs to choose method
                        .methods(List.of())
                        .tempToken(tempToken)
//                        .credentialChangeNeeded(markForcedCredentialChangeIfNeeded(user, tempToken))
                        .build();
            }

            // If user has MFA enabled, indicate verification required
            if (mfaService.needsMfaVerification(user)) {
                String tempToken = generateTemporaryTokenIfAvailable(user);
                return TokenShortResponse.builder()
                        .mfaVerificationRequired(true)
                        .mfaMethod(mfaService.getDefaultMethodName(user))
                        .methods(mfaService.getEnrolledMethods(user))
                        .tempToken(tempToken)
//                        .credentialChangeNeeded(markForcedCredentialChangeIfNeeded(user, tempToken))
                        .build();
            }
        }

        return createAccessTokenResponse(user);
    }

    private String generateTemporaryTokenIfAvailable(AspireUser user) {
        if (temporaryTokenService == null) {
            return null;
        }
        return temporaryTokenService.generateTemporaryToken(user.getUserId(), user.getUsername());
    }

    /**
     * When a forced password change is required for MFA flows, persist the temp token
     * as a password-reset token and return true so the client can redirect to change-password.
     */
    private boolean markForcedCredentialChangeIfNeeded(AspireUser user, String tempToken) {
        if (!requiresForcedCredentialChange(user)) {
            return false;
        }
        if (StringUtils.hasText(tempToken) && passwordResetService != null) {
            passwordResetService.savePasswordResetToken(user.getUsername(), tempToken);
        }
        return true;
    }

    private void passwordValidation(LoginWithPasswordRequest request, AspireUser user) {
        // Handle password matching with proper error handling
        try {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                if (!matchesLocalSuperadminPassword(request)) {
                    throw new UnauthorizedResourceException(ResponseMessage.USERNAME_PASSWORD_NOT_MATCH.getResponseMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            // Log the error for debugging
            logger.error("Password encoding error for user: {}, error: {}", request.getUsername(), e.getMessage());

            // Check if the password is stored as plain text (for backward compatibility)
            if (request.getPassword().equals(user.getPassword())) {
                // Password matches as plain text, but we should re-encode it for security
                logger.warn("User {} has plain text password, re-encoding for security", request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            } else {
                // Try to handle the case where the password might be stored with {noop} prefix
                String storedPassword = user.getPassword();
                if (storedPassword != null && storedPassword.startsWith("{noop}")) {
                    String plainPassword = storedPassword.substring(7); // Remove {noop} prefix
                    if (request.getPassword().equals(plainPassword)) {
                        logger.warn("User {} has {noop} prefixed password, re-encoding for security", request.getUsername());
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                        userRepository.save(user);
                    } else if (matchesLocalSuperadminPassword(request)) {
                        logger.warn("Using local-only superadmin bootstrap password fallback for {}", request.getUsername());
                    } else {
                        throw new UnauthorizedResourceException(ResponseMessage.USERNAME_PASSWORD_NOT_MATCH.getResponseMessage());
                    }
                } else if (matchesLocalSuperadminPassword(request)) {
                    logger.warn("Using local-only superadmin bootstrap password fallback for {}", request.getUsername());
                } else {
                    throw new UnauthorizedResourceException(ResponseMessage.USERNAME_PASSWORD_NOT_MATCH.getResponseMessage());
                }
            }
        }
    }

    private boolean matchesLocalSuperadminPassword(LoginWithPasswordRequest request) {
        return environment.acceptsProfiles(Profiles.of("local"))
                && localSuperadminEmail.equalsIgnoreCase(request.getUsername().trim())
                && localSuperadminPassword.equals(request.getPassword());
    }

    /**
     * Create access token response after successful MFA verification
     * This is a public method that can be called from MFA controller
     */
    public TokenShortResponse createAccessTokenResponseAfterMfa(AspireUser aspireUser) {
        assertUserStatusAllowsAuthentication(aspireUser);
        return createAccessTokenResponse(aspireUser);
    }

    private TokenShortResponse createAccessTokenResponse(AspireUser aspireUser) {
        // Revoke all previous tokens for this user to ensure only one active session
        revokeAllPreviousTokens(aspireUser.getUserId().toString());

        Map<String, Object> claims = getClaims(aspireUser);
        AccessTokenResponse response = getAccessTokenResponse(aspireUser, claims);

        // Compute permissions once so they are written to both stores identically.
        List<String> permissions = getUserPermissions(aspireUser);

        // Build the Redis session object (checksums, not raw JWTs).
        RedisAccessToken redisAccessToken = buildRedisAccessToken(response);

        // 1. MongoDB primary write — source of truth for the fallback path.
        if (sessionFallbackService != null) {
            sessionFallbackService.saveSession(redisAccessToken, permissions, jwtExpiration);
        }

        // 2. Redis best-effort cache — failures are now non-fatal (try/catch in repository).
        redisService.saveToken(redisAccessToken);
        redisService.savePermission(response, permissions);

        aspireUser.setLastLoginAt(response.getLastLoginTime());

        userRepository.save(aspireUser);

        // Log login activity to activity log
        saveLoginActivityLog(aspireUser);

        // Convert RoleData to role names for frontend compatibility
        List<String> roleNames = response.getRoles() != null
                ? response.getRoles().stream()
                .filter(role -> role != null && role.getRoleName() != null)
                .map(RoleData::getRoleName)
                .toList()
                : List.of();
        if (roleNames.isEmpty() && StringUtils.hasText(aspireUser.getUserType())) {
            roleNames = List.of(normalizeUserTypeString(aspireUser.getUserType()));
        }

        // Calculate trial days left
        Integer trialDaysLeft = null;
        if (Boolean.TRUE.equals(aspireUser.getIsTrial()) && aspireUser.getTrialEndDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(Instant.now(), aspireUser.getTrialEndDate());
            trialDaysLeft = daysBetween < 0 ? 0 : (int) daysBetween;
        }

//        boolean credentialChangeNeeded = response.isCredentialChangeNeeded();
        // Force password change: withhold session tokens; FE uses tempToken for change-password only.
       /* if (credentialChangeNeeded) {
            return TokenShortResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .tempToken(response.getAccessToken())
                    .deviceBindingNeeded(response.isDeviceBindingNeeded())
                    .credentialChangeNeeded(true)
                    .lastLoginTime(response.getLastLoginTime())
                    .roleNames(roleNames)
                    .trialDaysLeft(trialDaysLeft)
                    .isBuyNow(response.getIsBuyNow())
                    .onboardBy(response.getOnboardBy())
                    .build();
        }*/
        return TokenShortResponse.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
//                .deviceBindingNeeded(response.isDeviceBindingNeeded())
//                .credentialChangeNeeded(false)
                .lastLoginTime(response.getLastLoginTime())
                .roleNames(roleNames)
                .trialDaysLeft(trialDaysLeft)
                .isBuyNow(response.getIsBuyNow())
                .onboardBy(response.getOnboardBy())
                .build();

    }

    private Map<String, Object> getClaims(AspireUser aspireUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", aspireUser.getUserId().toString());
        claims.put("username", aspireUser.getUsername());
        claims.put("email", aspireUser.getEmail());
        claims.put("phoneNumber", aspireUser.getPhoneNumber());
        return claims;
    }

    private List<String> getUserPermissions(AspireUser aspireUser) {
        List<String> permissions = new ArrayList<>();
        for (var roleId : aspireUser.getRoles()) {
            var rolePermissionOpt = rolePermissionRepository.findByRoleId(roleId);
            if (rolePermissionOpt.isPresent()) {
                var rolePermission = rolePermissionOpt.get();
                if (rolePermission.getMenuPermissions() != null) {
                    for (var menuPermission : rolePermission.getMenuPermissions()) {
                        if (menuPermission.getPermissions() != null) {
                            permissions.addAll(menuPermission.getPermissions());
                        }
                    }
                }
            } else {
                logger.error("No permissions found for role ID: {}", roleId);
            }
        }
        return permissions;
    }

    /**
     * Builds a {@link RedisAccessToken} from the given response, storing JWT checksums
     * rather than raw tokens. Shared between the Redis cache write and the MongoDB
     * primary write so both stores are always populated with the same data.
     */
    private RedisAccessToken buildRedisAccessToken(AccessTokenResponse response) {
        final RedisAccessToken redisAccessToken = new RedisAccessToken();
        BeanUtils.copyProperties(response, redisAccessToken);
        redisAccessToken.setAccessToken(ChecksumUtil.createChecksum(response.getAccessToken()));
        redisAccessToken.setRefreshToken(ChecksumUtil.createChecksum(response.getRefreshToken()));
        redisAccessToken.setTokenId(response.getTokenId());
        redisAccessToken.setUserId(response.getUserId());
        redisAccessToken.setUserType(response.getUserType());
        redisAccessToken.setClientAdminId(response.getClientAdminId());
        redisAccessToken.setMspId(response.getMspId());
        redisAccessToken.setCountryId(response.getCountryId());
        redisAccessToken.setRoles(response.getRoles());
        redisAccessToken.setClientAdminEmail(response.getClientAdminEmail());
        redisAccessToken.setClientAdminFullName(response.getClientAdminFullName());
        return redisAccessToken;
    }

    private AccessTokenResponse getAccessTokenResponse(AspireUser aspireUser, Map<String, Object> claims) {
        // Generate a unique token ID for this session
        String tokenId = UUID.randomUUID().toString();

        final String jwt = JWTUtils.generateToken(claims, tokenId, String.valueOf(jwtExpiration), jwtSecret);
        final String refreshToken = JWTUtils.generateRefreshToken(aspireUser.getUsername(), String.valueOf(jwtRefreshExpiration), jwtSecret);

        AccessTokenResponse response = userMapper.mapToTokenResponse(aspireUser);
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setTokenId(tokenId); // Set the token ID
        response.setEmail(aspireUser.getEmail());
        response.setUsername(aspireUser.getUsername());
        response.setUserStatus(aspireUser.getStatus());
        // Roles are already populated by UserMapper.mapToTokenResponse

        // Get last login time from the user login history
        Optional<UserLoginHistory> lastLoginHistory = userLoginHistoryRepository
                .findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(aspireUser.getUsername(), TokenActionType.LOGIN);
        response.setLastLoginTime(lastLoginHistory.map(UserLoginHistory::getLoginTime).orElse(Instant.now()));


        // Save a refresh token to a database
        saveRefreshToken(aspireUser, refreshToken);

        // Soft gate: only MSP / CLIENT_ADMIN / ASPIRE_ADMIN / SUPER_ADMIN with temp password
        // Other user types (USER, CLIENT, SYSTEM_USER, etc.) are intentionally unchanged.
        /*if (requiresForcedCredentialChange(aspireUser)) {
            response.setCredentialChangeNeeded(true);
        }*/

        return response;
    }

    /**
     * Forced first-login password change is limited to admin-style accounts that were
     * provisioned with a temporary password ({@code isDefault=true}).
     * Buy-now users are excluded at create time by setting {@code isDefault=false}
     * (they chose their own password). Login uses {@code isDefault} only so that a later
     * admin-issued temp password can still force a reset.
     */
    private boolean requiresForcedCredentialChange(AspireUser user) {
        if (user == null || !Boolean.TRUE.equals(user.getIsDefault())) {
            return false;
        }
        String userType = user.getUserType();
        if (userType == null || userType.isBlank()) {
            return false;
        }
        return UserType.MSP.getValue().equalsIgnoreCase(userType)
                || UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.SYSTEM_USER.getValue().equalsIgnoreCase(userType);
    }

    private void saveUserLoginHistory(AspireUser aspireUser, String token, DeviceInfoRequest deviceInfo) {
        String deviceInfoJson = ObjectUtils.isEmpty(deviceInfo) ? " " : writeJsonString(deviceInfo);
        UserLoginHistory userLoginHistory = userMapper.mapToLoginHistoryEntity(
                aspireUser,
                token,
                getRemoteIPAddress(),
                deviceInfoJson
        );
        userLoginHistoryRepository.save(userLoginHistory);
    }


    public Boolean logout(LogoutRequest logoutRequest) {
        String targetUsername;
        DeviceInfoRequest deviceInfo = logoutRequest != null ? logoutRequest.getDeviceInfo() : null;

        CurrentUserContext currentUserContext = getCurrentUserContext();
        targetUsername = currentUserContext.getUsername();
        final var targetUserId = currentUserContext.getUserId();
        final var tokenId = currentUserContext.getTokenId();

        // Insert Logout history with device info
        UserLoginHistory history = new UserLoginHistory();
        history.setId(UUID.randomUUID());
        history.setUsername(targetUsername);
        history.setUserId(targetUserId);
        history.setClientAdminId(getClientAdminId(currentUserContext.getUserType(), targetUserId, currentUserContext.getClientAdminId()));
        history.setAction(TokenActionType.LOGOUT);
        history.setRequestIp(getRemoteIPAddress());
        history.setLogoutTime(Instant.now());
        history.setLoginTime(Instant.now());
        history.setUserType(currentUserContext.getUserType());

        // Save device info if provided
        if (deviceInfo != null) {
            history.setDeviceInfo(writeJsonString(deviceInfo));
            logger.info("Device info saved in logout activity log for user: {}", targetUsername);
        }

        userLoginHistoryRepository.save(history);

        // Log logout activity to activity log
        saveLogoutActivityLog(currentUserContext);

        // Revoke all refresh tokens for the user
        revokeAllRefreshTokens(targetUsername);

        // MongoDB revoke — durable removal of the session document.
        if (sessionFallbackService != null) {
            sessionFallbackService.revokeAllSessionsForUser(targetUserId);
        }

        // Redis revoke — now non-fatal (try/catch in repository).
        boolean deleted;
        if (tokenId != null) {
            deleted = redisService.deleteTokenByTokenId(tokenId);
        } else {
            deleted = redisService.deleteToken(targetUsername + ":" + targetUserId);
        }
        redisService.revokeAllPreviousTokensForUser(targetUserId);
        return deleted;
    }

    /**
     * Force-logout a user by userId without requiring CurrentContext.
     * Used by registration when status becomes INACTIVE / SUSPEND / blocked.
     */
    public Boolean forceLogout(ForceLogoutRequest request) {
        if (request == null || !StringUtils.hasText(request.getUserId())) {
            throw new BadRequestException("userId is required");
        }
        AspireUser user = resolveUserForForcedLogout(request.getUserId());
        String resolvedUserId = user.getUserId() != null ? user.getUserId().toString() : user.getId().toString();
        log.info("Forced logout for userId: {}, username: {}", resolvedUserId, user.getUsername());
        revokeAllPreviousTokens(resolvedUserId);
        return true;
    }

    private AspireUser resolveUserForForcedLogout(String userId) {
        try {
            UUID uuid = UUID.fromString(userId.trim());
            return userRepository.findByUserId(uuid)
                    .or(() -> userRepository.findById(uuid))
                    .orElseThrow(() -> new ResourceNotFoundException(ResponseMessage.USER_NOT_FOUND.getResponseMessage()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid userId format: " + userId);
        }
    }

    /**
     * Returns true when the user's status should block authentication and trigger forced logout.
     */
    public static boolean isRestrictiveStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return RESTRICTIVE_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
    }

    private void assertUserStatusAllowsAuthentication(AspireUser user) {
        if (isRestrictiveStatus(user.getStatus())) {
            throw new UnauthorizedResourceException(ResponseMessage.USER_IS_NOT_ACTIVE.getResponseMessage());
        }
        // End users must be ACTIVE (covers PENDING and other non-restrictive non-active values)
        if (UserType.USER.getValue().equalsIgnoreCase(user.getUserType())) {
            if (user.getStatus() == null || !UserStatus.ACTIVE.name().equalsIgnoreCase(user.getStatus())) {
                throw new UnauthorizedResourceException(ResponseMessage.USER_IS_NOT_ACTIVE.getResponseMessage());
            }
        }
    }

    private String getClientAdminId(String userType, String userId, String clientAdminId) {
        return userType.equalsIgnoreCase(UserType.USER.getValue())
                ? clientAdminId : userId;
    }


    /**
     * Refresh access token using refresh token
     */
    public TokenShortResponse refreshToken(RefreshTokenRequest request) {
        try {
            // Validate refresh token
            if (!JWTUtils.isRefreshToken(request.getRefreshToken(), jwtSecret)) {
                throw new UnauthorizedResourceException("Invalid refresh token");
            }

            // Extract username from a refresh token
            String username;
            try {
                username = JWTUtils.extractUserName(request.getRefreshToken(), jwtSecret);
            } catch (Exception e) {
                throw new UnauthorizedResourceException("Invalid refresh token format");
            }

            // Check if the refresh token exists and is valid in a database
            RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(request.getRefreshToken())
                    .orElseThrow(() -> new BadRequestException("Refresh token not found"));

            if (!refreshToken.isValid()) {
                throw new UnauthorizedResourceException("Refresh token is expired or revoked");
            }

            // Get user details
            AspireUser user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UnauthorizedResourceException("User not found"));

            assertUserStatusAllowsAuthentication(user);

            // Remove the used refresh token so it cannot be replayed.
            refreshTokenRepository.delete(refreshToken);

            // Revoke all previous tokens for this user to ensure only one active session
            revokeAllPreviousTokens(user.getUserId().toString());

            return createAccessTokenResponse(user);
        } catch (Exception e) {
            // Handle any unexpected exceptions
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new BadRequestException("Invalid refresh token");
        }
    }

    /**
     * Revoke all refresh tokens for a user (used during logout)
     */
    private void revokeAllRefreshTokens(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    /**
     * Save a refresh token to a database
     */
    private void saveRefreshToken(AspireUser aspireUser, String refreshToken) {
        RefreshToken token = new RefreshToken()
                .setToken(refreshToken)
                .setUsername(aspireUser.getUsername())
                .setUserType(aspireUser.getUserType())
                .setExpiryDate(Instant.now().plusSeconds(jwtRefreshExpiration * 60))
                .setDeviceId("")
                .setIpAddress(getRemoteIPAddress());

        refreshTokenRepository.save(token);
    }

    /**
     * Revoke all previous tokens for a user to ensure only one active session
     * Now properly implemented with user-to-token mapping
     */
    private void revokeAllPreviousTokens(String userId) {
        revokeAllRefreshTokensByUserId(userId);

        // MongoDB revoke (durable; best-effort — failure is logged but does not throw).
        if (sessionFallbackService != null) {
            sessionFallbackService.revokeAllSessionsForUser(userId);
        }

        // Redis revoke (cache; now non-fatal via try/catch in repository).
        redisService.revokeAllPreviousTokensForUser(userId);

        logger.info("Successfully revoked all previous tokens for userId: {}", userId);
    }

    /**
     * Revoke all refresh tokens for a user by userId
     */
    private void revokeAllRefreshTokensByUserId(String userId) {
        // Find user by userId (domain id) with fallback to Mongo document id
        UUID uuid = UUID.fromString(userId);
        AspireUser user = userRepository.findByUserId(uuid)
                .or(() -> userRepository.findById(uuid))
                .orElseThrow(() -> new UnauthorizedResourceException("User not found")); // only for access token

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUsernameAndIsRevokedFalse(user.getUsername());
        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(activeTokens);
        }
    }

    /**
     * Validate that the token is the only active token for the user
     * This ensures single active session enforcement
     */
    public boolean validateSingleActiveToken(String tokenId, String userId) {
        try {
            String activeTokenId = redisService.getActiveTokenIdForUser(userId);

            if (activeTokenId == null) {
                // No active token found, this shouldn't happen in normal flow
                logger.warn("No active token found for userId: {}", userId);
                return false;
            }

            if (!activeTokenId.equals(tokenId)) {
                // Different token is active, this token is invalid
                logger.warn("Token {} is not the active token for userId: {}. Active token: {}", tokenId, userId, activeTokenId);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Error validating single active token for userId: {}, tokenId: {}", userId, tokenId, e);
            return false;
        }
    }

    private UserType resolveUserType(String userTypeStr) {
        if (ObjectUtils.isEmpty(userTypeStr)) {
            return null;
        }
        try {
            return UserType.fromString(userTypeStr);
        } catch (Exception e) {
            logger.warn("Could not resolve UserType from value '{}': {}", userTypeStr, e.getMessage());
            return null;
        }
    }

    /**
     * Infer UserType from string when fromString fails (e.g. different format in context).
     * Ensures ASPIRE_ADMIN and MSP are never saved as SYSTEM_USER when the string indicates otherwise.
     */
    private UserType inferUserTypeFromString(String userTypeStr) {
        if (userTypeStr == null || userTypeStr.isBlank()) {
            return null;
        }
        String s = userTypeStr.trim();
        if (UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.SUPER_ADMIN;
        }
        if (UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.ASPIRE_ADMIN;
        }
        if (UserType.SYSTEM_USER.getValue().equalsIgnoreCase(s)) {
            return UserType.SYSTEM_USER;
        }
        if (UserType.MSP.getValue().equalsIgnoreCase(s)) {
            return UserType.MSP;
        }
        if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(s)) {
            return UserType.CLIENT_ADMIN;
        }
        if (UserType.CLIENT.getValue().equalsIgnoreCase(s)) {
            return UserType.CLIENT;
        }
        if (UserType.USER.getValue().equalsIgnoreCase(s)) {
            return UserType.USER;
        }
        // Try normalized form (e.g. "Aspire Admin" -> "ASPIRE_ADMIN")
        try {
            String normalized = s.replace(' ', '_').toUpperCase();
            return UserType.fromString(normalized);
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private void saveLoginActivityLog(AspireUser aspireUser) {
        try {
            String userId = aspireUser.getUserId().toString();
            String userTypeStr = aspireUser.getUserType();
            // Normalize so "Aspire_Admin", "aspire_admin", "ASPIRE_ADMIN" all match
            String normalizedType = normalizeUserTypeString(userTypeStr);

            UserType userType = resolveUserType(userTypeStr);
            if (userType == null && StringUtils.hasText(userTypeStr)) {
                userType = inferUserTypeFromString(userTypeStr);
            }
            // When entity has no userType, infer from mspId/clientAdminId so ASPIRE_ADMIN/MSP are not saved as SYSTEM_USER
            if (userType == null) {
                userType = inferUserTypeFromLoginUser(aspireUser);
            }

            String ipAddress = getRemoteIPAddress();

            String aspireAdminId = null;
            String mspId = ObjectUtils.isEmpty(aspireUser.getMspId()) ? null : aspireUser.getMspId();
            String clientAdminId = ObjectUtils.isEmpty(aspireUser.getClientAdminId()) ? null : aspireUser.getClientAdminId();
            String countryId = ObjectUtils.isEmpty(aspireUser.getCountry()) ? null : aspireUser.getCountry();

            // Use normalized type for ID assignment so all variants (ASPIRE_ADMIN, Aspire_Admin, SYSTEM_USER, etc.) are handled
            if (UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(normalizedType)
                    || UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(normalizedType)
                    || UserType.SYSTEM_USER.getValue().equalsIgnoreCase(normalizedType)) {
                aspireAdminId = userId;
            } else if (UserType.MSP.getValue().equalsIgnoreCase(normalizedType)) {
                mspId = (mspId == null) ? userId : mspId;
            } else if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(normalizedType)) {
                clientAdminId = (clientAdminId == null) ? userId : clientAdminId;
            } else if (userType != null) {
                // userType inferred from user but not yet applied to IDs
                switch (userType) {
                    case ASPIRE_ADMIN:
                    case SUPER_ADMIN:
                    case SYSTEM_USER:
                        aspireAdminId = userId;
                        break;
                    case MSP:
                        mspId = (mspId == null) ? userId : mspId;
                        break;
                    case CLIENT_ADMIN:
                        clientAdminId = (clientAdminId == null) ? userId : clientAdminId;
                        break;
                    default:
                        break;
                }
            }

            String fullName = (ObjectUtils.isEmpty(aspireUser.getFirstName()) ? "" : aspireUser.getFirstName())
                    + " "
                    + (ObjectUtils.isEmpty(aspireUser.getLastName()) ? "" : aspireUser.getLastName());

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(ActivityType.USER_LOGIN)
                    .userId(userId)
                    .email(aspireUser.getEmail())
                    .username(aspireUser.getUsername())
                    .fullName(fullName.trim())
                    .userType(userType != null ? userType : UserType.SYSTEM_USER)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .description("User " + aspireUser.getUsername() + " logged in")
                    .ipAddress(ipAddress)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            logger.error("Failed to save login activity log for user {}: {}", aspireUser.getUsername(), e.getMessage());
        }
    }

    /**
     * Infer UserType from AspireUser when userType field is null (e.g. legacy data).
     * Uses mspId/clientAdminId vs userId so ASPIRE_ADMIN and MSP get correct activity log.
     */
    private UserType inferUserTypeFromLoginUser(AspireUser aspireUser) {
        String userId = aspireUser.getUserId() != null ? aspireUser.getUserId().toString() : null;
        if (userId == null) {
            return null;
        }
        String mspId = aspireUser.getMspId();
        String clientAdminId = aspireUser.getClientAdminId();
        if (StringUtils.hasText(mspId) && mspId.equals(userId)) {
            return UserType.MSP;
        }
        if (StringUtils.hasText(clientAdminId) && clientAdminId.equals(userId)) {
            return UserType.CLIENT_ADMIN;
        }
        // userId present but no mspId/clientAdminId match -> likely Aspire Admin or Super Admin
        if (!StringUtils.hasText(mspId) && !StringUtils.hasText(clientAdminId)) {
            return UserType.ASPIRE_ADMIN;
        }
        return null;
    }

    private void saveLogoutActivityLog(CurrentUserContext currentUserContext) {
        try {
            String userId = currentUserContext.getUserId();
            String userTypeStr = currentUserContext.getUserType();
            // Normalize so "Aspire_Admin", "aspire_admin", "ASPIRE_ADMIN" all match
            String normalizedType = normalizeUserTypeString(userTypeStr);

            UserType userType = resolveUserType(userTypeStr);
            if (userType == null && StringUtils.hasText(userTypeStr)) {
                userType = inferUserTypeFromString(userTypeStr);
            }
            // When context has no userType, infer from IDs so ASPIRE_ADMIN/MSP are not saved as SYSTEM_USER
            if (userType == null && StringUtils.hasText(userId)) {
                userType = inferUserTypeFromLogoutContext(currentUserContext);
            }

            String ipAddress = getRemoteIPAddress();

            String aspireAdminId = null;
            String mspId = currentUserContext.getMspId();
            String clientAdminId = currentUserContext.getClientAdminId();
            String countryId = currentUserContext.getCountryId();

            // Use normalized type for ID assignment so all variants (ASPIRE_ADMIN, Aspire_Admin, SYSTEM_USER, etc.) are handled
            if (UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(normalizedType)
                    || UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(normalizedType)
                    || UserType.SYSTEM_USER.getValue().equalsIgnoreCase(normalizedType)) {
                aspireAdminId = userId;
            } else if (UserType.MSP.getValue().equalsIgnoreCase(normalizedType)) {
                mspId = (mspId == null || mspId.isEmpty()) ? userId : mspId;
            } else if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(normalizedType)) {
                clientAdminId = (clientAdminId == null || clientAdminId.isEmpty()) ? userId : clientAdminId;
            } else if (userType != null) {
                // userType inferred from context but not yet applied to IDs
                switch (userType) {
                    case ASPIRE_ADMIN:
                    case SUPER_ADMIN:
                    case SYSTEM_USER:
                        aspireAdminId = userId;
                        break;
                    case MSP:
                        mspId = (mspId == null || mspId.isEmpty()) ? userId : mspId;
                        break;
                    case CLIENT_ADMIN:
                        clientAdminId = (clientAdminId == null || clientAdminId.isEmpty()) ? userId : clientAdminId;
                        break;
                    default:
                        break;
                }
            }

            String fullName = currentUserContext.getFullName();

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(ActivityType.USER_LOGOUT)
                    .userId(userId)
                    .email(currentUserContext.getEmail())
                    .username(currentUserContext.getUsername())
                    .fullName(fullName)
                    .userType(userType != null ? userType : UserType.SYSTEM_USER)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .description("User " + currentUserContext.getUsername() + " logged out")
                    .ipAddress(ipAddress)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            logger.error("Failed to save logout activity log for user {}: {}", currentUserContext.getUsername(), e.getMessage());
        }
    }

    /**
     * Normalize userType string for consistent comparison (handles "Aspire_Admin", "aspire_admin", etc.).
     */
    private static String normalizeUserTypeString(String userTypeStr) {
        if (userTypeStr == null || userTypeStr.isBlank()) {
            return "";
        }
        return userTypeStr.trim().replace(' ', '_').toUpperCase();
    }

    /**
     * Infer UserType from logout context when userType header is null/missing (e.g. gateway/Redis not setting it).
     * Uses mspId/clientAdminId vs userId so ASPIRE_ADMIN and MSP still get correct activity log.
     */
    private UserType inferUserTypeFromLogoutContext(CurrentUserContext ctx) {
        String userId = ctx.getUserId();
        if (userId == null) {
            return null;
        }
        String mspId = ctx.getMspId();
        String clientAdminId = ctx.getClientAdminId();
        if (StringUtils.hasText(mspId) && mspId.equals(userId)) {
            return UserType.MSP;
        }
        if (StringUtils.hasText(clientAdminId) && clientAdminId.equals(userId)) {
            return UserType.CLIENT_ADMIN;
        }
        // userId present but no mspId/clientAdminId match -> likely Aspire Admin or Super Admin
        if (!StringUtils.hasText(mspId) && !StringUtils.hasText(clientAdminId)) {
            return UserType.ASPIRE_ADMIN;
        }
        return null;
    }

}
