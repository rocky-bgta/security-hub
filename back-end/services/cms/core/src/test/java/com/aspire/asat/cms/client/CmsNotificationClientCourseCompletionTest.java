package com.aspire.asat.cms.client;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.notification.CourseCompletionNotificationRequest;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsNotificationClientCourseCompletionTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ClientAdminServiceClient clientAdminServiceClient;

    @Test
    void sendCourseCompletionNotifications_dispatchesRoleBasedEventWithoutIdentityByRole() {
        CmsNotificationClient cmsNotificationClient =
                new CmsNotificationClient(notificationClient, clientAdminServiceClient);
        when(notificationClient.sendRoleBasedNotification(any(NotificationEventRequestDto.class)))
                .thenReturn(true);

        CourseCompletionNotificationRequest request = new CourseCompletionNotificationRequest(
                "user@example.com",
                "user-1",
                "Jane Doe",
                "Phishing Training",
                "May 23, 2026",
                "2026-05-23 10:00:00",
                "admin@example.com",
                "Acme Admin",
                "admin-1"
        );

        assertTrue(cmsNotificationClient.sendCourseCompletionNotifications(request));

        ArgumentCaptor<NotificationEventRequestDto> eventCaptor =
                ArgumentCaptor.forClass(NotificationEventRequestDto.class);
        verify(notificationClient).sendRoleBasedNotification(eventCaptor.capture());

        NotificationEventRequestDto event = eventCaptor.getValue();
        assertEquals("user-1", event.getTargetUserId());
        assertEquals(NotificationType.COURSE_COMPLETION_USER, event.getNotificationType());
        assertTrue(event.getChannels().contains(NotificationChannel.EMAIL));
        assertTrue(event.getChannels().contains(NotificationChannel.IN_APP));
        assertEquals("Jane Doe", event.getTemplateModel().get("userName"));
        assertEquals("Phishing Training", event.getTemplateModel().get("courseTitle"));
        assertEquals("2026-05-23 10:00:00", event.getTemplateModel().get("timestamp"));
        assertNull(event.getTemplateModelByRole());
    }

    @Test
    void sendCourseCompletionNotifications_missingUserEmail_doesNotDispatch() {
        CmsNotificationClient cmsNotificationClient =
                new CmsNotificationClient(notificationClient, clientAdminServiceClient);

        CourseCompletionNotificationRequest request = new CourseCompletionNotificationRequest(
                "",
                "user-1",
                "Jane Doe",
                "Phishing Training",
                "May 23, 2026",
                "2026-05-23 10:00:00",
                "admin@example.com",
                "Acme Admin",
                "admin-1"
        );

        assertFalse(cmsNotificationClient.sendCourseCompletionNotifications(request));
        verify(notificationClient, never()).sendRoleBasedNotification(any());
    }
}
