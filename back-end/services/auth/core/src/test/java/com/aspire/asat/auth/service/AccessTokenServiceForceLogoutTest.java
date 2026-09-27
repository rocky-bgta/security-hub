package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.RefreshToken;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.logger.ServiceLogger;
import com.aspire.asat.auth.mapper.UserMapper;
import com.aspire.asat.auth.model.token.ForceLogoutRequest;
import com.aspire.asat.auth.model.token.LoginWithPasswordRequest;
import com.aspire.asat.auth.repository.RefreshTokenRepository;
import com.aspire.asat.auth.repository.RolePermissionRepository;
import com.aspire.asat.auth.repository.UserLoginHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.impl.PasswordResetServiceImpl;
import com.aspire.asat.common.client.ActivityLogClient;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceForceLogoutTest {

    @Mock private UserLoginHistoryRepository userLoginHistoryRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RedisService redisService;
    @Mock private UserMapper userMapper;
    @Mock private ActivityLogClient activityLogClient;
    @Mock private PasswordResetServiceImpl passwordResetService;
    @Mock private ServiceLogger serviceLogger;

    @InjectMocks
    private AccessTokenService accessTokenService;

    private AspireUser testUser;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        testUser = new AspireUser();
        testUser.setId(userId);
        testUser.setUserId(userId);
        testUser.setUsername("admin@example.com");
        testUser.setEmail("admin@example.com");
        testUser.setStatus("ACTIVE");
        testUser.setUserType(UserType.CLIENT_ADMIN.getValue());
        testUser.setPassword("encoded");

        setField(accessTokenService, "jwtSecret", "test-secret-key-that-is-long-enough-for-hs256");
        setField(accessTokenService, "jwtExpiration", 30L);
        setField(accessTokenService, "jwtRefreshExpiration", 1440L);
        accessTokenService.setLogger(serviceLogger);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void forceLogout_withUserId_revokesTokensWithoutCurrentContext() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUsernameAndIsRevokedFalse("admin@example.com"))
                .thenReturn(Collections.emptyList());

        Boolean result = accessTokenService.forceLogout(new ForceLogoutRequest().setUserId(userId.toString()));

        assertTrue(result);
        verify(redisService).revokeAllPreviousTokensForUser(userId.toString());
        verify(userLoginHistoryRepository, never()).save(any());
    }

    @Test
    void forceLogout_withUserId_revokesRefreshTokens() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUsernameAndIsRevokedFalse("admin@example.com"))
                .thenReturn(List.of(new RefreshToken().setToken("rt").setUsername("admin@example.com")));

        Boolean result = accessTokenService.forceLogout(new ForceLogoutRequest().setUserId(userId.toString()));

        assertTrue(result);
        verify(redisService).revokeAllPreviousTokensForUser(userId.toString());
        verify(refreshTokenRepository).deleteAll(any());
    }

    @Test
    void login_blocksClientAdminWithInactiveStatus() {
        testUser.setStatus("INACTIVE");
        when(userRepository.findByUsernameIgnoreCase("admin@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginWithPasswordRequest request = new LoginWithPasswordRequest();
        request.setUsername("admin@example.com");
        request.setPassword("password");

        assertThrows(UnauthorizedResourceException.class,
                () -> accessTokenService.loginWithUsernamePassword(request));
    }

    @Test
    void login_blocksUserWithSuspendStatus() {
        testUser.setUserType(UserType.USER.getValue());
        testUser.setStatus("SUSPEND");
        when(userRepository.findByUsernameIgnoreCase("admin@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginWithPasswordRequest request = new LoginWithPasswordRequest();
        request.setUsername("admin@example.com");
        request.setPassword("password");

        assertThrows(UnauthorizedResourceException.class,
                () -> accessTokenService.loginWithUsernamePassword(request));
    }

    @Test
    void isRestrictiveStatus_recognizesInactiveSuspendAndBlocked() {
        assertTrue(AccessTokenService.isRestrictiveStatus("INACTIVE"));
        assertTrue(AccessTokenService.isRestrictiveStatus("suspend"));
        assertTrue(AccessTokenService.isRestrictiveStatus("TEMPORARY_BLOCKED"));
        assertTrue(AccessTokenService.isRestrictiveStatus("BLOCKED"));
        assertFalse(AccessTokenService.isRestrictiveStatus("ACTIVE"));
    }
}
