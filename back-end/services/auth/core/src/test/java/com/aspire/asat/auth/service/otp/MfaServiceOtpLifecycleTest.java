package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.MfaAuditEventType;
import com.aspire.asat.auth.dto.enums.MfaCodeStatus;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.MfaCode;
import com.aspire.asat.auth.exception.OtpCooldownException;
import com.aspire.asat.auth.exception.OtpInvalidException;
import com.aspire.asat.auth.exception.OtpLockedException;
import com.aspire.asat.auth.exception.OtpRateLimitException;
import com.aspire.asat.auth.model.mfa.GenerateOtpRequest;
import com.aspire.asat.auth.model.mfa.GenerateOtpResponse;
import com.aspire.asat.auth.model.mfa.OtpIssueDecision;
import com.aspire.asat.auth.model.mfa.ResendOtpRequest;
import com.aspire.asat.auth.model.mfa.VerifyOtpRequest;
import com.aspire.asat.auth.repository.MfaCodeRepository;
import com.aspire.asat.auth.repository.UserMfaMethodRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.MfaService;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaServiceOtpLifecycleTest {

    @Mock private MfaConfig mfaConfig;
    @Mock private MfaCodeRepository mfaCodeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserMfaMethodRepository userMfaMethodRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationClient notificationClient;
    @Mock private OtpSecurityService otpSecurityService;
    @Mock private MfaAuditService mfaAuditService;
    @InjectMocks private MfaService mfaService;

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
                .build();
        lenient().when(mfaConfig.getEnabled()).thenReturn(true);
        lenient().when(mfaConfig.getEmailEnabled()).thenReturn(true);
        lenient().when(mfaConfig.getOtpLength()).thenReturn(6);
        lenient().when(mfaConfig.getOtpValiditySeconds()).thenReturn(300);
        lenient().when(mfaConfig.getMaxAttempts()).thenReturn(5);
        lenient().when(mfaConfig.getLockDurationSeconds()).thenReturn(900);
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        lenient().when(userMfaMethodRepository.findByUserId(userId)).thenReturn(List.of());
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
        lenient().when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(notificationClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(true);
        lenient().when(otpSecurityService.authorizeIssue(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(OtpIssueDecision.builder()
                        .resendCooldownSeconds(60)
                        .resendsRemaining(5)
                        .attemptsRemaining(5)
                        .countedAsResend(false)
                        .resendCount(0)
                        .build());
        lenient().when(otpSecurityService.remainingLockSeconds(userId)).thenReturn(Optional.of(900));
    }

    @Test
    void generateOtp_InvalidatesPreviousUnusedCodes() {
        MfaCode previous = MfaCode.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .isUsed(false)
                .status(MfaCodeStatus.ACTIVE.name())
                .generatedBy("EMAIL")
                .build();
        when(mfaCodeRepository.findByUserIdAndGeneratedByAndIsUsedFalse(userId, "EMAIL"))
                .thenReturn(List.of(previous));

        GenerateOtpResponse response = mfaService.generateOtp(
                GenerateOtpRequest.builder().method(MfaMethod.EMAIL).build(), userId);

        assertEquals(60, response.getResendCooldownSeconds());
        assertEquals(5, response.getResendsRemaining());
        assertEquals(5, response.getAttemptsRemaining());
        assertTrue(previous.getIsUsed());
        assertEquals(MfaCodeStatus.INVALIDATED.name(), previous.getStatus());
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_INVALIDATED), eq(userId), eq("EMAIL"),
                any(), any(), eq(MfaAuditService.OUTCOME_SUCCESS), any(), any());
    }

    @Test
    void generateOtp_WhenLocked_DoesNotIssueCode() {
        doThrow(new OtpLockedException(900))
                .when(otpSecurityService).authorizeIssue(any(), any(), any(), any(), anyBoolean());

        assertThrows(OtpLockedException.class, () -> mfaService.generateOtp(
                GenerateOtpRequest.builder().method(MfaMethod.EMAIL).build(), userId));
        verify(mfaCodeRepository, never()).save(any());
        verify(notificationClient, never()).sendNotification(any());
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_LOCKED), eq(userId), eq("EMAIL"),
                any(), any(), eq(MfaAuditService.OUTCOME_DENIED), any(), any());
    }

    @Test
    void resendOtp_WhenCooldownActive_DoesNotIssueCode() {
        doThrow(new OtpCooldownException(45))
                .when(otpSecurityService).authorizeIssue(any(), any(), any(), any(), eq(true));

        assertThrows(OtpCooldownException.class, () -> mfaService.resendOtp(
                ResendOtpRequest.builder().method(MfaMethod.EMAIL).build(), userId));
        verify(notificationClient, never()).sendNotification(any());
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_COOLDOWN_BLOCKED), eq(userId), eq("EMAIL"),
                any(), any(), eq(MfaAuditService.OUTCOME_DENIED), any(), any());
    }

    @Test
    void resendOtp_WhenHourlyCapExceeded_DoesNotIssueCode() {
        doThrow(new OtpRateLimitException(3600))
                .when(otpSecurityService).authorizeIssue(any(), any(), any(), any(), eq(true));

        assertThrows(OtpRateLimitException.class, () -> mfaService.resendOtp(
                ResendOtpRequest.builder().method(MfaMethod.EMAIL).build(), userId));
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_RATE_LIMITED), eq(userId), eq("EMAIL"),
                any(), any(), eq(MfaAuditService.OUTCOME_DENIED), any(), any());
    }

    @Test
    void verifyOtp_FifthFailure_LocksAndInvalidates() {
        UUID sessionId = UUID.randomUUID();
        MfaCode code = activeCode(sessionId, 4);
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "hashed-otp")).thenReturn(false);

        OtpLockedException ex = assertThrows(OtpLockedException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("000000").sessionId(sessionId).build(),
                userId));

        assertEquals(OtpMessages.LOCKED, ex.getMessage());
        assertEquals(MfaCodeStatus.INVALIDATED.name(), code.getStatus());
        assertTrue(code.getIsUsed());
        verify(otpSecurityService).lockUser(userId);
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_LOCKED), eq(userId), eq("EMAIL"),
                eq(sessionId), any(), eq(MfaAuditService.OUTCOME_DENIED), any(), any());
    }

    @Test
    void verifyOtp_InvalidCode_DoesNotLockBeforeMaxAttempts() {
        UUID sessionId = UUID.randomUUID();
        MfaCode code = activeCode(sessionId, 1);
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "hashed-otp")).thenReturn(false);

        OtpInvalidException ex = assertThrows(OtpInvalidException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("000000").sessionId(sessionId).build(),
                userId));

        assertEquals(OtpMessages.INVALID, ex.getMessage());
        assertEquals(2, code.getAttemptCount());
        verify(otpSecurityService, never()).lockUser(any());
    }

    @Test
    void verifyOtp_Expired_DoesNotConsumeAttempt() {
        UUID sessionId = UUID.randomUUID();
        MfaCode code = activeCode(sessionId, 2);
        code.setExpiresAt(Instant.now().minusSeconds(5));
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.empty());
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalse(userId, sessionId))
                .thenReturn(Optional.of(code));

        assertThrows(com.aspire.asat.auth.exception.OtpExpiredException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("123456").sessionId(sessionId).build(),
                userId));

        assertEquals(2, code.getAttemptCount());
        assertEquals(MfaCodeStatus.EXPIRED.name(), code.getStatus());
        verify(otpSecurityService, never()).lockUser(any());
    }

    @Test
    void verifyOtp_UsedCode_IsRejectedAsInvalid() {
        UUID sessionId = UUID.randomUUID();
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.empty());
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalse(userId, sessionId))
                .thenReturn(Optional.empty());

        OtpInvalidException ex = assertThrows(OtpInvalidException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("123456").sessionId(sessionId).build(),
                userId));
        assertEquals(OtpMessages.INVALID, ex.getMessage());
    }

    @Test
    void verifyOtp_Success_MarksUsedAndClearsLifecycle() {
        UUID sessionId = UUID.randomUUID();
        MfaCode code = activeCode(sessionId, 0);
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.of(code));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);

        mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("123456").sessionId(sessionId).build(),
                userId);

        assertTrue(code.getIsUsed());
        assertEquals(MfaCodeStatus.USED.name(), code.getStatus());
        verify(otpSecurityService).recordSuccessfulVerify(userId, MfaMethod.EMAIL);
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_VERIFY_SUCCESS), eq(userId), eq("EMAIL"),
                eq(sessionId), any(), eq(MfaAuditService.OUTCOME_SUCCESS), any(), any());
    }

    @Test
    void verifyOtp_WhenLocked_DoesNotAttemptMatch() {
        doThrow(new OtpLockedException(400)).when(otpSecurityService).assertVerifyAllowed(eq(userId), any());

        assertThrows(OtpLockedException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("123456").sessionId(UUID.randomUUID()).build(),
                userId));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void resendOtp_ReturnsCooldownFields() {
        when(otpSecurityService.authorizeIssue(any(), any(), any(), any(), eq(true)))
                .thenReturn(OtpIssueDecision.builder()
                        .resendCooldownSeconds(120)
                        .resendsRemaining(4)
                        .attemptsRemaining(5)
                        .countedAsResend(true)
                        .resendCount(1)
                        .build());
        when(mfaCodeRepository.findByUserIdAndGeneratedByAndIsUsedFalse(userId, "EMAIL"))
                .thenReturn(List.of());

        GenerateOtpResponse response = mfaService.resendOtp(
                ResendOtpRequest.builder().method(MfaMethod.EMAIL).build(), userId);

        assertEquals(120, response.getResendCooldownSeconds());
        assertEquals(4, response.getResendsRemaining());
        verify(mfaAuditService).record(eq(MfaAuditEventType.OTP_RESENT), eq(userId), eq("EMAIL"),
                any(), any(), eq(MfaAuditService.OUTCOME_SUCCESS), any(), any());
        verify(otpSecurityService).recordActiveSession(eq(userId), eq(MfaMethod.EMAIL), any());
        verify(mfaCodeRepository, times(1)).save(any(MfaCode.class));
    }

    @Test
    void verifyOtp_AlreadyAtMaxAttempts_LocksWithoutMatching() {
        UUID sessionId = UUID.randomUUID();
        MfaCode code = activeCode(sessionId, 5);
        when(mfaCodeRepository.findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(
                eq(userId), eq(sessionId), any(Instant.class)))
                .thenReturn(Optional.of(code));

        assertThrows(OtpLockedException.class, () -> mfaService.verifyOtp(
                VerifyOtpRequest.builder().method(MfaMethod.EMAIL).code("123456").sessionId(sessionId).build(),
                userId));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(otpSecurityService).lockUser(userId);
    }

    private MfaCode activeCode(UUID sessionId, int attemptCount) {
        return MfaCode.builder()
                .userId(userId)
                .sessionId(sessionId)
                .isUsed(false)
                .codeHash("hashed-otp")
                .expiresAt(Instant.now().plusSeconds(60))
                .attemptCount(attemptCount)
                .status(MfaCodeStatus.ACTIVE.name())
                .build();
    }
}
