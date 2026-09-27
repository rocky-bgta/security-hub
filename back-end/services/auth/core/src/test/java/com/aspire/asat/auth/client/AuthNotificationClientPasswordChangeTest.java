package com.aspire.asat.auth.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthNotificationClientPasswordChangeTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AuthNotificationClient authNotificationClient;

    @Test
    void sendPasswordChangeNotificationToUserAndAdmin_dispatchesRoleBasedEventWithoutIdentityByRole() {
        when(notificationClient.sendRoleBasedNotification(any(NotificationEventRequestDto.class))).thenReturn(true);

        AuthNotificationClient.PasswordChangeNotificationData req = new AuthNotificationClient.PasswordChangeNotificationData(
                "user@example.com",
                "user-1",
                "Jane Doe",
                "admin@example.com",
                "admin-1",
                "Acme Admin",
                "2026-05-23 10:00:00",
                "CLIENT_ADMIN"
        );

        authNotificationClient.sendPasswordChangeNotificationToUserAndAdmin(req);

        ArgumentCaptor<NotificationEventRequestDto> captor = ArgumentCaptor.forClass(NotificationEventRequestDto.class);
        verify(notificationClient).sendRoleBasedNotification(captor.capture());

        NotificationEventRequestDto event = captor.getValue();
        assertEquals("user-1", event.getTargetUserId());
        assertEquals(NotificationType.USER_PASSWORD_CHANGE, event.getNotificationType());
        assertTrue(event.getChannels().contains(NotificationChannel.EMAIL));
        assertTrue(event.getChannels().contains(NotificationChannel.IN_APP));
        assertEquals("Jane Doe", event.getTemplateModel().get("userName"));
        assertEquals("2026-05-23 10:00:00", event.getTemplateModel().get("timestamp"));
        assertEquals("user@example.com", event.getTemplateModel().get("userEmail"));
        assertEquals("CLIENT_ADMIN", event.getTemplateModel().get("userType"));
        assertNull(event.getTemplateModelByRole());
    }
}
