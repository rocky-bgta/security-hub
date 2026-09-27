package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.UserMfaMethod;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.OtpInvalidException;
import com.aspire.asat.auth.model.mfa.AuthenticatorSetupResponse;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.util.Base32Util;
import com.aspire.asat.auth.util.EncryptionUtil;
import com.aspire.asat.auth.util.TotpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotpServiceTest {

    @Mock
    private MfaConfig mfaConfig;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MfaService mfaService;

    @InjectMocks
    private TotpService totpService;

    private UUID userId;
    private AspireUser user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = AspireUser.builder()
                .id(userId)
                .userId(userId)
                .email("test@example.com")
                .username("test@example.com")
                .build();
    }

    @Test
    void setupAuthenticator_WhenNotEnrolled_ReturnsSecret() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getAuthenticatorEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)).thenReturn(false);

        AuthenticatorSetupResponse response = totpService.setupAuthenticator(userId);

        assertNotNull(response.getSecret());
        assertNotNull(response.getOtpauthUri());
        assertTrue(response.getOtpauthUri().contains(response.getSecret()));
    }

    @Test
    void setupAuthenticator_WhenAlreadyEnrolled_ThrowsBadRequest() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getAuthenticatorEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> totpService.setupAuthenticator(userId));
    }

    @Test
    void setupAuthenticator_AllowedWhenOtherMethodsEnrolled() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getAuthenticatorEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)).thenReturn(false);

        AuthenticatorSetupResponse response = totpService.setupAuthenticator(userId);

        assertNotNull(response.getSecret());
        verify(mfaService).isMethodEnrolled(user, MfaMethod.AUTHENTICATOR);
    }

    @Test
    void verifyAuthenticatorSetup_EnrollsWithoutReplacingOtherMethods() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getAuthenticatorEnabled()).thenReturn(true);
        when(mfaConfig.getEncryptionKey()).thenReturn("ASAT-MFA-TEST-KEY");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)).thenReturn(false);

        AuthenticatorSetupResponse setup = totpService.setupAuthenticator(userId);
        String code = TotpUtil.generateTotp(setup.getSecret());

        totpService.verifyAuthenticatorSetup(userId, code);

        verify(mfaService).enrollMethod(eq(user), eq(MfaMethod.AUTHENTICATOR), any());
    }

    @Test
    void verifyAuthenticatorSetup_AlreadyEnrolled_ThrowsBadRequest() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getAuthenticatorEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR))
                .thenReturn(false)
                .thenReturn(true);

        AuthenticatorSetupResponse setup = totpService.setupAuthenticator(userId);
        String code = TotpUtil.generateTotp(setup.getSecret());

        assertThrows(BadRequestException.class, () -> totpService.verifyAuthenticatorSetup(userId, code));
        verify(mfaService, never()).enrollMethod(any(), any(), any());
    }

    @Test
    void verifyAuthenticatorSetup_NoSetupInProgress_Throws() {
        assertThrows(OtpInvalidException.class, () -> totpService.verifyAuthenticatorSetup(userId, "123456"));
    }

    @Test
    void verifyTotpCode_UsesEnrolledAuthenticatorSecret() {
        byte[] secretBytes = new byte[20];
        new SecureRandom().nextBytes(secretBytes);
        String secret = Base32Util.encode(secretBytes);
        String key = EncryptionUtil.normalizeKey("ASAT-MFA-TEST-KEY");
        String encrypted = EncryptionUtil.encrypt(secret, key);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaConfig.getEncryptionKey()).thenReturn("ASAT-MFA-TEST-KEY");
        when(mfaService.getEnrolledMethod(user, MfaMethod.AUTHENTICATOR))
                .thenReturn(UserMfaMethod.builder()
                        .userId(userId)
                        .method("AUTHENTICATOR")
                        .secret(encrypted)
                        .isDefault(false)
                        .build());

        String code = TotpUtil.generateTotp(secret);

        assertTrue(totpService.verifyTotpCode(userId, code));
    }

    @Test
    void verifyTotpCode_NotEnrolled_Throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaService.getEnrolledMethod(user, MfaMethod.AUTHENTICATOR)).thenReturn(null);

        assertThrows(OtpInvalidException.class, () -> totpService.verifyTotpCode(userId, "123456"));
    }
}
