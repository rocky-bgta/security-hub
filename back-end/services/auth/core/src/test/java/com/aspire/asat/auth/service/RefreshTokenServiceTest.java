package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.RefreshToken;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.logger.ServiceLogger;
import com.aspire.asat.auth.mapper.UserMapper;
import com.aspire.asat.auth.model.token.RefreshTokenRequest;
import com.aspire.asat.auth.repository.RefreshTokenRepository;
import com.aspire.asat.auth.repository.RolePermissionRepository;
import com.aspire.asat.auth.repository.UserLoginHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.impl.PasswordResetServiceImpl;
import com.aspire.asat.auth.util.JWTUtils;
import com.aspire.asat.common.client.ActivityLogClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final String JWT_SECRET = "test-secret-key-that-is-long-enough-for-hs256";

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
    private RefreshToken testRefreshToken;
    private RefreshTokenRequest testRequest;
    private String validRefreshJwt;

    @BeforeEach
    void setUp() throws Exception {
        UUID userId = UUID.randomUUID();
        testUser = new AspireUser();
        testUser.setId(userId);
        testUser.setUserId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus("ACTIVE");
        testUser.setRoles(List.of());

        setField(accessTokenService, "jwtSecret", JWT_SECRET);
        setField(accessTokenService, "jwtExpiration", 30L);
        setField(accessTokenService, "jwtRefreshExpiration", 1440L);
        accessTokenService.setLogger(serviceLogger);

        validRefreshJwt = JWTUtils.generateRefreshToken("testuser", "1440", JWT_SECRET);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken(validRefreshJwt);
        testRefreshToken.setUsername("testuser");
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        testRefreshToken.setRevoked(false);

        testRequest = new RefreshTokenRequest();
        testRequest.setRefreshToken(validRefreshJwt);
    }

    @Test
    void testRefreshTokenSuccess() {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setUserId(testUser.getUserId().toString());
        tokenResponse.setRoles(Collections.emptyList());

        when(refreshTokenRepository.findByTokenAndIsRevokedFalse(validRefreshJwt))
                .thenReturn(Optional.of(testRefreshToken));
        when(userRepository.findByUsernameIgnoreCase("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.findByUserId(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(userMapper.mapToTokenResponse(any())).thenReturn(tokenResponse);
        when(userLoginHistoryRepository.findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TokenShortResponse response = accessTokenService.refreshToken(testRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    void testRefreshTokenNotFound() {
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse(validRefreshJwt))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> accessTokenService.refreshToken(testRequest));
    }

    @Test
    void testRefreshTokenExpired() {
        testRefreshToken.setExpiryDate(Instant.now().minusSeconds(3600));
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse(validRefreshJwt))
                .thenReturn(Optional.of(testRefreshToken));

        assertThrows(BadRequestException.class, () -> accessTokenService.refreshToken(testRequest));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
