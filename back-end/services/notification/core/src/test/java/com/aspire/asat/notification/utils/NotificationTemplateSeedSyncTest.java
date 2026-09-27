package com.aspire.asat.notification.utils;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateSeedSyncTest {

    @Mock
    private NotificationTemplateRepository repository;

    @Test
    void syncInsertOnly_existingTemplateIsPreservedAndNotSaved() {
        NotificationTemplate existing = emailTemplate(
                NotificationType.NEW_USER_REGISTERED,
                "Admin edited subject - {{companyName}}",
                "<p>Admin edited body {{userName}}</p>");

        when(repository.findAll()).thenReturn(List.of(existing));

        NotificationTemplate seed = emailTemplate(
                NotificationType.NEW_USER_REGISTERED,
                "Hardcoded default subject - {{companyName}}",
                "<p>Hardcoded default body {{userName}}</p>");

        NotificationTemplateSeedSync.Result result =
                NotificationTemplateSeedSync.syncInsertOnly(repository, List.of(seed));

        assertEquals(0, result.inserted());
        assertEquals(1, result.preserved());
        verify(repository, never()).save(any());
    }

    @Test
    void syncInsertOnly_missingTemplateIsInserted() {
        when(repository.findAll()).thenReturn(List.of());

        NotificationTemplate seed = emailTemplate(
                NotificationType.WELCOME_EMAIL,
                "Welcome - {{companyName}}",
                "<p>Welcome {{userName}}</p>");

        NotificationTemplateSeedSync.Result result =
                NotificationTemplateSeedSync.syncInsertOnly(repository, List.of(seed));

        assertEquals(1, result.inserted());
        assertEquals(0, result.preserved());

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(repository).save(captor.capture());
        assertEquals(NotificationType.WELCOME_EMAIL, captor.getValue().getNotificationType());
        assertEquals("Welcome - {{companyName}}", captor.getValue().getSubjectTemplate());
    }

    @Test
    void syncInsertOnly_mixedExistingAndMissingTemplates() {
        NotificationTemplate existing = emailTemplate(
                NotificationType.NEW_USER_REGISTERED,
                "Existing subject",
                "<p>Existing html</p>");

        when(repository.findAll()).thenReturn(List.of(existing));

        NotificationTemplate existingSeed = emailTemplate(
                NotificationType.NEW_USER_REGISTERED,
                "Should not overwrite",
                "<p>Should not overwrite</p>");
        NotificationTemplate missingSeed = emailTemplate(
                NotificationType.COURSE_ASSIGNED,
                "New seed subject",
                "<p>New seed html</p>");

        NotificationTemplateSeedSync.Result result = NotificationTemplateSeedSync.syncInsertOnly(
                repository, List.of(existingSeed, missingSeed));

        assertEquals(1, result.inserted());
        assertEquals(1, result.preserved());

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(repository).save(captor.capture());
        assertEquals(NotificationType.COURSE_ASSIGNED, captor.getValue().getNotificationType());
    }

    private static NotificationTemplate emailTemplate(NotificationType type,
                                                      String subject,
                                                      String html) {
        return NotificationTemplate.builder()
                .notificationType(type)
                .channel(NotificationChannel.EMAIL)
                .templateName(type.name() + " Email Template")
                .subjectTemplate(subject)
                .htmlTemplate(html)
                .isActive(true)
                .isDefault(true)
                .build();
    }
}
