package com.aspire.asat.notification.utils;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.config.NotificationDataProperties;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationSettingsRepository;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import com.aspire.asat.notification.service.NotificationRoleSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDataInitializerRoleTemplateSeedTest {

    /**
     * The 22 role variants created by the mongosh migration (and mirrored in the seeder).
     * Pre-existing CLIENT_ADMIN rows for other families are also seeded but are not part of
     * this set.
     */
    private static final Set<String> MIGRATION_ROLE_TRIPLES = Set.of(
            // WELCOME_EMAIL — no MSP IN_APP
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.IN_APP, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.IN_APP, NotificationRecipientRole.ASPIRE_ADMIN),

            // USER_PASSWORD_CHANGE — MSP + ASPIRE_ADMIN (CLIENT_ADMIN already existed)
            key(NotificationType.USER_PASSWORD_CHANGE, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.USER_PASSWORD_CHANGE, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.USER_PASSWORD_CHANGE, NotificationChannel.IN_APP, NotificationRecipientRole.MSP),
            key(NotificationType.USER_PASSWORD_CHANGE, NotificationChannel.IN_APP, NotificationRecipientRole.ASPIRE_ADMIN),

            // COURSE_COMPLETION_USER — ASPIRE_ADMIN only (MSP disabled; CLIENT_ADMIN already existed)
            key(NotificationType.COURSE_COMPLETION_USER, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.COURSE_COMPLETION_USER, NotificationChannel.IN_APP, NotificationRecipientRole.ASPIRE_ADMIN),

            // CERTIFICATE_ISSUED — EMAIL MSP + ASPIRE_ADMIN (CLIENT_ADMIN already existed)
            key(NotificationType.CERTIFICATE_ISSUED, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.CERTIFICATE_ISSUED, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),

            // PACKAGE_ASSIGNED_USER — MSP + ASPIRE_ADMIN
            key(NotificationType.PACKAGE_ASSIGNED_USER, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.PACKAGE_ASSIGNED_USER, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.PACKAGE_ASSIGNED_USER, NotificationChannel.IN_APP, NotificationRecipientRole.MSP),
            key(NotificationType.PACKAGE_ASSIGNED_USER, NotificationChannel.IN_APP, NotificationRecipientRole.ASPIRE_ADMIN),

            // PACKAGE_ASSIGNED_AND_USER_CREDENTIAL — ASPIRE_ADMIN only (MSP disabled)
            key(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationChannel.IN_APP, NotificationRecipientRole.ASPIRE_ADMIN),

            // USER_SUSPENSION — EMAIL for all three admin roles
            key(NotificationType.USER_SUSPENSION, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.USER_SUSPENSION, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.USER_SUSPENSION, NotificationChannel.EMAIL, NotificationRecipientRole.ASPIRE_ADMIN)
    );

    /**
     * The 27 role SMS variants for SMS-matrix types seeded on startup. USER has no role SMS row.
     */
    private static final Set<String> ROLE_SMS_TRIPLES = Set.of(
            key(NotificationType.USER_SUSPENDED, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.USER_SUSPENDED, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.USER_SUSPENDED, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.USER_SUSPENSION, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.USER_SUSPENSION, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.USER_SUSPENSION, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.PACKAGE_EXPIRY, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.PACKAGE_EXPIRY, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.PACKAGE_EXPIRY, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.PAYMENT_FAILURE, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.PAYMENT_FAILURE, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.PAYMENT_FAILURE, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.PENDING_PAYMENT, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.PENDING_PAYMENT, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.PENDING_PAYMENT, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN),
            key(NotificationType.SECURITY_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.CLIENT_ADMIN),
            key(NotificationType.SECURITY_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.MSP),
            key(NotificationType.SECURITY_ALERTS, NotificationChannel.SMS, NotificationRecipientRole.ASPIRE_ADMIN)
    );

    private static final Set<String> SKIPPED_MSP_TRIPLES = Set.of(
            key(NotificationType.WELCOME_EMAIL, NotificationChannel.IN_APP, NotificationRecipientRole.MSP),
            key(NotificationType.COURSE_COMPLETION_USER, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.COURSE_COMPLETION_USER, NotificationChannel.IN_APP, NotificationRecipientRole.MSP),
            key(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP),
            key(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationChannel.IN_APP, NotificationRecipientRole.MSP)
    );

    @Mock
    private NotificationSettingsRepository settingsRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationDataProperties dataProperties;

    @Mock
    private NotificationRoleSettingsService roleSettingsService;

    @InjectMocks
    private NotificationDataInitializer initializer;

    @BeforeEach
    void setUp() {
        when(dataProperties.isInitializeOnStartup()).thenReturn(true);
        when(settingsRepository.findAll()).thenReturn(List.of());
        when(settingsRepository.save(any(NotificationSettings.class))).thenAnswer(inv -> inv.getArgument(0));
        when(settingsRepository.count()).thenReturn(0L);

        when(roleSettingsService.getMatrix()).thenReturn(List.of());
        when(roleSettingsService.seedIfMissing(any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(NotificationRoleSettings.builder().build());

        when(templateRepository.findAll()).thenReturn(List.of());
        when(templateRepository.save(any(NotificationTemplate.class))).thenAnswer(inv -> inv.getArgument(0));
        when(templateRepository.count()).thenReturn(0L);
    }

    @Test
    void run_seedsAllTwentyTwoMigrationRoleVariantTriples() {
        initializer.run();

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        org.mockito.Mockito.verify(templateRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        Set<String> seededRoleTriples = captor.getAllValues().stream()
                .filter(template -> template.getRecipientRole() != null)
                .map(template -> key(template.getNotificationType(), template.getChannel(), template.getRecipientRole()))
                .collect(Collectors.toSet());

        assertEquals(22, MIGRATION_ROLE_TRIPLES.size());
        assertTrue(seededRoleTriples.containsAll(MIGRATION_ROLE_TRIPLES),
                "Seeded templates must include every migration role triple. Missing: "
                        + MIGRATION_ROLE_TRIPLES.stream()
                        .filter(triple -> !seededRoleTriples.contains(triple))
                        .collect(Collectors.toSet()));
    }

    @Test
    void run_doesNotSeedDisabledMspCombinations() {
        initializer.run();

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        org.mockito.Mockito.verify(templateRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        Set<String> seededRoleTriples = captor.getAllValues().stream()
                .filter(template -> template.getRecipientRole() != null)
                .map(template -> key(template.getNotificationType(), template.getChannel(), template.getRecipientRole()))
                .collect(Collectors.toSet());

        for (String skipped : SKIPPED_MSP_TRIPLES) {
            assertFalse(seededRoleTriples.contains(skipped), "Must not seed disabled combination: " + skipped);
        }
    }

    @Test
    void run_roleVariantsCarryAdminAddressedCopyAndAreNotDefault() {
        initializer.run();

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        org.mockito.Mockito.verify(templateRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        NotificationTemplate welcomeBase = captor.getAllValues().stream()
                .filter(t -> t.getNotificationType() == NotificationType.WELCOME_EMAIL
                        && t.getChannel() == NotificationChannel.EMAIL
                        && t.getRecipientRole() == null)
                .findFirst()
                .orElseThrow();

        NotificationTemplate welcomeMsp = captor.getAllValues().stream()
                .filter(t -> t.getNotificationType() == NotificationType.WELCOME_EMAIL
                        && t.getChannel() == NotificationChannel.EMAIL
                        && t.getRecipientRole() == NotificationRecipientRole.MSP)
                .findFirst()
                .orElseThrow();

        assertTrue(welcomeBase.getSubjectTemplate().contains("Welcome to"),
                "Base WELCOME_EMAIL subject should be user-facing");
        assertEquals("New User Registration - {{companyName}}", welcomeMsp.getSubjectTemplate(),
                "MSP WELCOME_EMAIL subject should reuse NEW_USER_REGISTERED admin copy");
        assertTrue(welcomeMsp.getTextTemplate().contains("{{mspName}}"),
                "MSP WELCOME_EMAIL body should address the MSP by name");
        assertFalse(welcomeMsp.isDefault(), "Role variants must not be marked default");
        assertTrue(welcomeBase.isDefault(), "Base templates remain the default for a (type, channel) pair");
    }

    @Test
    void run_seedsAllTwentySevenMigrationRoleSmsTriples() {
        initializer.run();

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        org.mockito.Mockito.verify(templateRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        Set<String> seededRoleSmsTriples = captor.getAllValues().stream()
                .filter(template -> template.getRecipientRole() != null)
                .filter(template -> template.getChannel() == NotificationChannel.SMS)
                .map(template -> key(template.getNotificationType(), template.getChannel(), template.getRecipientRole()))
                .collect(Collectors.toSet());

        assertEquals(27, ROLE_SMS_TRIPLES.size());
        assertTrue(seededRoleSmsTriples.containsAll(ROLE_SMS_TRIPLES),
                "Seeded SMS templates must include every migration role SMS triple. Missing: "
                        + ROLE_SMS_TRIPLES.stream()
                        .filter(triple -> !seededRoleSmsTriples.contains(triple))
                        .collect(Collectors.toSet()));
    }

    @Test
    void run_roleSmsVariantsUseAdminAlertCopyAndAreNotDefault() {
        initializer.run();

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        org.mockito.Mockito.verify(templateRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

        NotificationTemplate baseSms = captor.getAllValues().stream()
                .filter(t -> t.getNotificationType() == NotificationType.USER_SUSPENSION
                        && t.getChannel() == NotificationChannel.SMS
                        && t.getRecipientRole() == null)
                .findFirst()
                .orElseThrow();

        NotificationTemplate clientAdminSms = captor.getAllValues().stream()
                .filter(t -> t.getNotificationType() == NotificationType.USER_SUSPENSION
                        && t.getChannel() == NotificationChannel.SMS
                        && t.getRecipientRole() == NotificationRecipientRole.CLIENT_ADMIN)
                .findFirst()
                .orElseThrow();

        assertTrue(baseSms.getTextTemplate().startsWith("Hi{{userName}}"),
                "Base USER_SUSPENSION SMS should remain user-addressed");
        assertFalse(clientAdminSms.getTextTemplate().startsWith("Hi{{userName}}"),
                "Admin role SMS must not use end-user voice");
        assertTrue(clientAdminSms.getTextTemplate().startsWith("ASAT alert:"),
                "Admin role SMS should use third-person alert copy");
        assertFalse(clientAdminSms.isDefault(), "Role SMS variants must not be marked default");
        assertTrue(baseSms.isDefault(), "Base SMS remains the default for a (type, channel) pair");
    }

    @Test
    void run_seedsMspAndAspireAdminRoleSettingsDisabledByDefault() {
        initializer.run();

        ArgumentCaptor<Boolean> enabledCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(roleSettingsService).seedIfMissing(
                eq(NotificationType.WELCOME_EMAIL),
                eq(NotificationRecipientRole.USER),
                enabledCaptor.capture(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        assertTrue(enabledCaptor.getValue(), "USER role settings default to enabled");

        verify(roleSettingsService).seedIfMissing(
                eq(NotificationType.WELCOME_EMAIL),
                eq(NotificationRecipientRole.CLIENT_ADMIN),
                enabledCaptor.capture(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        assertTrue(enabledCaptor.getValue(), "CLIENT_ADMIN role settings default to enabled");

        verify(roleSettingsService).seedIfMissing(
                eq(NotificationType.WELCOME_EMAIL),
                eq(NotificationRecipientRole.MSP),
                enabledCaptor.capture(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        assertFalse(enabledCaptor.getValue(), "MSP role settings default to disabled (opt-in)");

        verify(roleSettingsService).seedIfMissing(
                eq(NotificationType.WELCOME_EMAIL),
                eq(NotificationRecipientRole.ASPIRE_ADMIN),
                enabledCaptor.capture(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
        assertFalse(enabledCaptor.getValue(), "ASPIRE_ADMIN role settings default to disabled (opt-in)");
    }

    private static String key(NotificationType type, NotificationChannel channel, NotificationRecipientRole role) {
        return type + ":" + channel + ":" + role;
    }
}
