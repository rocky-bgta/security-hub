package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.MfaCode;
import com.aspire.asat.auth.entity.UserMfaMethod;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.OtpExpiredException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.model.mfa.GenerateOtpRequest;
import com.aspire.asat.auth.model.mfa.GenerateOtpResponse;
import com.aspire.asat.auth.model.mfa.OtpIssueDecision;
import com.aspire.asat.auth.model.mfa.ResendOtpRequest;
import com.aspire.asat.auth.model.mfa.UserMfaMethodsResponse;
import com.aspire.asat.auth.model.mfa.VerifyOtpRequest;
import com.aspire.asat.auth.model.mfa.VerifyOtpResponse;
import com.aspire.asat.auth.repository.MfaCodeRepository;
import com.aspire.asat.auth.repository.UserMfaMethodRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.otp.MfaAuditService;
import com.aspire.asat.auth.service.otp.OtpSecurityService;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private MfaConfig mfaConfig;

    @Mock
    private MfaCodeRepository mfaCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMfaMethodRepository userMfaMethodRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private OtpSecurityService otpSecurityService;

    @Mock
    private MfaAuditService mfaAuditService;

    @InjectMocks
    private MfaService mfaService;

    private UUID userId;
    private AspireUser user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = AspireUser.builder()
                .id(userId)
                .userId(userId)
                .email("test@example.com")
                .firstName("Test")
                .userType("USER")
                .phoneNumber("+15551234567")
                .build();

        OtpIssueDecision decision = OtpIssueDecision.builder()
                .resendCooldownSeconds(60)
                .resendsRemaining(5)
                .attemptsRemaining(5)
                .countedAsResend(false)
                .resendCount(0)
                .build();
        lenient().when(otpSecurityService.authorizeIssue(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(decision);
        lenient().when(otpSecurityService.remainingLockSeconds(any())).thenReturn(java.util.Optional.empty());
        lenient().when(mfaCodeRepository.findByUserIdAndGeneratedByAndIsUsedFalse(any(), any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void generateOtp_SmsWithRequestPhone_UsesRequestPhone() {
        // Arrange
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .phoneNumber("+19998887777")
                .build();

        // Act
        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertEquals(300, response.getExpiresIn());
        assertEquals(60, response.getResendCooldownSeconds());
        assertEquals(5, response.getResendsRemaining());
        assertEquals(5, response.getAttemptsRemaining());

        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("+19998887777", captor.getValue().getPhoneNumber());
        assertEquals("+19998887777", captor.getValue().getTo());
        assertEquals("+19998887777", response.getPhoneNumber());

        ArgumentCaptor<MfaCode> codeCaptor = ArgumentCaptor.forClass(MfaCode.class);
        verify(mfaCodeRepository).save(codeCaptor.capture());
        assertEquals("+19998887777", codeCaptor.getValue().getPhoneNumber());
        assertEquals("+15551234567", user.getPhoneNumber());
    }

    @Test
    void generateOtp_SmsWithoutRequestPhone_UsesUserPhone() {
        // Arrange
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .build();

        // Act
        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());

        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("+15551234567", captor.getValue().getPhoneNumber());
        assertEquals("+15551234567", captor.getValue().getTo());
        assertEquals("+15551234567", response.getPhoneNumber());
    }

    @Test
    void generateOtp_SmsWithNeitherPhone_ThrowsBadRequest() {
        // Arrange
        user.setPhoneNumber(null);
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .build();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> mfaService.generateOtp(request, userId));
        verify(mfaCodeRepository, never()).save(any());
        verify(notificationClient, never()).sendNotification(any());
    }

    @Test
    void generateOtp_Email_DoesNotRequirePhone() {
        // Arrange
        user.setPhoneNumber(null);
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getEmailEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.EMAIL)
                .build();

        // Act
        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());

        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("test@example.com", captor.getValue().getTo());
        assertNull(response.getPhoneNumber());
    }

    @Test
    void resendOtp_SmsWithoutRequestPhone_UsesUserPhone() {
        stubResendCommon(MfaMethod.SMS);

        ResendOtpRequest request = ResendOtpRequest.builder()
                .method(MfaMethod.SMS)
                .build();

        GenerateOtpResponse response = mfaService.resendOtp(request, userId);

        assertNotNull(response.getSessionId());
        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("+15551234567", captor.getValue().getPhoneNumber());
        assertEquals(List.of(NotificationChannel.SMS), captor.getValue().getChannels());
    }

    @Test
    void resendOtp_Email_SendsToUserEmail() {
        stubResendCommon(MfaMethod.EMAIL);

        ResendOtpRequest request = ResendOtpRequest.builder()
                .method(MfaMethod.EMAIL)
                .build();

        GenerateOtpResponse response = mfaService.resendOtp(request, userId);

        assertNotNull(response.getSessionId());
        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("test@example.com", captor.getValue().getTo());
        assertEquals(List.of(NotificationChannel.EMAIL), captor.getValue().getChannels());
    }

    @Test
    void resendOtp_PhoneCallWithoutRequestPhone_UsesUserPhone() {
        stubResendCommon(MfaMethod.PHONE_CALL);

        ResendOtpRequest request = ResendOtpRequest.builder()
                .method(MfaMethod.PHONE_CALL)
                .build();

        GenerateOtpResponse response = mfaService.resendOtp(request, userId);

        assertNotNull(response.getSessionId());
        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("+15551234567", captor.getValue().getPhoneNumber());
        assertEquals(List.of(NotificationChannel.PHONE_CALL), captor.getValue().getChannels());
    }

    @Test
    void resendOtp_SmsWithNeitherPhone_ThrowsBadRequest() {
        user.setPhoneNumber(null);
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResendOtpRequest request = ResendOtpRequest.builder()
                .method(MfaMethod.SMS)
                .build();

        assertThrows(BadRequestException.class, () -> mfaService.resendOtp(request, userId));
        verify(notificationClient, never()).sendNotification(any());
    }

    @Test
    void verifyOtp_ExpiredCode_ThrowsOtpExpiredException() {
        UUID sessionId = UUID.randomUUID();
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.empty());

        MfaCode expiredCode = MfaCode.builder()
                .userId(userId)
                .sessionId(sessionId)
                .isUsed(false)
                .expiresAt(Instant.now().minusSeconds(60))
                .attemptCount(0)
                .build();
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalse(userId, sessionId))
                .thenReturn(Optional.of(expiredCode));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.SMS)
                .code("123456")
                .sessionId(sessionId)
                .build();

        OtpExpiredException exception = assertThrows(
                OtpExpiredException.class, () -> mfaService.verifyOtp(request, userId));
        assertEquals(OtpMessages.EXPIRED, exception.getMessage());
    }

    @Test
    void resendOtp_Authenticator_ThrowsBadRequest() {
        ResendOtpRequest request = ResendOtpRequest.builder()
                .method(MfaMethod.AUTHENTICATOR)
                .build();

        assertThrows(BadRequestException.class, () -> mfaService.resendOtp(request, userId));
        verify(notificationClient, never()).sendNotification(any());
    }

    @Test
    void verifyOtp_FirstMethod_EnrollsAsDefault() {
        UUID sessionId = stubValidOtp(sessionIdForVerify());
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.EMAIL)
                .code("123456")
                .sessionId(sessionId)
                .build();

        VerifyOtpResponse response = mfaService.verifyOtp(request, userId);

        assertTrue(response.getVerified());
        ArgumentCaptor<UserMfaMethod> captor = ArgumentCaptor.forClass(UserMfaMethod.class);
        verify(userMfaMethodRepository).save(captor.capture());
        assertEquals("EMAIL", captor.getValue().getMethod());
        assertTrue(captor.getValue().getIsDefault());
        assertTrue(user.getMfaEnabled());
    }

    @Test
    void verifyOtp_SecondMethod_KeepsExistingDefault() {
        UUID sessionId = stubValidOtp(sessionIdForVerify());
        UserMfaMethod email = enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(60));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(email));
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.SMS)
                .code("123456")
                .sessionId(sessionId)
                .build();

        mfaService.verifyOtp(request, userId);

        ArgumentCaptor<UserMfaMethod> captor = ArgumentCaptor.forClass(UserMfaMethod.class);
        verify(userMfaMethodRepository).save(captor.capture());
        assertEquals("SMS", captor.getValue().getMethod());
        assertFalse(captor.getValue().getIsDefault());
        assertTrue(email.getIsDefault());
    }

    @Test
    void verifyOtp_LoginChallenge_NonDefaultEnrolledMethod_SucceedsWithoutChangingDefault() {
        UUID sessionId = stubValidOtp(sessionIdForVerify());
        UserMfaMethod email = enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(120));
        UserMfaMethod sms = enrolled(MfaMethod.SMS, false, Instant.now().minusSeconds(60));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(email, sms));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.SMS)
                .code("123456")
                .sessionId(sessionId)
                .tempToken("temp-token")
                .build();

        VerifyOtpResponse response = mfaService.verifyOtp(request, userId);

        assertTrue(response.getVerified());
        verify(userMfaMethodRepository, never()).save(any(UserMfaMethod.class));
        assertTrue(email.getIsDefault());
        assertFalse(sms.getIsDefault());
    }

    @Test
    void verifyOtp_LoginChallenge_CannotEnrollUnenrolledMethod() {
        UUID sessionId = stubValidOtp(sessionIdForVerify());
        UserMfaMethod sms = enrolled(MfaMethod.SMS, true, Instant.now());
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(sms));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.EMAIL)
                .code("123456")
                .sessionId(sessionId)
                .tempToken("temp-token")
                .build();

        assertThrows(BadRequestException.class, () -> mfaService.verifyOtp(request, userId));
        verify(userMfaMethodRepository, never()).save(any(UserMfaMethod.class));
    }

    @Test
    void generateOtp_LoginChallenge_UnenrolledMethod_ThrowsBadRequest() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getEmailEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.SMS, true, Instant.now())));

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.EMAIL)
                .tempToken("temp-token")
                .build();

        assertThrows(BadRequestException.class, () -> mfaService.generateOtp(request, userId));
        verify(mfaCodeRepository, never()).save(any());
    }

    @Test
    void generateOtp_Jwt_AlreadyEnrolled_ThrowsBadRequest() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.SMS, true, Instant.now())));

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .build();

        assertThrows(BadRequestException.class, () -> mfaService.generateOtp(request, userId));
        verify(mfaCodeRepository, never()).save(any());
    }

    @Test
    void generateOtp_LoginChallenge_NonDefaultEnrolledMethod_Succeeds() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(
                enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(60)),
                enrolled(MfaMethod.SMS, false, Instant.now())));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .tempToken("temp-token")
                .build();

        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        assertNotNull(response.getSessionId());
        verify(mfaCodeRepository).save(any(MfaCode.class));
    }

    @Test
    void setDefaultMethod_SwitchesDefault() {
        UserMfaMethod email = enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(60));
        UserMfaMethod sms = enrolled(MfaMethod.SMS, false, Instant.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(email, sms));
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mfaService.setDefaultMethod(userId, MfaMethod.SMS);

        assertFalse(email.getIsDefault());
        assertTrue(sms.getIsDefault());
        verify(userMfaMethodRepository, times(2)).save(any(UserMfaMethod.class));
    }

    @Test
    void setDefaultMethod_Unenrolled_ThrowsBadRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.EMAIL, true, Instant.now())));

        assertThrows(BadRequestException.class, () -> mfaService.setDefaultMethod(userId, MfaMethod.SMS));
    }

    @Test
    void removeMethod_LastMethod_ThrowsBadRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.EMAIL, true, Instant.now())));

        assertThrows(BadRequestException.class, () -> mfaService.removeMethod(userId, MfaMethod.EMAIL));
        verify(userMfaMethodRepository, never()).delete(any());
    }

    @Test
    void removeMethod_Default_PromotesOldestRemaining() {
        Instant older = Instant.now().minusSeconds(120);
        Instant newer = Instant.now().minusSeconds(30);
        UserMfaMethod email = enrolled(MfaMethod.EMAIL, true, Instant.now());
        UserMfaMethod sms = enrolled(MfaMethod.SMS, false, older);
        UserMfaMethod authenticator = enrolled(MfaMethod.AUTHENTICATOR, false, newer);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(email, sms, authenticator));
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mfaService.removeMethod(userId, MfaMethod.EMAIL);

        verify(userMfaMethodRepository).delete(email);
        assertTrue(sms.getIsDefault());
        assertFalse(authenticator.getIsDefault());
        verify(userMfaMethodRepository).save(sms);
    }

    @Test
    void removeMethod_NotEnrolled_ThrowsNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.EMAIL, true, Instant.now())));

        assertThrows(ResourceNotFoundException.class, () -> mfaService.removeMethod(userId, MfaMethod.SMS));
    }

    @Test
    void ensureMigrated_CopiesLegacyUserFields() {
        user.setMfaEnabled(true);
        user.setMfaMethod("SMS");
        user.setMfaSecret(null);
        user.setMfaSetupAt(Instant.parse("2024-01-01T00:00:00Z"));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<UserMfaMethod> migrated = mfaService.ensureMigrated(user);

        assertEquals(1, migrated.size());
        assertEquals("SMS", migrated.get(0).getMethod());
        assertTrue(migrated.get(0).getIsDefault());
        assertEquals(user.getMfaSetupAt(), migrated.get(0).getCreatedAt());
        verify(userMfaMethodRepository).save(any(UserMfaMethod.class));
    }

    @Test
    void ensureMigrated_CopiesAuthenticatorSecret() {
        user.setMfaEnabled(true);
        user.setMfaMethod("AUTHENTICATOR");
        user.setMfaSecret("encrypted-totp-secret");
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<UserMfaMethod> migrated = mfaService.ensureMigrated(user);

        assertEquals("AUTHENTICATOR", migrated.get(0).getMethod());
        assertEquals("encrypted-totp-secret", migrated.get(0).getSecret());
        assertTrue(migrated.get(0).getIsDefault());
    }

    @Test
    void needsMfaSetup_WhenNoMethods_True() {
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        assertTrue(mfaService.needsMfaSetup(user));
        assertFalse(mfaService.needsMfaVerification(user));
    }

    @Test
    void needsMfaVerification_WhenMethodsExist_True() {
        when(userMfaMethodRepository.findByUserId(userId))
                .thenReturn(List.of(enrolled(MfaMethod.EMAIL, true, Instant.now())));
        assertFalse(mfaService.needsMfaSetup(user));
        assertTrue(mfaService.needsMfaVerification(user));
        assertEquals("EMAIL", mfaService.getDefaultMethodName(user));
        assertEquals(1, mfaService.getEnrolledMethods(user).size());
        assertEquals("EMAIL", mfaService.getEnrolledMethods(user).get(0).getMethod());
        assertTrue(mfaService.getEnrolledMethods(user).get(0).getIsDefault());
    }

    @Test
    void listUserMethods_ReturnsDefaultFirst() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(
                enrolled(MfaMethod.SMS, false, Instant.now(), "+19998887777"),
                enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(10))));

        UserMfaMethodsResponse response = mfaService.listUserMethods(userId);

        assertEquals("EMAIL", response.getMethods().get(0).getMethod());
        assertTrue(response.getMethods().get(0).getIsDefault());
        assertNull(response.getMethods().get(0).getPhoneNumber());
        assertEquals("SMS", response.getMethods().get(1).getMethod());
        assertEquals("+19998887777", response.getMethods().get(1).getPhoneNumber());
    }

    @Test
    void verifyOtp_JwtAddMethod_PersistsPhoneOnMethod_DoesNotChangeProfile() {
        UUID sessionId = stubValidOtp(sessionIdForVerify(), "+19998887777");
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMfaMethodRepository.save(any(UserMfaMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .method(MfaMethod.SMS)
                .code("123456")
                .sessionId(sessionId)
                .build();

        VerifyOtpResponse response = mfaService.verifyOtp(request, userId);

        assertTrue(response.getVerified());
        ArgumentCaptor<UserMfaMethod> captor = ArgumentCaptor.forClass(UserMfaMethod.class);
        verify(userMfaMethodRepository).save(captor.capture());
        assertEquals("SMS", captor.getValue().getMethod());
        assertEquals("+19998887777", captor.getValue().getPhoneNumber());
        assertEquals("+15551234567", user.getPhoneNumber());
    }

    @Test
    void generateOtp_LoginChallenge_UsesEnrolledPhone_IgnoresRequestPhone() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(
                enrolled(MfaMethod.SMS, true, Instant.now(), "+19998887777")));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .phoneNumber("+10000000000")
                .tempToken("temp-token")
                .build();

        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        assertEquals("+19998887777", response.getPhoneNumber());
        ArgumentCaptor<NotificationRequestDto> notifyCaptor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(notifyCaptor.capture());
        assertEquals("+19998887777", notifyCaptor.getValue().getPhoneNumber());
        ArgumentCaptor<MfaCode> codeCaptor = ArgumentCaptor.forClass(MfaCode.class);
        verify(mfaCodeRepository).save(codeCaptor.capture());
        assertEquals("+19998887777", codeCaptor.getValue().getPhoneNumber());
        assertEquals("+15551234567", user.getPhoneNumber());
    }

    @Test
    void generateOtp_LaterSmsWithoutRequestPhone_UsesEnrolledMfaPhoneNotProfile() {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getSmsEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of(
                enrolled(MfaMethod.EMAIL, true, Instant.now().minusSeconds(60)),
                enrolled(MfaMethod.SMS, false, Instant.now(), "+19998887777")));
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);

        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .method(MfaMethod.SMS)
                .tempToken("temp-token")
                .build();

        GenerateOtpResponse response = mfaService.generateOtp(request, userId);

        assertEquals("+19998887777", response.getPhoneNumber());
        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationClient).sendNotification(captor.capture());
        assertEquals("+19998887777", captor.getValue().getPhoneNumber());
        assertEquals("+15551234567", user.getPhoneNumber());
    }

    private UUID sessionIdForVerify() {
        return UUID.randomUUID();
    }

    private UUID stubValidOtp(UUID sessionId) {
        return stubValidOtp(sessionId, null);
    }

    private UUID stubValidOtp(UUID sessionId, String phoneNumber) {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getMaxAttempts()).thenReturn(5);
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        MfaCode code = MfaCode.builder()
                .userId(userId)
                .sessionId(sessionId)
                .isUsed(false)
                .codeHash("hashed-otp")
                .expiresAt(Instant.now().plusSeconds(60))
                .attemptCount(0)
                .phoneNumber(phoneNumber)
                .build();
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.of(code));
        org.mockito.Mockito.lenient().when(mfaCodeRepository.save(any(MfaCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return sessionId;
    }

    private UserMfaMethod enrolled(MfaMethod method, boolean isDefault, Instant createdAt) {
        return enrolled(method, isDefault, createdAt, null);
    }

    private UserMfaMethod enrolled(MfaMethod method, boolean isDefault, Instant createdAt, String phoneNumber) {
        return UserMfaMethod.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .method(method.name())
                .phoneNumber(phoneNumber)
                .isDefault(isDefault)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private void stubResendCommon(MfaMethod method) {
        when(mfaConfig.getEnabled()).thenReturn(true);
        when(mfaConfig.getOtpLength()).thenReturn(6);
        when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        switch (method) {
            case SMS -> when(mfaConfig.getSmsEnabled()).thenReturn(true);
            case EMAIL -> when(mfaConfig.getEmailEnabled()).thenReturn(true);
            case PHONE_CALL -> when(mfaConfig.getPhoneCallEnabled()).thenReturn(true);
            default -> {
            }
        }
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mfaCodeRepository.findByUserIdAndGeneratedByAndIsUsedFalse(eq(userId), eq(method.name())))
                .thenReturn(Collections.emptyList());
        when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);
    }
}
