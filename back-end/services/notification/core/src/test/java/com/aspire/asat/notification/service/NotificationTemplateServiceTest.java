package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import com.aspire.asat.notification.service.support.NotificationTemplateRoleMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository repository;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private NotificationTemplateService templateService;

    @Test
    void getTemplate_usesActiveTemplateFromRepository() {
        NotificationTemplate template = emailTemplate(
                "<p>Hello {{userName}} from {{companyName}}</p>",
                "Welcome {{userName}}");

        when(repository.findByNotificationTypeAndChannelAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(template));

        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL);

        assertTrue(result.isPresent());
        assertEquals(template.getId(), result.get().getId());
    }

    @Test
    void generateHtmlContent_replacesPlaceholdersFromModel() {
        NotificationTemplate template = emailTemplate(
                "<p>Hello {{userName}} from {{companyName}}</p>",
                "Welcome {{userName}}");
        template.setId("tpl-render-1");

        String html = templateService.generateHtmlContent(template, Map.of(
                "userName", "Jane Doe",
                "companyName", "Aspire Tech"
        ));

        assertEquals("<p>Hello Jane Doe from Aspire Tech</p>", html);
    }

    @Test
    void generateSubject_replacesPlaceholdersFromModel() {
        NotificationTemplate template = emailTemplate(
                "<p>Body</p>",
                "Welcome {{userName}} to {{companyName}}");

        String subject = templateService.generateSubject(template, Map.of(
                "userName", "Jane Doe",
                "companyName", "Aspire Tech"
        ));

        assertEquals("Welcome Jane Doe to Aspire Tech", subject);
    }

    @Test
    void updateTemplate_persistsWithoutChangingLookupKey() {
        NotificationTemplate template = emailTemplate(
                "<p>Updated body for {{userName}}</p>",
                "Updated subject {{companyName}}");
        template.setId("tpl-update-1");

        when(repository.save(template)).thenReturn(template);

        NotificationTemplate saved = templateService.updateTemplate(template);

        assertEquals("tpl-update-1", saved.getId());
        assertEquals(NotificationType.WELCOME_EMAIL, saved.getNotificationType());
        assertEquals(NotificationChannel.EMAIL, saved.getChannel());
        assertEquals("<p>Updated body for {{userName}}</p>", saved.getHtmlTemplate());
    }

    @Test
    void getTemplate_withNullRole_behavesLikeRoleAgnosticLookup() {
        NotificationTemplate template = emailTemplate("<p>Hi</p>", "Subject");
        when(repository.findByNotificationTypeAndChannelAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(template));

        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, (NotificationRecipientRole) null);

        assertTrue(result.isPresent());
        assertEquals(template.getId(), result.get().getId());
    }

    @Test
    void getTemplate_withRole_prefersRoleSpecificTemplateOverRoleAgnostic() {
        NotificationTemplate roleTemplate = emailTemplate("<p>Admin body</p>", "Admin subject");
        roleTemplate.setId("tpl-role-1");
        roleTemplate.setRecipientRole(NotificationRecipientRole.CLIENT_ADMIN);

        when(repository.findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN))
                .thenReturn(Optional.of(roleTemplate));

        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN);

        assertTrue(result.isPresent());
        assertEquals("tpl-role-1", result.get().getId());
    }

    @Test
    void getTemplate_withRole_fallsBackToRoleAgnosticWhenNoRoleSpecificTemplate() {
        NotificationTemplate fallbackTemplate = emailTemplate("<p>Generic body</p>", "Generic subject");
        fallbackTemplate.setId("tpl-fallback-1");

        when(repository.findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN))
                .thenReturn(Optional.empty());
        when(repository.findByNotificationTypeAndChannelAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(fallbackTemplate));

        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN);

        assertTrue(result.isPresent());
        assertEquals("tpl-fallback-1", result.get().getId());
    }

    @Test
    void createTemplate_activeTemplate_deactivatesOnlyOtherTemplatesForSameRole() {
        NotificationTemplate newTemplate = emailTemplate("<p>New</p>", "New subject");
        newTemplate.setRecipientRole(NotificationRecipientRole.CLIENT_ADMIN);
        newTemplate.setActive(true);

        NotificationTemplate existingSameRole = emailTemplate("<p>Old</p>", "Old subject");
        existingSameRole.setId("tpl-existing-role");
        existingSameRole.setRecipientRole(NotificationRecipientRole.CLIENT_ADMIN);
        existingSameRole.setActive(true);

        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN))
                .thenReturn(List.of(existingSameRole));
        when(repository.save(existingSameRole)).thenReturn(existingSameRole);
        when(repository.save(newTemplate)).thenReturn(newTemplate);

        templateService.createTemplate(newTemplate);

        assertFalse(existingSameRole.isActive());
        verify(repository, times(1)).findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN);
        verify(repository, never()).findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null);
    }

    @Test
    void getTemplate_prefersRoleAgnosticBaseOverRoleVariantSharingTypeAndChannel() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");

        when(repository.findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null))
                .thenReturn(Optional.of(base));

        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL);

        assertTrue(result.isPresent());
        assertEquals("tpl-base", result.get().getId());
        verify(repository, never()).findByNotificationTypeAndChannelAndIsActiveTrue(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL);
    }

    @Test
    void createRoleVariant_inheritsBaseContentForOmittedFields() {
        NotificationTemplate base = emailTemplate("<p>Base body {{userName}}</p>", "Base subject");
        base.setId("tpl-base");

        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null))
                .thenReturn(List.of(base));
        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(List.of());
        when(repository.save(any(NotificationTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationTemplate overrides = NotificationTemplate.builder()
                .subjectTemplate("MSP subject")
                .build();

        NotificationTemplate variant = templateService.createRoleVariant(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP, overrides);

        assertEquals(NotificationRecipientRole.MSP, variant.getRecipientRole());
        assertEquals("MSP subject", variant.getSubjectTemplate());
        assertEquals("<p>Base body {{userName}}</p>", variant.getHtmlTemplate());
        assertEquals("Welcome Email Template (MSP)", variant.getTemplateName());
        assertTrue(variant.isActive());
        assertFalse(variant.isDefault());
    }

    @Test
    void createRoleVariant_leavesBaseTemplateActive() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");

        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null))
                .thenReturn(List.of(base));
        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(List.of());
        when(repository.save(any(NotificationTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        templateService.createRoleVariant(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP, null);

        assertTrue(base.isActive());
        verify(repository, never()).save(base);
    }

    @Test
    void createRoleVariant_nullRole_isRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                templateService.createRoleVariant(
                        NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null, null));

        assertTrue(error.getMessage().contains("Recipient role is required"));
        verify(repository, never()).save(any(NotificationTemplate.class));
    }

    @Test
    void createRoleVariant_withoutBaseTemplate_isRejected() {
        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null))
                .thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                templateService.createRoleVariant(NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                        NotificationRecipientRole.MSP, null));

        assertTrue(error.getMessage().contains("No base template exists"));
        verify(repository, never()).save(any(NotificationTemplate.class));
    }

    @Test
    void createRoleVariant_existingVariantForRole_isRejected() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");
        NotificationTemplate existingVariant = emailTemplate("<p>MSP</p>", "MSP subject");
        existingVariant.setId("tpl-msp");
        existingVariant.setRecipientRole(NotificationRecipientRole.MSP);

        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null))
                .thenReturn(List.of(base));
        when(repository.findAllByNotificationTypeAndChannelAndRecipientRole(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.MSP))
                .thenReturn(List.of(existingVariant));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                templateService.createRoleVariant(NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                        NotificationRecipientRole.MSP, null));

        assertTrue(error.getMessage().contains("tpl-msp"));
        verify(repository, never()).save(any(NotificationTemplate.class));
    }

    @Test
    void deleteRoleVariant_removesVariantSoRoleFallsBackToBase() {
        NotificationTemplate variant = emailTemplate("<p>MSP</p>", "MSP subject");
        variant.setId("tpl-msp");
        variant.setRecipientRole(NotificationRecipientRole.MSP);
        when(repository.findById("tpl-msp")).thenReturn(Optional.of(variant));

        templateService.deleteRoleVariant("tpl-msp");

        verify(repository).deleteById("tpl-msp");
    }

    @Test
    void deleteRoleVariant_baseTemplate_isRejected() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");
        when(repository.findById("tpl-base")).thenReturn(Optional.of(base));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                templateService.deleteRoleVariant("tpl-base"));

        assertTrue(error.getMessage().contains("Base templates cannot be deleted"));
        verify(repository, never()).deleteById("tpl-base");
    }

    @Test
    void deleteRoleVariant_unknownTemplate_isRejected() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> templateService.deleteRoleVariant("missing"));

        verify(repository, never()).deleteById("missing");
    }

    @Test
    void getAllTemplates_roleFilter_fallsBackToBaseWhenNoRoleVariant() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");

        when(repository.search(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, NotificationRecipientRole.USER, true, null))
                .thenReturn(List.of());
        when(repository.search(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null, true, null))
                .thenReturn(List.of(base));

        List<NotificationTemplate> result = templateService.getAllTemplates(
                0, 20, NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                NotificationRecipientRole.USER, true);

        assertEquals(1, result.size());
        assertEquals("tpl-base", result.get(0).getId());
        assertEquals(1L, templateService.countAllTemplates(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                NotificationRecipientRole.USER, true));
    }

    @Test
    void getAllTemplates_roleFilter_prefersRoleVariantOverBase() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");
        NotificationTemplate clientAdmin = emailTemplate("<p>Admin</p>", "Admin subject");
        clientAdmin.setId("tpl-ca");
        clientAdmin.setRecipientRole(NotificationRecipientRole.CLIENT_ADMIN);

        when(repository.search(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                NotificationRecipientRole.CLIENT_ADMIN, true, null))
                .thenReturn(List.of(clientAdmin));
        when(repository.search(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL, null, true, null))
                .thenReturn(List.of(base));

        List<NotificationTemplate> result = templateService.getAllTemplates(
                0, 20, NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL,
                NotificationRecipientRole.CLIENT_ADMIN, true);

        assertEquals(1, result.size());
        assertEquals("tpl-ca", result.get(0).getId());
    }

    @Test
    void getAllTemplates_roleFilter_includesBaseOnlyForUncoveredTypeChannels() {
        NotificationTemplate welcomeBase = emailTemplate("<p>Welcome base</p>", "Welcome");
        welcomeBase.setId("tpl-welcome-base");
        NotificationTemplate passwordBase = NotificationTemplate.builder()
                .id("tpl-password-base")
                .notificationType(NotificationType.PASSWORD_RESET_REQUEST)
                .channel(NotificationChannel.EMAIL)
                .templateName("Password Reset Base")
                .isActive(true)
                .isDefault(true)
                .build();
        NotificationTemplate welcomeClientAdmin = emailTemplate("<p>CA</p>", "CA");
        welcomeClientAdmin.setId("tpl-welcome-ca");
        welcomeClientAdmin.setRecipientRole(NotificationRecipientRole.CLIENT_ADMIN);

        when(repository.search(null, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN, true, null))
                .thenReturn(List.of(welcomeClientAdmin));
        when(repository.search(null, NotificationChannel.EMAIL, null, true, null))
                .thenReturn(List.of(welcomeBase, passwordBase));

        List<NotificationTemplate> result = templateService.getAllTemplates(
                0, 20, null, NotificationChannel.EMAIL, NotificationRecipientRole.CLIENT_ADMIN, true);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> "tpl-welcome-ca".equals(t.getId())));
        assertTrue(result.stream().anyMatch(t -> "tpl-password-base".equals(t.getId())));
        assertFalse(result.stream().anyMatch(t -> "tpl-welcome-base".equals(t.getId())));
    }

    @Test
    void getRoleMatrix_splitsBaseTemplateFromRoleVariants() {
        NotificationTemplate base = emailTemplate("<p>Base</p>", "Base subject");
        base.setId("tpl-base");
        NotificationTemplate mspVariant = emailTemplate("<p>MSP</p>", "MSP subject");
        mspVariant.setId("tpl-msp");
        mspVariant.setRecipientRole(NotificationRecipientRole.MSP);
        NotificationTemplate inactiveUserVariant = emailTemplate("<p>Old user</p>", "Old user subject");
        inactiveUserVariant.setId("tpl-user-inactive");
        inactiveUserVariant.setRecipientRole(NotificationRecipientRole.USER);
        inactiveUserVariant.setActive(false);
        NotificationTemplate activeUserVariant = emailTemplate("<p>User</p>", "User subject");
        activeUserVariant.setId("tpl-user");
        activeUserVariant.setRecipientRole(NotificationRecipientRole.USER);

        when(repository.findAllByNotificationTypeAndChannel(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL))
                .thenReturn(List.of(mspVariant, inactiveUserVariant, base, activeUserVariant));

        NotificationTemplateRoleMatrix matrix = templateService.getRoleMatrix(
                NotificationType.WELCOME_EMAIL, NotificationChannel.EMAIL);

        assertEquals("tpl-base", matrix.baseTemplate().getId());
        assertEquals(2, matrix.variants().size());
        assertEquals("tpl-msp", matrix.variantFor(NotificationRecipientRole.MSP).orElseThrow().getId());
        assertEquals("tpl-user", matrix.variantFor(NotificationRecipientRole.USER).orElseThrow().getId());
        assertTrue(matrix.variantFor(NotificationRecipientRole.CLIENT_ADMIN).isEmpty());
    }

    private static NotificationTemplate emailTemplate(String html, String subject) {
        return NotificationTemplate.builder()
                .notificationType(NotificationType.WELCOME_EMAIL)
                .channel(NotificationChannel.EMAIL)
                .templateName("Welcome Email Template")
                .htmlTemplate(html)
                .subjectTemplate(subject)
                .isActive(true)
                .isDefault(true)
                .build();
    }
}
