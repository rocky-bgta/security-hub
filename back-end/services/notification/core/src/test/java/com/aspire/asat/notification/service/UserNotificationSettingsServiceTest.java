package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsResponseDto;
import com.aspire.asat.notification.model.ClientNotificationSettings;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.model.UserNotificationSettings;
import com.aspire.asat.notification.repository.ClientNotificationSettingsRepository;
import com.aspire.asat.notification.repository.UserNotificationSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationSettingsServiceTest {

    private static final String USER_ID = "user-1";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final NotificationType TYPE = NotificationType.WELCOME_EMAIL;

    @Mock
    private UserNotificationSettingsRepository repository;

    @Mock
    private NotificationSettingsService globalSettingsService;

    @Mock
    private NotificationRoleSettingsService roleSettingsService;

    @Mock
    private ClientNotificationSettingsRepository clientNotificationSettingsRepository;

    @InjectMocks
    private UserNotificationSettingsService service;

    @Test
    void isEnabledForUser_absentRow_defaultsToAllowed() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());

        assertTrue(service.isEnabledForUser(USER_ID, TYPE));
    }

    @Test
    void isEnabledForUser_blankUserId_defaultsToAllowed() {
        assertTrue(service.isEnabledForUser(" ", TYPE));
        assertTrue(service.isEnabledForUser(null, TYPE));
    }

    @Test
    void isEnabledForUser_disabledRow_returnsFalse() {
        UserNotificationSettings setting = userSettings(false);
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.of(setting));

        assertFalse(service.isEnabledForUser(USER_ID, TYPE));
    }

    @Test
    void isChannelEnabledForUser_absentRow_defaultsToAllowed() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());

        assertTrue(service.isChannelEnabledForUser(USER_ID, TYPE, NotificationChannel.SMS));
    }

    @Test
    void isChannelEnabledForUser_blankUserId_defaultsToAllowed() {
        assertTrue(service.isChannelEnabledForUser("", TYPE, NotificationChannel.SMS));
    }

    @Test
    void isChannelEnabledForUser_channelDisabled_returnsFalse() {
        UserNotificationSettings setting = userSettings(true);
        setting.setSmsEnabled(false);
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.of(setting));

        assertFalse(service.isChannelEnabledForUser(USER_ID, TYPE, NotificationChannel.SMS));
    }

    @Test
    void getMergedSetting_userRowPresent_sourceIsUser() {
        UserNotificationSettings setting = userSettings(true);
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.of(setting));

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER, TYPE);

        assertEquals("USER", dto.getSource());
        assertTrue(dto.isCustomized());
    }

    @Test
    void getMergedSetting_noUserRow_orgRowPresent_sourceIsOrganization() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        ClientNotificationSettings orgSetting = ClientNotificationSettings.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .notificationType(TYPE)
                .enabled(true)
                .emailEnabled(true)
                .inAppEnabled(true)
                .build();
        when(clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(CLIENT_ADMIN_ID, TYPE))
                .thenReturn(Optional.of(orgSetting));

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER, TYPE);

        assertEquals("ORGANIZATION", dto.getSource());
        assertFalse(dto.isCustomized());
    }

    @Test
    void getMergedSetting_noUserOrOrgRow_roleRowPresent_sourceIsRole() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(CLIENT_ADMIN_ID, TYPE))
                .thenReturn(Optional.empty());
        NotificationRoleSettings roleSetting = NotificationRoleSettings.builder()
                .notificationType(TYPE)
                .role(NotificationRecipientRole.USER)
                .enabled(true)
                .emailEnabled(true)
                .inAppEnabled(true)
                .build();
        when(roleSettingsService.getSetting(TYPE, NotificationRecipientRole.USER)).thenReturn(Optional.of(roleSetting));

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER, TYPE);

        assertEquals("ROLE", dto.getSource());
    }

    @Test
    void getMergedSetting_noUserOrgOrRoleRow_fallsBackToGlobal() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(CLIENT_ADMIN_ID, TYPE))
                .thenReturn(Optional.empty());
        when(roleSettingsService.getSetting(TYPE, NotificationRecipientRole.USER)).thenReturn(Optional.empty());
        NotificationSettings globalSetting = NotificationSettings.builder()
                .notificationType(TYPE)
                .enabled(true)
                .emailEnabled(true)
                .inAppEnabled(true)
                .build();
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.of(globalSetting));

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER, TYPE);

        assertEquals("GLOBAL", dto.getSource());
    }

    @Test
    void getMergedSetting_nothingConfiguredAnywhere_defaultsToAllowedGlobal() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(CLIENT_ADMIN_ID, TYPE))
                .thenReturn(Optional.empty());
        when(roleSettingsService.getSetting(TYPE, NotificationRecipientRole.USER)).thenReturn(Optional.empty());
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.empty());

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER, TYPE);

        assertEquals("GLOBAL", dto.getSource());
        assertTrue(dto.isEnabled());
        assertFalse(dto.isCustomized());
    }

    @Test
    void getMergedSetting_nullClientAdminIdAndRole_skipsToGlobal() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.empty());

        UserNotificationSettingsResponseDto dto = service.getMergedSetting(USER_ID, null, null, TYPE);

        assertEquals("GLOBAL", dto.getSource());
    }

    @Test
    void getAllForUser_returnsOneRowPerNotificationType() {
        when(repository.findByUserIdAndNotificationType(any(), any())).thenReturn(Optional.empty());
        when(clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(any(), any())).thenReturn(Optional.empty());
        when(roleSettingsService.getSetting(any(), any())).thenReturn(Optional.empty());
        when(globalSettingsService.getSettings(any())).thenReturn(Optional.empty());

        assertEquals(NotificationType.values().length,
                service.getAllForUser(USER_ID, CLIENT_ADMIN_ID, NotificationRecipientRole.USER).size());
    }

    @Test
    void updateUserSetting_existingRow_updatesOnlyProvidedChannels() {
        UserNotificationSettings existing = userSettings(true);
        existing.setSmsEnabled(false);
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserNotificationSettings result = service.updateUserSetting(USER_ID, TYPE, false, null, null, true, null, null);

        assertFalse(result.isEnabled());
        assertTrue(result.isSmsEnabled());
        assertTrue(result.isEmailEnabled());
    }

    @Test
    void updateUserSetting_missingRow_seedsFromGlobalDefaults() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        NotificationSettings globalSetting = NotificationSettings.builder()
                .notificationType(TYPE)
                .enabled(true)
                .emailEnabled(true)
                .inAppEnabled(false)
                .smsEnabled(true)
                .build();
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.of(globalSetting));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserNotificationSettings result = service.updateUserSetting(USER_ID, TYPE, true, null, null, null, null, null);

        assertTrue(result.isEmailEnabled());
        assertFalse(result.isInAppEnabled());
        assertTrue(result.isSmsEnabled());
    }

    @Test
    void updateUserSetting_missingRowAndGlobal_fallsBackToSafeDefaults() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserNotificationSettings result = service.updateUserSetting(USER_ID, TYPE, true, null, null, null, null, null);

        assertTrue(result.isEmailEnabled());
        assertTrue(result.isInAppEnabled());
        assertFalse(result.isSmsEnabled());
        assertFalse(result.isPushEnabled());
        assertFalse(result.isPhoneCallEnabled());
    }

    @Test
    void enableForUser_setsEnabledTrue() {
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.empty());
        when(globalSettingsService.getSettings(TYPE)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.enableForUser(USER_ID, TYPE).isEnabled());
    }

    @Test
    void disableForUser_setsEnabledFalse() {
        UserNotificationSettings existing = userSettings(true);
        when(repository.findByUserIdAndNotificationType(USER_ID, TYPE)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(service.disableForUser(USER_ID, TYPE).isEnabled());
    }

    @Test
    void deleteUserSetting_delegatesToRepository() {
        service.deleteUserSetting(USER_ID, TYPE);

        verify(repository).deleteByUserIdAndNotificationType(USER_ID, TYPE);
        verify(repository, never()).save(any());
    }

    private static UserNotificationSettings userSettings(boolean enabled) {
        return UserNotificationSettings.builder()
                .userId(USER_ID)
                .notificationType(TYPE)
                .enabled(enabled)
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(true)
                .pushEnabled(false)
                .phoneCallEnabled(false)
                .build();
    }
}
