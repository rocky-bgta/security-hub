package com.aspire.asat.auth.service.impl;

import com.aspire.asat.auth.client.AuthNotificationClient;
import com.aspire.asat.auth.dto.PasswordResetRequestDto;
import com.aspire.asat.auth.dto.ResetPasswordDto;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.password.PasswordResetToken;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.repository.EndUserPackageRepository;
import com.aspire.asat.auth.repository.PasswordResetTokenRepository;
import com.aspire.asat.auth.repository.UserPasswordHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.OrgTimezoneService;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.aspire.asat.auth.service.impl.PasswordResetServiceImpl.INACTIVE_USER_RESET_MESSAGE;
import static com.aspire.asat.auth.service.impl.PasswordResetServiceImpl.NO_COURSE_ASSIGNED_RESET_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private UserPasswordHistoryRepository passwordHistoryRepository;
    @Mock private EndUserPackageRepository endUserPackageRepository;
    @Mock private AuthNotificationClient authNotificationClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ActivityLogClient activityLogClient;
    @Mock private OrgTimezoneService orgTimezoneService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private AspireUser endUser;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        endUser = new AspireUser();
        endUser.setUserId(userId);
        endUser.setId(userId);
        endUser.setUsername("user@example.com");
        endUser.setEmail("user@example.com");
        endUser.setFirstName("Test");
        endUser.setLastName("User");
        endUser.setUserType(UserType.USER.getValue());
        endUser.setStatus("ACTIVE");

        setField(passwordResetService, "tokenExpirySeconds", 3600L);
        setField(passwordResetService, "setTokenExpirySeconds", 28800L);
        setField(passwordResetService, "resetBaseUrl", "http://localhost/reset");
        setField(passwordResetService, "passwordHistoryCount", 10);
        setField(passwordResetService, "passwordHistoryMessageCount", 5);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void requestPasswordReset_inactiveUser_throwsAndDoesNotSaveToken() {
        endUser.setStatus("INACTIVE");
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.of(endUser));

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setUsername("user@example.com");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> passwordResetService.requestPasswordReset(request));

        assertEquals(INACTIVE_USER_RESET_MESSAGE, ex.getMessage());
        verify(tokenRepository, never()).save(any());
        verify(endUserPackageRepository, never()).existsByUserIdAndActiveTrue(anyString());
    }

    @Test
    void requestPasswordReset_suspendUser_withoutCourse_throwsNoCourseMessage() {
        // SUSPEND is not treated as inactive for password-reset eligibility;
        // end users still require an active course assignment.
        endUser.setStatus("SUSPEND");
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.of(endUser));
        when(endUserPackageRepository.existsByUserIdAndActiveTrue(userId.toString())).thenReturn(false);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setUsername("user@example.com");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> passwordResetService.requestPasswordReset(request));

        assertEquals(NO_COURSE_ASSIGNED_RESET_MESSAGE, ex.getMessage());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestPasswordReset_activeUserWithoutCourse_throws() {
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.of(endUser));
        when(endUserPackageRepository.existsByUserIdAndActiveTrue(userId.toString())).thenReturn(false);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setUsername("user@example.com");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> passwordResetService.requestPasswordReset(request));

        assertEquals(NO_COURSE_ASSIGNED_RESET_MESSAGE, ex.getMessage());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestPasswordReset_activeUserWithCourse_succeeds() {
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.of(endUser));
        when(endUserPackageRepository.existsByUserIdAndActiveTrue(userId.toString())).thenReturn(true);
        when(tokenRepository.findByUsernameAndUsedFalse("user@example.com")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setUsername("user@example.com");

        passwordResetService.requestPasswordReset(request);

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(authNotificationClient).sendPasswordResetNotification(
                anyString(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void requestPasswordReset_activeClientAdmin_skipsCourseCheck() {
        AspireUser admin = new AspireUser();
        admin.setUserId(UUID.randomUUID());
        admin.setUsername("admin@example.com");
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setUserType(UserType.CLIENT_ADMIN.getValue());
        admin.setStatus("ACTIVE");

        when(userRepository.findByUsernameIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(tokenRepository.findByUsernameAndUsedFalse("admin@example.com")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setUsername("admin@example.com");

        passwordResetService.requestPasswordReset(request);

        verify(endUserPackageRepository, never()).existsByUserIdAndActiveTrue(anyString());
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_rejectsWhenUserBecameInactive() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("valid-token")
                .username("user@example.com")
                .expiryDate(Instant.now().plusSeconds(3600))
                .used(false)
                .build();
        endUser.setStatus("INACTIVE");

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.of(endUser));

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken("valid-token");
        dto.setNewPassword("NewPass1!");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> passwordResetService.resetPassword(dto));

        assertEquals(INACTIVE_USER_RESET_MESSAGE, ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
