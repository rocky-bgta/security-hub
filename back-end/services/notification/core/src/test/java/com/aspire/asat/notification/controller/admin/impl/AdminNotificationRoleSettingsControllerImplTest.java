package com.aspire.asat.notification.controller.admin.impl;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsResponseDto;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.service.NotificationRoleSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationRoleSettingsControllerImplTest {

    @Mock
    private NotificationRoleSettingsService roleSettingsService;

    @InjectMocks
    private AdminNotificationRoleSettingsControllerImpl controller;

    @Test
    void getMatrix_returnsPaginatedResponse() {
        NotificationRoleSettings settings = NotificationRoleSettings.builder()
                .id("row-1")
                .notificationType(NotificationType.WELCOME_EMAIL)
                .role(NotificationRecipientRole.MSP)
                .enabled(true)
                .build();
        when(roleSettingsService.getMatrix(isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(settings)));
        when(roleSettingsService.toResponseDto(settings, true))
                .thenReturn(NotificationRoleSettingsResponseDto.builder()
                        .id("row-1")
                        .notificationType(NotificationType.WELCOME_EMAIL)
                        .role(NotificationRecipientRole.MSP)
                        .enabled(true)
                        .build());

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> response =
                controller.getMatrix(null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        assertEquals(1, response.getBody().getData().getItems().size());
        assertEquals(0, response.getBody().getData().getOffset());
        assertEquals(20, response.getBody().getData().getPageSize());
    }

    @Test
    void getMatrix_appliesRoleAndNotificationTypeFilters() {
        when(roleSettingsService.getMatrix(
                eq(NotificationType.WELCOME_EMAIL), eq(NotificationRecipientRole.MSP), eq(1), eq(10)))
                .thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> response =
                controller.getMatrix("msp", "welcome_email", 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(roleSettingsService).getMatrix(
                NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, 1, 10);
    }

    @Test
    void getMatrix_invalidRoleFilter_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> response =
                controller.getMatrix("SUPERVISOR", null, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(roleSettingsService, never()).getMatrix(any(), any(), anyInt(), anyInt());
    }

    @Test
    void getMatrix_invalidNotificationTypeFilter_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> response =
                controller.getMatrix(null, "NOT_A_TYPE", 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(roleSettingsService, never()).getMatrix(any(), any(), anyInt(), anyInt());
    }

    @Test
    void getMatrix_hiddenNotificationType_propagatesBadRequest() {
        when(roleSettingsService.getMatrix(eq(NotificationType.PACKAGE_ASSIGNED), isNull(), eq(0), eq(20)))
                .thenThrow(new IllegalArgumentException(
                        "Notification type is not managed in the admin portal: PACKAGE_ASSIGNED"));

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> response =
                controller.getMatrix(null, "PACKAGE_ASSIGNED", 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification type is not managed in the admin portal: PACKAGE_ASSIGNED",
                response.getBody().getMessage());
    }

    @Test
    void getSettingsForType_hiddenNotificationType_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<List<NotificationRoleSettingsResponseDto>>> response =
                controller.getSettingsForType(NotificationType.COURSE_COMPLETION);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(roleSettingsService, never()).getSettingsForType(any());
    }

    @Test
    void executeAction_hiddenNotificationType_returnsBadRequest() {
        com.aspire.asat.notification.dto.role.NotificationRoleSettingsActionDto actionDto =
                com.aspire.asat.notification.dto.role.NotificationRoleSettingsActionDto.builder()
                        .action(com.aspire.asat.notification.enums.ClientNotificationActionType.ENABLE)
                        .notificationType(NotificationType.USER_PROFILE_UPDATED)
                        .role(NotificationRecipientRole.USER)
                        .build();

        ResponseEntity<ApiResponseDto<NotificationRoleSettingsResponseDto>> response =
                controller.executeAction(actionDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(roleSettingsService, never()).enableForRole(any(), any());
    }
}
