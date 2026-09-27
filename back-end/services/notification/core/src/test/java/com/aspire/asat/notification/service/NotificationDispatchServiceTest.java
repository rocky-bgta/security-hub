package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.client.RegistrationServiceClient;
import com.aspire.asat.notification.dto.dispatch.NotificationDispatchResultDto;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.service.support.NotificationRecipientResolver;
import com.aspire.asat.notification.service.support.NotificationRecipientTarget;
import com.aspire.asat.notification.service.support.NotificationTemplateModelEnricher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    private static final NotificationType TYPE = NotificationType.CERTIFICATE_ISSUED;
    private static final String TARGET_USER_ID = "user-1";

    @Mock
    private NotificationSettingsService globalSettingsService;

    @Mock
    private NotificationPreferenceResolver preferenceResolver;

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new NotificationDispatchService(
                globalSettingsService,
                preferenceResolver,
                registrationServiceClient,
                recipientResolver,
                new NotificationTemplateModelEnricher(),
                notificationDeliveryService,
                notificationHistoryService);
    }

    private NotificationEventRequestDto baseEvent() {
        return NotificationEventRequestDto.builder()
                .targetUserId(TARGET_USER_ID)
                .notificationType(TYPE)
                .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                .templateModel(Map.of("courseTitle", "Phishing 101"))
                .build();
    }

    private NotificationRecipientBundleDto fullBundle() {
        return NotificationRecipientBundleDto.builder()
                .userId("user-1")
                .userFullName("Jane Doe")
                .email("user@example.com")
                .clientAdminId("client-admin-1")
                .clientAdminUserId("admin-user-1")
                .clientAdminEmail("admin@example.com")
                .clientAdminName("Acme Admin")
                .mspUserId("msp-user-1")
                .mspEmail("msp@example.com")
                .mspName("MSP Co")
                .organizationName("Acme Org")
                .aspireAdmins(List.of())
                .build();
    }

    @Test
    void dispatch_globalDisabled_returnsEmptyResultAndSkipsAllRoles() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(false);

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(0, result.getSentCount());
        assertEquals(4, result.getSkippedRoles().size());
        verify(registrationServiceClient, never()).getNotificationRecipientBundle(any());
    }

    @Test
    void dispatch_bundleNotResolved_returnsEmptyResult() {
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.empty());

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(0, result.getSentCount());
        assertEquals(4, result.getSkippedRoles().size());
        verify(notificationDeliveryService, never()).deliverNotification(any(), any());
    }

    @Test
    void dispatch_allRolesResolvedAndAllowed_sendsToEachRoleAndReturnsSentCount() {
        NotificationRecipientBundleDto bundle = fullBundle();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        stubTarget(NotificationRecipientRole.USER, bundle,
                new NotificationRecipientTarget("user-1", "Jane Doe", "user@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.CLIENT_ADMIN, bundle,
                new NotificationRecipientTarget("admin-user-1", "Acme Admin", "admin@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.MSP, bundle,
                new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null));
        stubTarget(NotificationRecipientRole.ASPIRE_ADMIN, bundle, List.of());

        when(preferenceResolver.isTypeAllowed(eq(TYPE), any(), any(), any())).thenReturn(true);

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(3, result.getSentCount());
        assertEquals(List.of(NotificationRecipientRole.ASPIRE_ADMIN), result.getSkippedRoles());
        verify(notificationDeliveryService, times(3)).deliverNotification(any(), anyString());
        verify(notificationHistoryService, times(3)).finalizeNotificationLog(anyString(), eq(DeliveryStatus.SUCCESS));
    }

    @Test
    void dispatch_enrichesTemplateModelWithRegistrationIdentityWithoutCallerByRole() {
        NotificationRecipientBundleDto bundle = fullBundle();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        stubTarget(NotificationRecipientRole.USER, bundle, List.of());
        stubTarget(NotificationRecipientRole.CLIENT_ADMIN, bundle,
                new NotificationRecipientTarget("admin-user-1", "Acme Admin", "admin@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.MSP, bundle,
                new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null));
        stubTarget(NotificationRecipientRole.ASPIRE_ADMIN, bundle, List.of());

        when(preferenceResolver.isTypeAllowed(eq(TYPE), any(), any(), any())).thenReturn(true);

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());
        assertEquals(2, result.getSentCount());

        ArgumentCaptor<NotificationRequestDto> requestCaptor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationDeliveryService, times(2)).deliverNotification(requestCaptor.capture(), anyString());

        NotificationRequestDto clientAdminRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getRecipientRole() == NotificationRecipientRole.CLIENT_ADMIN)
                .findFirst()
                .orElseThrow();
        Map<String, Object> clientAdminModel = clientAdminRequest.getTemplateModel();
        assertEquals("Phishing 101", clientAdminModel.get("courseTitle"));
        assertEquals("Acme Admin", clientAdminModel.get(NotificationTemplateValue.CLIENT_ADMIN_NAME));
        assertEquals("MSP Co", clientAdminModel.get(NotificationTemplateValue.MSP_NAME));
        assertEquals("msp@example.com", clientAdminModel.get(NotificationTemplateValue.MSP_EMAIL));
        assertEquals("Acme Org", clientAdminModel.get(NotificationTemplateValue.ORGANIZATION_NAME));
        assertEquals("Acme Admin", clientAdminModel.get(NotificationTemplateValue.ADMIN_NAME));
        assertNull(baseEvent().getTemplateModelByRole());

        NotificationRequestDto mspRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getRecipientRole() == NotificationRecipientRole.MSP)
                .findFirst()
                .orElseThrow();
        assertEquals("MSP Co", mspRequest.getTemplateModel().get(NotificationTemplateValue.ADMIN_NAME));
        assertEquals("MSP Co", mspRequest.getTemplateModel().get(NotificationTemplateValue.MSP_NAME));
    }

    @Test
    void dispatch_roleGatedOff_skipsThatRoleOnly() {
        NotificationRecipientBundleDto bundle = fullBundle();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        stubTarget(NotificationRecipientRole.USER, bundle,
                new NotificationRecipientTarget("user-1", "Jane Doe", "user@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.CLIENT_ADMIN, bundle,
                new NotificationRecipientTarget("admin-user-1", "Acme Admin", "admin@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.MSP, bundle,
                new NotificationRecipientTarget("msp-user-1", "MSP Co", "msp@example.com", null, null));
        stubTarget(NotificationRecipientRole.ASPIRE_ADMIN, bundle, List.of());

        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.USER, "client-admin-1", "user-1")).thenReturn(true);
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.CLIENT_ADMIN, "client-admin-1", "admin-user-1")).thenReturn(true);
        when(preferenceResolver.isTypeAllowed(TYPE, NotificationRecipientRole.MSP, null, "msp-user-1")).thenReturn(false);

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(2, result.getSentCount());
        assertTrue(result.getSkippedRoles().contains(NotificationRecipientRole.MSP));
        assertTrue(result.getSkippedRoles().contains(NotificationRecipientRole.ASPIRE_ADMIN));
        verify(notificationDeliveryService, never()).deliverNotification(
                argThatRecipientRole(NotificationRecipientRole.MSP), anyString());
    }

    @Test
    void dispatch_perRecipientDeliveryFailure_isolatedFromOtherRecipients() {
        NotificationRecipientBundleDto bundle = fullBundle();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        stubTarget(NotificationRecipientRole.USER, bundle,
                new NotificationRecipientTarget("user-1", "Jane Doe", "user@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.CLIENT_ADMIN, bundle,
                new NotificationRecipientTarget("admin-user-1", "Acme Admin", "admin@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.MSP, bundle, List.of());
        stubTarget(NotificationRecipientRole.ASPIRE_ADMIN, bundle, List.of());

        when(preferenceResolver.isTypeAllowed(eq(TYPE), any(), any(), any())).thenReturn(true);
        doThrow(new RuntimeException("SMTP down"))
                .when(notificationDeliveryService).deliverNotification(
                        argThatRecipientRole(NotificationRecipientRole.USER), anyString());

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(1, result.getSentCount());
        assertTrue(result.getSkippedRoles().contains(NotificationRecipientRole.USER));
        verify(notificationHistoryService).finalizeNotificationLog(anyString(), eq(DeliveryStatus.FAILED));
        verify(notificationHistoryService).finalizeNotificationLog(anyString(), eq(DeliveryStatus.SUCCESS));
    }

    @Test
    void dispatch_missingMspAndAspireAdminRecipients_addsThemToSkippedRoles() {
        NotificationRecipientBundleDto bundle = NotificationRecipientBundleDto.builder()
                .userId("user-1")
                .userFullName("Jane Doe")
                .email("user@example.com")
                .clientAdminId("client-admin-1")
                .clientAdminUserId("admin-user-1")
                .clientAdminEmail("admin@example.com")
                .clientAdminName("Acme Admin")
                .aspireAdmins(List.of())
                .build();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        stubTarget(NotificationRecipientRole.USER, bundle,
                new NotificationRecipientTarget("user-1", "Jane Doe", "user@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.CLIENT_ADMIN, bundle,
                new NotificationRecipientTarget("admin-user-1", "Acme Admin", "admin@example.com", null, "client-admin-1"));
        stubTarget(NotificationRecipientRole.MSP, bundle, List.of());
        stubTarget(NotificationRecipientRole.ASPIRE_ADMIN, bundle, List.of());

        lenient().when(preferenceResolver.isTypeAllowed(eq(TYPE), any(), any(), any())).thenReturn(true);

        NotificationDispatchResultDto result = dispatchService.dispatch(baseEvent());

        assertEquals(2, result.getSentCount());
        assertTrue(result.getSkippedRoles().contains(NotificationRecipientRole.MSP));
        assertTrue(result.getSkippedRoles().contains(NotificationRecipientRole.ASPIRE_ADMIN));
    }

    @Test
    void dispatch_explicitRecipientRolesSubset_onlyDispatchesToThoseRoles() {
        NotificationRecipientBundleDto bundle = fullBundle();
        when(globalSettingsService.isNotificationTypeEnabled(TYPE)).thenReturn(true);
        when(registrationServiceClient.getNotificationRecipientBundle(TARGET_USER_ID)).thenReturn(Optional.of(bundle));

        NotificationEventRequestDto event = NotificationEventRequestDto.builder()
                .targetUserId(TARGET_USER_ID)
                .notificationType(TYPE)
                .channels(List.of(NotificationChannel.EMAIL))
                .templateModel(Map.of())
                .recipientRoles(List.of(NotificationRecipientRole.USER))
                .build();

        stubTarget(NotificationRecipientRole.USER, bundle,
                new NotificationRecipientTarget("user-1", "Jane Doe", "user@example.com", null, "client-admin-1"));
        when(preferenceResolver.isTypeAllowed(eq(TYPE), eq(NotificationRecipientRole.USER), any(), any())).thenReturn(true);

        NotificationDispatchResultDto result = dispatchService.dispatch(event);

        assertEquals(1, result.getSentCount());
        assertEquals(List.of(), result.getSkippedRoles());
        verify(recipientResolver, never()).resolveTargets(eq(NotificationRecipientRole.MSP), any());
        verify(recipientResolver, never()).resolveTargets(eq(NotificationRecipientRole.CLIENT_ADMIN), any());
        verify(recipientResolver, never()).resolveTargets(eq(NotificationRecipientRole.ASPIRE_ADMIN), any());
    }

    private void stubTarget(NotificationRecipientRole role, NotificationRecipientBundleDto bundle, NotificationRecipientTarget target) {
        stubTarget(role, bundle, List.of(target));
    }

    private void stubTarget(NotificationRecipientRole role, NotificationRecipientBundleDto bundle, List<NotificationRecipientTarget> targets) {
        lenient().when(recipientResolver.resolveTargets(role, bundle)).thenReturn(targets);
    }

    private static NotificationRequestDto argThatRecipientRole(NotificationRecipientRole role) {
        return org.mockito.ArgumentMatchers.argThat(request -> request != null && request.getRecipientRole() == role);
    }
}
