package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsResponseDto;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.repository.NotificationRoleSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRoleSettingsServiceTest {

    @Mock
    private NotificationRoleSettingsRepository repository;

    @InjectMocks
    private NotificationRoleSettingsService service;

    @Test
    void isEnabledForRole_absentRow_defaultsToAllowed() {
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.empty());

        assertTrue(service.isEnabledForRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP));
    }

    @Test
    void isEnabledForRole_nullRole_defaultsToAllowed() {
        assertTrue(service.isEnabledForRole(NotificationType.WELCOME_EMAIL, null));
    }

    @Test
    void isEnabledForRole_disabledRow_returnsFalse() {
        NotificationRoleSettings settings = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, false);
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.of(settings));

        assertFalse(service.isEnabledForRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP));
    }

    @Test
    void isChannelEnabledForRole_absentRow_defaultsToAllowed() {
        when(repository.findByNotificationTypeAndRole(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.CLIENT_ADMIN))
                .thenReturn(Optional.empty());

        assertTrue(service.isChannelEnabledForRole(
                NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.CLIENT_ADMIN, NotificationChannel.SMS));
    }

    @Test
    void isChannelEnabledForRole_nullRole_defaultsToAllowed() {
        assertTrue(service.isChannelEnabledForRole(NotificationType.CERTIFICATE_ISSUED, null, NotificationChannel.SMS));
    }

    @Test
    void isChannelEnabledForRole_channelDisabledOnRow_returnsFalse() {
        NotificationRoleSettings settings = roleSettings(NotificationType.LEADERBOARD_UPDATE, NotificationRecipientRole.MSP, true);
        settings.setEmailEnabled(false);
        when(repository.findByNotificationTypeAndRole(NotificationType.LEADERBOARD_UPDATE, NotificationRecipientRole.MSP))
                .thenReturn(Optional.of(settings));

        assertFalse(service.isChannelEnabledForRole(NotificationType.LEADERBOARD_UPDATE, NotificationRecipientRole.MSP, NotificationChannel.EMAIL));
        assertTrue(service.isChannelEnabledForRole(NotificationType.LEADERBOARD_UPDATE, NotificationRecipientRole.MSP, NotificationChannel.IN_APP));
    }

    @Test
    void isChannelEnabledForRole_rowDisabledOverridesChannelFlags() {
        NotificationRoleSettings settings = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, false);
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.of(settings));

        assertFalse(service.isChannelEnabledForRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, NotificationChannel.EMAIL));
    }

    @Test
    void seedIfMissing_existingRow_leftUntouched() {
        NotificationRoleSettings existing = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.USER, true);
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.USER))
                .thenReturn(Optional.of(existing));

        NotificationRoleSettings result = service.seedIfMissing(
                NotificationType.WELCOME_EMAIL, NotificationRecipientRole.USER, false, false, false, false, false, false);

        assertEquals(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void seedIfMissing_missingRow_createsWithSuppliedDefaults() {
        when(repository.findByNotificationTypeAndRole(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.MSP))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationRoleSettings result = service.seedIfMissing(
                NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.MSP, true, true, true, false, false, false);

        assertEquals(NotificationType.CERTIFICATE_ISSUED, result.getNotificationType());
        assertEquals(NotificationRecipientRole.MSP, result.getRole());
        assertTrue(result.isEnabled());
        assertTrue(result.isEmailEnabled());
        assertTrue(result.isInAppEnabled());
        assertFalse(result.isSmsEnabled());
    }

    @Test
    void updateRoleSetting_existingRow_updatesOnlyProvidedChannels() {
        NotificationRoleSettings existing = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, true);
        existing.setSmsEnabled(false);
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationRoleSettings result = service.updateRoleSetting(
                NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, false, null, null, true, null, null);

        assertFalse(result.isEnabled());
        assertTrue(result.isSmsEnabled());
        assertTrue(result.isEmailEnabled());
    }

    @Test
    void updateRoleSetting_missingRow_createsNewRow() {
        when(repository.findByNotificationTypeAndRole(NotificationType.PACKAGE_EXPIRY, NotificationRecipientRole.ASPIRE_ADMIN))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationRoleSettings result = service.updateRoleSetting(
                NotificationType.PACKAGE_EXPIRY, NotificationRecipientRole.ASPIRE_ADMIN, true, false, null, null, null, null);

        assertEquals(NotificationType.PACKAGE_EXPIRY, result.getNotificationType());
        assertEquals(NotificationRecipientRole.ASPIRE_ADMIN, result.getRole());
        assertTrue(result.isEnabled());
        assertFalse(result.isEmailEnabled());
        assertTrue(result.isInAppEnabled());
    }

    @Test
    void enableForRole_delegatesToUpdateWithEnabledTrue() {
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationRoleSettings result = service.enableForRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP);

        assertTrue(result.isEnabled());
    }

    @Test
    void disableForRole_delegatesToUpdateWithEnabledFalse() {
        NotificationRoleSettings existing = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, true);
        when(repository.findByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationRoleSettings result = service.disableForRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP);

        assertFalse(result.isEnabled());
    }

    @Test
    void deleteRoleSetting_delegatesToRepository() {
        service.deleteRoleSetting(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP);

        verify(repository).deleteByNotificationTypeAndRole(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP);
    }

    @Test
    void toResponseDto_mapsAllFields() {
        NotificationRoleSettings settings = roleSettings(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.CLIENT_ADMIN, true);
        settings.setId("row-1");

        NotificationRoleSettingsResponseDto dto = service.toResponseDto(settings, true);

        assertEquals("row-1", dto.getId());
        assertEquals(NotificationType.CERTIFICATE_ISSUED, dto.getNotificationType());
        assertEquals(NotificationRecipientRole.CLIENT_ADMIN, dto.getRole());
        assertTrue(dto.isEnabled());
        assertTrue(dto.isCustomized());
    }

    @Test
    void getMatrix_returnsRepositoryOrderedResult() {
        NotificationRoleSettings settings = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.USER, true);
        when(repository.findAllByOrderByNotificationTypeAscRoleAsc()).thenReturn(java.util.List.of(settings));

        java.util.List<NotificationRoleSettings> matrix = service.getMatrix();

        assertEquals(1, matrix.size());
        verify(repository).findAllByOrderByNotificationTypeAscRoleAsc();
    }

    @Test
    void getMatrix_paginated_noFilters_delegatesToAdminVisibleFind() {
        NotificationRoleSettings settings = roleSettings(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.USER, true);
        org.springframework.data.domain.Page<NotificationRoleSettings> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(settings));
        when(repository.findByNotificationTypeInOrderByNotificationTypeAscRoleAsc(
                eq(NotificationType.adminVisibleTypes()),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<NotificationRoleSettings> result = service.getMatrix(null, null, 0, 20);

        assertEquals(1, result.getTotalElements());
        verify(repository).findByNotificationTypeInOrderByNotificationTypeAscRoleAsc(
                eq(NotificationType.adminVisibleTypes()),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getMatrix_paginated_filtersByNotificationType() {
        when(repository.findByNotificationTypeOrderByRoleAsc(
                org.mockito.ArgumentMatchers.eq(NotificationType.WELCOME_EMAIL),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.getMatrix(NotificationType.WELCOME_EMAIL, null, 1, 10);

        verify(repository).findByNotificationTypeOrderByRoleAsc(
                org.mockito.ArgumentMatchers.eq(NotificationType.WELCOME_EMAIL),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getMatrix_paginated_filtersByRole_usesAdminVisibleTypes() {
        when(repository.findByNotificationTypeInAndRoleOrderByNotificationTypeAsc(
                eq(NotificationType.adminVisibleTypes()),
                org.mockito.ArgumentMatchers.eq(NotificationRecipientRole.MSP),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.getMatrix(null, NotificationRecipientRole.MSP, 0, 20);

        verify(repository).findByNotificationTypeInAndRoleOrderByNotificationTypeAsc(
                eq(NotificationType.adminVisibleTypes()),
                org.mockito.ArgumentMatchers.eq(NotificationRecipientRole.MSP),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getMatrix_paginated_filtersByNotificationTypeAndRole() {
        when(repository.findByNotificationTypeAndRole(
                org.mockito.ArgumentMatchers.eq(NotificationType.WELCOME_EMAIL),
                org.mockito.ArgumentMatchers.eq(NotificationRecipientRole.MSP),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.getMatrix(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, 0, 20);

        verify(repository).findByNotificationTypeAndRole(
                org.mockito.ArgumentMatchers.eq(NotificationType.WELCOME_EMAIL),
                org.mockito.ArgumentMatchers.eq(NotificationRecipientRole.MSP),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getMatrix_paginated_hiddenNotificationType_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.getMatrix(NotificationType.PACKAGE_ASSIGNED, null, 0, 20));
        verify(repository, never()).findByNotificationTypeOrderByRoleAsc(any(), any());
    }

    private static NotificationRoleSettings roleSettings(NotificationType type, NotificationRecipientRole role, boolean enabled) {
        return NotificationRoleSettings.builder()
                .notificationType(type)
                .role(role)
                .enabled(enabled)
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .pushEnabled(false)
                .phoneCallEnabled(false)
                .build();
    }
}
