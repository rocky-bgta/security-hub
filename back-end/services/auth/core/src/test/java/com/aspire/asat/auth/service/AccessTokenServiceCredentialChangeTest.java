package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.UserLoginHistory;
import com.aspire.asat.auth.logger.ServiceLogger;
import com.aspire.asat.auth.mapper.UserMapper;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceCredentialChangeTest {

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
        testUser.setPassword("encoded");
        testUser.setRoles(List.of());

        setField(accessTokenService, "jwtSecret", "test-secret-key-that-is-long-enough-for-hs256");
        setField(accessTokenService, "jwtExpiration", 30L);
        setField(accessTokenService, "jwtRefreshExpiration", 1440L);
        accessTokenService.setLogger(serviceLogger);
    }

    @ParameterizedTest
    @EnumSource(value = UserType.class, names = {"MSP", "CLIENT_ADMIN", "ASPIRE_ADMIN", "SUPER_ADMIN", "SYSTEM_USER"})
    void requiresForcedCredentialChange_trueForScopedTypesWithIsDefault(UserType userType) throws Exception {
        testUser.setUserType(userType.getValue());
        testUser.setIsDefault(true);

        assertTrue(invokeRequiresForcedCredentialChange(testUser));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CLIENT"})
    void requiresForcedCredentialChange_falseForNonScopedTypesEvenWithIsDefault(String userType) throws Exception {
        testUser.setUserType(userType);
        testUser.setIsDefault(true);

        assertFalse(invokeRequiresForcedCredentialChange(testUser));
    }

    @Test
    void requiresForcedCredentialChange_falseWhenIsDefaultFalse() throws Exception {
        testUser.setUserType(UserType.MSP.getValue());
        testUser.setIsDefault(false);

        assertFalse(invokeRequiresForcedCredentialChange(testUser));
    }

    @Test
    void requiresForcedCredentialChange_falseWhenIsDefaultFalseEvenIfBuyNow() throws Exception {
        testUser.setUserType(UserType.CLIENT_ADMIN.getValue());
        testUser.setIsDefault(false);
        testUser.setIsBuyNow(true);

        assertFalse(invokeRequiresForcedCredentialChange(testUser));
    }

    @Test
    void login_issuesTokens_forBuyNowClientAdmin() {
        testUser.setUserType(UserType.CLIENT_ADMIN.getValue());
        testUser.setIsDefault(false);
        testUser.setIsBuyNow(true);
        stubSuccessfulLogin();

        TokenShortResponse response = accessTokenService.loginWithUsernamePassword(loginRequest());

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void login_issuesTokens_forMspWithDefaultPassword() {
        // Forced credential-change gate on login response is currently disabled in AccessTokenService.
        testUser.setUserType(UserType.MSP.getValue());
        testUser.setIsDefault(true);
        stubSuccessfulLogin();

        TokenShortResponse response = accessTokenService.loginWithUsernamePassword(loginRequest());

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void login_issuesTokens_forEndUserWithDefaultPassword() {
        testUser.setUserType(UserType.USER.getValue());
        testUser.setIsDefault(true);
        stubSuccessfulLogin();

        TokenShortResponse response = accessTokenService.loginWithUsernamePassword(loginRequest());

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void login_issuesTokens_forClientAdminAfterPasswordChanged() {
        testUser.setUserType(UserType.CLIENT_ADMIN.getValue());
        testUser.setIsDefault(false);
        stubSuccessfulLogin();

        TokenShortResponse response = accessTokenService.loginWithUsernamePassword(loginRequest());

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    private void stubSuccessfulLogin() {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setUserId(userId.toString());
        tokenResponse.setUserType(testUser.getUserType());
        tokenResponse.setRoles(Collections.emptyList());
        tokenResponse.setCredentialChangeNeeded(false);

        when(userRepository.findByUsernameIgnoreCase("admin@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userMapper.mapToLoginHistoryEntity(any(), any(), any(), any())).thenReturn(new UserLoginHistory());
        when(userLoginHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.mapToTokenResponse(any())).thenReturn(tokenResponse);
        when(userLoginHistoryRepository.findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private LoginWithPasswordRequest loginRequest() {
        LoginWithPasswordRequest request = new LoginWithPasswordRequest();
        request.setUsername("admin@example.com");
        request.setPassword("password");
        return request;
    }

    private boolean invokeRequiresForcedCredentialChange(AspireUser user) throws Exception {
        Method method = AccessTokenService.class.getDeclaredMethod("requiresForcedCredentialChange", AspireUser.class);
        method.setAccessible(true);
        return (boolean) method.invoke(accessTokenService, user);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
