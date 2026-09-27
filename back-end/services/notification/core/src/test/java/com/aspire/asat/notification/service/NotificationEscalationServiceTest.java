package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.client.RegistrationServiceClient;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.service.support.NotificationRecipientResolver;
import com.aspire.asat.notification.service.support.NotificationRecipientTarget;
import com.aspire.asat.notification.service.support.NotificationTemplateModelEnricher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEscalationServiceTest {

    private static final NotificationType TYPE = NotificationType.CERTIFICATE_ISSUED;
    private static final String USER_ID = "user-1";

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private NotificationPreferenceResolver preferenceResolver;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationEscalationService escalationService;

    @BeforeEach
    void setUp() {
        escalationService = new NotificationEscalationService(
                registrationServiceClient,
                recipientResolver,
                new NotificationTemplateModelEnricher(),
                preferenceResolver,
                notificationDeliveryService,
                notificationHistoryService,
                objectMapper);
    }

    private NotificationRequestDto legacyRequest(NotificationRecipientRole recipientRole) {
        return NotificationRequestDto.builder()
                .to("user@example.com")
                .userId(USER_ID)
                .notificationType(TYPE)
                .channels(List.of(NotificationChannel.EMAIL))
                .templateModel(Map.of("courseTitle", "Phishing 101"))
                .recipientRole(recipientRole)
                .build();
    }

    private NotificationRecipientBundleDto bundleWithMspOnly() {
        return NotificationRecipientBundleDto.builder()
                .userId(USER_ID)
                .email("user@example.com")
                .mspUserId("msp-user-1")
                .mspEmail("msp@example.com")
                .aspireAdmins(List.of())
                .build();
    }

    @Test
    void escalate_recipientRoleAlreadySet_skipsEntirely() {
        NotificationRequestDto request = legacyRequest(NotificationRecipientRole.USER);

        escalationService.escalate(request, "parent-log-1");

        verify(registrationServiceClient, never()).getNotificationRecipientBundle(any());
        verify(notificationDeliveryService, never()).deliverNotification(any(), anyString());
    }

    @Test
    void escalate_noAnchorUserIdOrClientAdminId_skipsEntirely() {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .to("user@example.com")
                .notificationType(TYPE)
                .channels(List.of(NotificationChannel.EMAIL))
                .templateModel(Map.of())
                .build();

        escalationService.escalate(request, "parent-log-1");

        verify(registrationServiceClient, never()).getNotificationRecipientBundle(any());
    }

    @Test
    void escalate_mspRoleGatedOff_producesNoSend() {
        NotificationRequestDto request = legacyRequest(null);
        when(registrationServiceClient.getNotificationRecipientBundle(USER_ID)).thenReturn(Optional.of(bundleWithMspOnly()));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.MSP), any()))
                .thenReturn(List.of(new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null)));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.ASPIRE_ADMIN), any())).thenReturn(List.of());
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, null, "msp-user-1")).thenReturn(false);

        escalationService.escalate(request, "parent-log-1");

        verify(notificationDeliveryService, never()).deliverNotification(any(), anyString());
    }

    @Test
    void escalate_mspRoleAllowed_deliversToMsp() {
        NotificationRequestDto request = legacyRequest(null);
        when(registrationServiceClient.getNotificationRecipientBundle(USER_ID)).thenReturn(Optional.of(bundleWithMspOnly()));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.MSP), any()))
                .thenReturn(List.of(new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null)));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.ASPIRE_ADMIN), any())).thenReturn(List.of());
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, null, "msp-user-1")).thenReturn(true);

        escalationService.escalate(request, "parent-log-1");

        verify(notificationDeliveryService, times(1)).deliverNotification(any(), anyString());
        verify(notificationHistoryService).finalizeNotificationLog(anyString(), eq(DeliveryStatus.SUCCESS));
    }

    @Test
    void escalate_twoLegacyRequestsSameEvent_dedupedInProcess() {
        NotificationRequestDto request = legacyRequest(null);
        when(registrationServiceClient.getNotificationRecipientBundle(USER_ID)).thenReturn(Optional.of(bundleWithMspOnly()));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.MSP), any()))
                .thenReturn(List.of(new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null)));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.ASPIRE_ADMIN), any())).thenReturn(List.of());
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, null, "msp-user-1")).thenReturn(true);

        escalationService.escalate(request, "parent-log-1");
        escalationService.escalate(request, "parent-log-2");

        verify(notificationDeliveryService, times(1)).deliverNotification(any(), anyString());
    }

    @Test
    void escalate_registrationServiceFailure_doesNotPropagateAndDoesNotAffectPrimaryDelivery() {
        NotificationRequestDto request = legacyRequest(null);
        when(registrationServiceClient.getNotificationRecipientBundle(USER_ID)).thenThrow(new RuntimeException("registration unreachable"));

        assertDoesNotThrow(() -> escalationService.escalate(request, "parent-log-1"));
        verify(notificationDeliveryService, never()).deliverNotification(any(), anyString());
    }

    @Test
    void escalate_deliveryFailure_isSwallowedAndDoesNotAffectPrimaryDelivery() {
        NotificationRequestDto request = legacyRequest(null);
        NotificationRecipientBundleDto bundle = NotificationRecipientBundleDto.builder()
                .userId(USER_ID)
                .email("user@example.com")
                .mspUserId("msp-user-1")
                .mspEmail("msp@example.com")
                .aspireAdmins(List.of())
                .build();

        when(registrationServiceClient.getNotificationRecipientBundle(USER_ID)).thenReturn(Optional.of(bundle));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.MSP), any()))
                .thenReturn(List.of(new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null)));
        when(recipientResolver.resolveTargets(eq(NotificationRecipientRole.ASPIRE_ADMIN), any())).thenReturn(List.of());
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, null, "msp-user-1")).thenReturn(true);
        lenient().doThrow(new RuntimeException("delivery failed"))
                .when(notificationDeliveryService).deliverNotification(any(), anyString());

        assertDoesNotThrow(() -> escalationService.escalate(request, "parent-log-1"));
        verify(notificationHistoryService).finalizeNotificationLog(anyString(), eq(DeliveryStatus.FAILED));
    }
}
