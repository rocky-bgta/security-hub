package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Covers the full precedence chain owned by {@link NotificationPreferenceResolver}:
 * global -&gt; role -&gt; organization -&gt; user, including the plan's worked example
 * (Welcome enabled globally, MSP role disabled, everything else enabled) and a user
 * override beating a permissive role setting.
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferenceResolverTest {

    private static final NotificationType TYPE = NotificationType.WELCOME_EMAIL;
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String USER_ID = "user-1";

    @Mock
    private NotificationSettingsService globalSettingsService;

    @Mock
    private NotificationRoleSettingsService roleSettingsService;

    @Mock
    private ClientNotificationSettingsService clientSettingsService;

    @Mock
    private UserNotificationSettingsService userSettingsService;

    @InjectMocks
    private NotificationPreferenceResolver resolver;

    private void allowAllLayers() {
        lenient().when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        lenient().when(globalSettingsService.isChannelEnabled(TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        lenient().when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.MSP)).thenReturn(true);
        lenient().when(roleSettingsService.isChannelEnabledForRole(TYPE, NotificationRecipientRole.MSP, NotificationChannel.EMAIL)).thenReturn(true);
        lenient().when(clientSettingsService.isNotificationTypeEnabledForClient(CLIENT_ADMIN_ID, TYPE)).thenReturn(true);
        lenient().when(clientSettingsService.isChannelEnabledForClient(CLIENT_ADMIN_ID, TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        lenient().when(userSettingsService.isEnabledForUser(USER_ID, TYPE)).thenReturn(true);
        lenient().when(userSettingsService.isChannelEnabledForUser(USER_ID, TYPE, NotificationChannel.EMAIL)).thenReturn(true);
    }

    @Test
    void isTypeAllowed_allLayersEnabled_returnsTrue() {
        allowAllLayers();

        assertTrue(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_globalDisabled_shortCircuitsAndSkipsAllRecipients() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(false);

        assertFalse(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_workedExample_mspRoleDisabledEverythingElseEnabled_returnsFalse() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.MSP)).thenReturn(false);

        assertFalse(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_workedExample_otherRolesUnaffectedByMspBeingDisabled() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.CLIENT_ADMIN)).thenReturn(true);
        when(clientSettingsService.isNotificationTypeEnabledForClient(CLIENT_ADMIN_ID, TYPE)).thenReturn(true);
        when(userSettingsService.isEnabledForUser(USER_ID, TYPE)).thenReturn(true);

        assertTrue(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.CLIENT_ADMIN, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_orgDisabled_returnsFalseEvenWhenRoleAllows() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.USER)).thenReturn(true);
        when(clientSettingsService.isNotificationTypeEnabledForClient(CLIENT_ADMIN_ID, TYPE)).thenReturn(false);

        assertFalse(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.USER, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_userOverrideDisabled_beatsPermissiveRoleAndOrg() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.USER)).thenReturn(true);
        when(clientSettingsService.isNotificationTypeEnabledForClient(CLIENT_ADMIN_ID, TYPE)).thenReturn(true);
        when(userSettingsService.isEnabledForUser(USER_ID, TYPE)).thenReturn(false);

        assertFalse(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.USER, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_userOverrideEnabled_allowsWhenAllUpstreamLayersAllow() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.USER)).thenReturn(true);
        when(clientSettingsService.isNotificationTypeEnabledForClient(CLIENT_ADMIN_ID, TYPE)).thenReturn(true);
        when(userSettingsService.isEnabledForUser(USER_ID, TYPE)).thenReturn(true);

        assertTrue(resolver.isTypeAllowed(TYPE, NotificationRecipientRole.USER, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isTypeAllowed_nullRoleClientAndUser_onlyChecksGlobalLayer() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, null)).thenReturn(true);
        when(clientSettingsService.isNotificationTypeEnabledForClient(null, TYPE)).thenReturn(true);
        when(userSettingsService.isEnabledForUser(null, TYPE)).thenReturn(true);

        assertTrue(resolver.isTypeAllowed(TYPE, null, null, null));
    }

    @Test
    void isChannelAllowed_allLayersEnabled_returnsTrue() {
        allowAllLayers();

        assertTrue(resolver.isChannelAllowed(TYPE, NotificationChannel.EMAIL, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isChannelAllowed_globalChannelDisabled_returnsFalse() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(globalSettingsService.isChannelEnabled(TYPE, NotificationChannel.SMS)).thenReturn(false);

        assertFalse(resolver.isChannelAllowed(TYPE, NotificationChannel.SMS, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isChannelAllowed_roleChannelDisabled_returnsFalse() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(globalSettingsService.isChannelEnabled(TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.MSP)).thenReturn(true);
        when(roleSettingsService.isChannelEnabledForRole(TYPE, NotificationRecipientRole.MSP, NotificationChannel.EMAIL)).thenReturn(false);

        assertFalse(resolver.isChannelAllowed(TYPE, NotificationChannel.EMAIL, NotificationRecipientRole.MSP, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isChannelAllowed_orgChannelDisabled_returnsFalse() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(globalSettingsService.isChannelEnabled(TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.USER)).thenReturn(true);
        when(roleSettingsService.isChannelEnabledForRole(TYPE, NotificationRecipientRole.USER, NotificationChannel.EMAIL)).thenReturn(true);
        when(clientSettingsService.isChannelEnabledForClient(CLIENT_ADMIN_ID, TYPE, NotificationChannel.EMAIL)).thenReturn(false);

        assertFalse(resolver.isChannelAllowed(TYPE, NotificationChannel.EMAIL, NotificationRecipientRole.USER, CLIENT_ADMIN_ID, USER_ID));
    }

    @Test
    void isChannelAllowed_userChannelDisabled_beatsEnabledOrgAndRole() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(globalSettingsService.isChannelEnabled(TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        when(roleSettingsService.isEnabledForRole(TYPE, NotificationRecipientRole.USER)).thenReturn(true);
        when(roleSettingsService.isChannelEnabledForRole(TYPE, NotificationRecipientRole.USER, NotificationChannel.EMAIL)).thenReturn(true);
        when(clientSettingsService.isChannelEnabledForClient(CLIENT_ADMIN_ID, TYPE, NotificationChannel.EMAIL)).thenReturn(true);
        when(userSettingsService.isEnabledForUser(USER_ID, TYPE)).thenReturn(true);
        when(userSettingsService.isChannelEnabledForUser(USER_ID, TYPE, NotificationChannel.EMAIL)).thenReturn(false);

        assertFalse(resolver.isChannelAllowed(TYPE, NotificationChannel.EMAIL, NotificationRecipientRole.USER, CLIENT_ADMIN_ID, USER_ID));
    }
}
