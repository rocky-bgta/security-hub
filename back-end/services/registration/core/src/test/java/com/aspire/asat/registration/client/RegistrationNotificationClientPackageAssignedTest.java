package com.aspire.asat.registration.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.registration.client.service.AuthServiceClient;
import com.aspire.asat.registration.data.auth.request.PasswordResetRequestDto;
import com.aspire.asat.registration.data.auth.response.PasswordResetTokenResponseDto;
import com.aspire.asat.registration.dto.notification.PackageAssignmentNotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationNotificationClientPackageAssignedTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private AuthServiceClient authServiceClient;

    private RegistrationNotificationClient client;

    private RegistrationNotificationClient buildClient() {
        RegistrationNotificationClient c = new RegistrationNotificationClient(notificationClient, authServiceClient);
        setField(c, "tokenExpirySeconds", 3600L);
        setField(c, "resetBaseUrl", "https://portal.example.com/reset");
        return c;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = RegistrationNotificationClient.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field " + fieldName, e);
        }
    }

    @Test
    void sendPackageAssignedNotification_withCredentials_dispatchesCredentialTypeWithResetUrl() {
        client = buildClient();
        when(authServiceClient.generatePasswordResetToken(any(PasswordResetRequestDto.class)))
                .thenReturn(PasswordResetTokenResponseDto.builder().token("tok-123").build());
        when(notificationClient.sendRoleBasedNotification(any(NotificationEventRequestDto.class))).thenReturn(true);

        PackageAssignmentNotificationDto request = PackageAssignmentNotificationDto.builder()
                .userEmail("user@example.com")
                .userId("user-1")
                .userName("Jane Doe")
                .packageName("Security 101")
                .adminName("Acme Admin")
                .build();

        client.sendPackageAssignedNotification(request, true);

        ArgumentCaptor<NotificationEventRequestDto> captor = ArgumentCaptor.forClass(NotificationEventRequestDto.class);
        verify(notificationClient).sendRoleBasedNotification(captor.capture());

        NotificationEventRequestDto event = captor.getValue();
        assertEquals("user-1", event.getTargetUserId());
        assertEquals(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, event.getNotificationType());
        assertTrue(event.getChannels().contains(NotificationChannel.EMAIL));
        assertTrue(event.getChannels().contains(NotificationChannel.IN_APP));
        assertTrue(((String) event.getTemplateModel().get("resetUrl")).contains("tok-123"));
        assertNull(event.getTemplateModelByRole());
    }

    @Test
    void sendPackageAssignedNotification_withoutCredentials_dispatchesUserTypeWithoutTokenLookup() {
        client = buildClient();
        when(notificationClient.sendRoleBasedNotification(any(NotificationEventRequestDto.class))).thenReturn(true);

        PackageAssignmentNotificationDto request = PackageAssignmentNotificationDto.builder()
                .userEmail("user@example.com")
                .userId("user-1")
                .userName("Jane Doe")
                .packageName("Security 101")
                .adminName("Acme Admin")
                .build();

        client.sendPackageAssignedNotification(request, false);

        ArgumentCaptor<NotificationEventRequestDto> captor = ArgumentCaptor.forClass(NotificationEventRequestDto.class);
        verify(notificationClient).sendRoleBasedNotification(captor.capture());
        verify(authServiceClient, never()).generatePasswordResetToken(any());

        assertEquals(NotificationType.PACKAGE_ASSIGNED_USER, captor.getValue().getNotificationType());
    }

    @Test
    void sendPackageAssignedNotification_tokenGenerationFails_stillDispatchesNotification() {
        client = buildClient();
        when(authServiceClient.generatePasswordResetToken(any(PasswordResetRequestDto.class))).thenReturn(null);
        when(notificationClient.sendRoleBasedNotification(any(NotificationEventRequestDto.class))).thenReturn(true);

        PackageAssignmentNotificationDto request = PackageAssignmentNotificationDto.builder()
                .userEmail("user@example.com")
                .userId("user-1")
                .userName("Jane Doe")
                .packageName("Security 101")
                .adminName("Acme Admin")
                .build();

        client.sendPackageAssignedNotification(request, true);

        ArgumentCaptor<NotificationEventRequestDto> captor = ArgumentCaptor.forClass(NotificationEventRequestDto.class);
        verify(notificationClient).sendRoleBasedNotification(captor.capture());
        assertFalse(captor.getValue().getTemplateModel().containsKey("resetUrl"));
    }
}
