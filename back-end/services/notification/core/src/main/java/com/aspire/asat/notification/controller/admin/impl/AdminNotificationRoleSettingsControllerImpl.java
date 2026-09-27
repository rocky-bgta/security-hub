package com.aspire.asat.notification.controller.admin.impl;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.admin.AdminNotificationRoleSettingsController;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsActionDto;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsResponseDto;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.service.NotificationRoleSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin controller implementation for the role-based notification settings matrix.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationRoleSettingsControllerImpl implements AdminNotificationRoleSettingsController {

    private final NotificationRoleSettingsService roleSettingsService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> getMatrix(
            String role,
            String notificationType,
            Integer offset,
            Integer pageSize) {

        log.info("Admin requesting role notification settings with role: {}, notificationType: {}, offset: {}, pageSize: {}",
                role, notificationType, offset, pageSize);

        try {
            NotificationRecipientRole roleFilter = parseRoleFilter(role);
            NotificationType notificationTypeFilter = parseNotificationTypeFilter(notificationType);
            int safeOffset = offset == null ? 0 : offset;
            int safePageSize = pageSize == null ? 20 : pageSize;

            Page<NotificationRoleSettings> page = roleSettingsService.getMatrix(
                    notificationTypeFilter, roleFilter, safeOffset, safePageSize);

            List<NotificationRoleSettingsResponseDto> items = page.getContent().stream()
                    .map(settings -> roleSettingsService.toResponseDto(settings, true))
                    .toList();

            AllResponseDto<List<NotificationRoleSettingsResponseDto>> paginatedResponse =
                    new AllResponseDto<>(safeOffset, safePageSize, page.getTotalElements(), items);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Role notification settings matrix retrieved successfully",
                    HttpStatus.OK.value(),
                    paginatedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<NotificationRoleSettingsResponseDto>>> getSettingsForType(NotificationType notificationType) {
        log.info("Admin requesting role notification settings for type: {}", notificationType);

        try {
            NotificationType.requireAdminVisible(notificationType);

            List<NotificationRoleSettingsResponseDto> settings = roleSettingsService.getSettingsForType(notificationType).stream()
                    .map(setting -> roleSettingsService.toResponseDto(setting, true))
                    .toList();

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Role notification settings retrieved successfully",
                    HttpStatus.OK.value(),
                    settings));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<NotificationRoleSettingsResponseDto>> executeAction(
            @Valid @RequestBody NotificationRoleSettingsActionDto actionDto) {

        NotificationType notificationType = actionDto.getNotificationType();
        NotificationRecipientRole role = actionDto.getRole();

        log.info("Admin executing action: {} on notification type: {}, role: {}",
                actionDto.getAction(), notificationType, role);

        try {
            NotificationType.requireAdminVisible(notificationType);

            NotificationRoleSettings result;
            String message;

            switch (actionDto.getAction()) {
                case ENABLE:
                    result = roleSettingsService.enableForRole(notificationType, role);
                    message = "Role notification setting enabled successfully";
                    break;

                case DISABLE:
                    result = roleSettingsService.disableForRole(notificationType, role);
                    message = "Role notification setting disabled successfully";
                    break;

                case UPDATE_CHANNELS:
                    result = roleSettingsService.updateRoleSetting(
                            notificationType, role, true,
                            actionDto.getEmailEnabled(), actionDto.getInAppEnabled(),
                            actionDto.getSmsEnabled(), actionDto.getPushEnabled(), actionDto.getPhoneCallEnabled());
                    message = "Role notification channel settings updated successfully";
                    break;

                case DELETE:
                    roleSettingsService.deleteRoleSetting(notificationType, role);
                    return ResponseEntity.ok(new ApiResponseDto<>(
                            "Role notification setting deleted successfully",
                            HttpStatus.OK.value(),
                            null));

                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(
                            "Invalid action: " + actionDto.getAction(),
                            HttpStatus.BAD_REQUEST.value(),
                            null));
            }

            NotificationRoleSettingsResponseDto responseDto = roleSettingsService.toResponseDto(result, true);
            return ResponseEntity.ok(new ApiResponseDto<>(message, HttpStatus.OK.value(), responseDto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null));
        } catch (Exception e) {
            log.error("Error executing action {} on notification type {}, role {}: {}",
                    actionDto.getAction(), notificationType, role, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>(
                    "Error executing action: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null));
        }
    }

    private static NotificationRecipientRole parseRoleFilter(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return NotificationRecipientRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role filter: " + role);
        }
    }

    private static NotificationType parseNotificationTypeFilter(String notificationType) {
        if (notificationType == null || notificationType.isBlank()) {
            return null;
        }
        try {
            return NotificationType.valueOf(notificationType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid notificationType filter: " + notificationType);
        }
    }
}
