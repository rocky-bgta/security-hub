package com.aspire.asat.notification.controller.admin.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.admin.AdminNotificationSettingsDynamicController;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationSettingsActionDto;
import com.aspire.asat.notification.dto.admin.NotificationSettingsResponseDto;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.service.NotificationSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Dynamic admin controller implementation for managing notification settings with a single action endpoint
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationSettingsDynamicControllerImpl implements AdminNotificationSettingsDynamicController {

    private static final String NOTIFICATION_SETTINGS_NOT_FOUND_FOR_TYPE = "Notification settings not found for type:";
    private final NotificationSettingsService settingsService;
    private final MessageService messageService;

    public ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationSettingsResponseDto>>>> getAllSettings(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize) {

        log.info("Admin requesting all notification settings with offset: {}, pageSize: {}", offset, pageSize);

        List<NotificationSettings> allSettings = settingsService.getAllSettings();
        List<NotificationSettingsResponseDto> responseDtos = allSettings.stream()
                .map(this::mapToResponseDto)
                .toList();

        AllResponseDto<List<NotificationSettingsResponseDto>> paginatedResponse =
                new AllResponseDto<>(offset, pageSize, (long) responseDtos.size(), responseDtos);

        ApiResponseDto<AllResponseDto<List<NotificationSettingsResponseDto>>> response = new ApiResponseDto<>(
                "Notification settings retrieved successfully",
                HttpStatus.OK.value(),
                paginatedResponse
        );

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<ApiResponseDto<NotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody NotificationSettingsActionDto actionDto) {

        log.info("Admin executing action: {} on notification type: {}",
                actionDto.getAction(), actionDto.getNotificationType());

        try {
            NotificationType notificationType = NotificationType.valueOf(actionDto.getNotificationType().toUpperCase());
            NotificationSettings result;
            String message;

            switch (actionDto.getAction()) {
                case CREATE:
                    result = handleCreateAction(notificationType, actionDto);
                    message = "Notification settings created successfully";
                    break;

                case UPDATE:
                    result = handleUpdateAction(notificationType, actionDto);
                    message = messageService.get(MessageKeys.NOTIFICATION_SETTINGS_UPDATED);
                    break;

                case ENABLE:
                    result = settingsService.enableNotificationType(notificationType);
                    message = "Notification type enabled successfully (all others disabled)";
                    break;

                case DISABLE:
                    result = settingsService.disableNotificationType(notificationType);
                    message = "Notification type disabled successfully";
                    break;

                case ENABLE_CHANNEL:
                    result = handleEnableChannelAction(notificationType, actionDto);
                    message = "Channel enabled successfully";
                    break;

                case DISABLE_CHANNEL:
                    result = handleDisableChannelAction(notificationType, actionDto);
                    message = "Channel disabled successfully";
                    break;

                case DELETE:
                    result = handleDeleteAction(notificationType);
                    message = "Notification settings disabled successfully";
                    break;

                default:
                    ApiResponseDto<NotificationSettingsResponseDto> errorResponse = new ApiResponseDto<>(
                            "Invalid action: " + actionDto.getAction(),
                            HttpStatus.BAD_REQUEST.value(),
                            null
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (result == null) {
                ApiResponseDto<NotificationSettingsResponseDto> notFoundResponse = new ApiResponseDto<>(
                        NOTIFICATION_SETTINGS_NOT_FOUND_FOR_TYPE + " " + notificationType,
                        HttpStatus.NOT_FOUND.value(),
                        null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            NotificationSettingsResponseDto responseDto = mapToResponseDto(result);
            ApiResponseDto<NotificationSettingsResponseDto> response = new ApiResponseDto<>(
                    message,
                    HttpStatus.OK.value(),
                    responseDto
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ApiResponseDto<NotificationSettingsResponseDto> errorResponse = new ApiResponseDto<>(
                    "Invalid notification type: " + actionDto.getNotificationType(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error executing action {} on notification type {}: {}",
                    actionDto.getAction(), actionDto.getNotificationType(), e.getMessage(), e);

            ApiResponseDto<NotificationSettingsResponseDto> errorResponse = new ApiResponseDto<>(
                    "Error executing action: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Handle CREATE action
     */
    private NotificationSettings handleCreateAction(NotificationType notificationType, NotificationSettingsActionDto actionDto) {
        // Check if settings already exist
        if (settingsService.getSettings(notificationType).isPresent()) {
            throw new IllegalArgumentException("Notification settings already exist for type: " + notificationType);
        }

        NotificationSettings settings = NotificationSettings.builder()
                .notificationType(notificationType)
                .enabled(actionDto.getEnabled())
                .description(actionDto.getDescription())
                .emailEnabled(Objects.requireNonNullElse(actionDto.getEmailEnabled(), true))
                .inAppEnabled(Objects.requireNonNullElse(actionDto.getInAppEnabled(), true))
                .smsEnabled(Objects.requireNonNullElse(actionDto.getSmsEnabled(), false))
                .pushEnabled(Objects.requireNonNullElse(actionDto.getPushEnabled(), false))
                .phoneCallEnabled(Objects.requireNonNullElse(actionDto.getPhoneCallEnabled(), false))
                .defaultEmailTemplateId(actionDto.getDefaultEmailTemplateId())
                .defaultInAppTemplateId(actionDto.getDefaultInAppTemplateId())
                .defaultSmsTemplateId(actionDto.getDefaultSmsTemplateId())
                .defaultPushTemplateId(actionDto.getDefaultPushTemplateId())
                .defaultPhoneCallTemplateId(actionDto.getDefaultPhoneCallTemplateId())
                .build();

        return settingsService.saveSettings(settings);
    }

    /**
     * Handle UPDATE action
     */
    private NotificationSettings handleUpdateAction(NotificationType notificationType, NotificationSettingsActionDto actionDto) {
        NotificationSettings existingSettings = settingsService.getSettings(notificationType)
                .orElseThrow(() -> new IllegalArgumentException(NOTIFICATION_SETTINGS_NOT_FOUND_FOR_TYPE + " " + notificationType));

        // Update fields if provided
        if (actionDto.getEnabled() != null) {
            existingSettings.setEnabled(actionDto.getEnabled());
        }
        if (actionDto.getDescription() != null) {
            existingSettings.setDescription(actionDto.getDescription());
        }
        if (actionDto.getEmailEnabled() != null) {
            existingSettings.setEmailEnabled(actionDto.getEmailEnabled());
        }
        if (actionDto.getInAppEnabled() != null) {
            existingSettings.setInAppEnabled(actionDto.getInAppEnabled());
        }
        if (actionDto.getSmsEnabled() != null) {
            existingSettings.setSmsEnabled(actionDto.getSmsEnabled());
        }
        if (actionDto.getPushEnabled() != null) {
            existingSettings.setPushEnabled(actionDto.getPushEnabled());
        }
        if (actionDto.getDefaultEmailTemplateId() != null) {
            existingSettings.setDefaultEmailTemplateId(actionDto.getDefaultEmailTemplateId());
        }
        if (actionDto.getDefaultInAppTemplateId() != null) {
            existingSettings.setDefaultInAppTemplateId(actionDto.getDefaultInAppTemplateId());
        }
        if (actionDto.getDefaultSmsTemplateId() != null) {
            existingSettings.setDefaultSmsTemplateId(actionDto.getDefaultSmsTemplateId());
        }
        if (actionDto.getDefaultPushTemplateId() != null) {
            existingSettings.setDefaultPushTemplateId(actionDto.getDefaultPushTemplateId());
        }

        return settingsService.saveSettings(existingSettings);
    }

    /**
     * Handle ENABLE_CHANNEL action
     */
    private NotificationSettings handleEnableChannelAction(NotificationType notificationType, NotificationSettingsActionDto actionDto) {
        if (actionDto.getChannel() == null) {
            throw new IllegalArgumentException("Channel is required for enable channel action");
        }

        NotificationSettings settings = getNotificationSettings(notificationType);

        final NotificationChannel channel;
        try {
            channel = NotificationChannel.fromCode(actionDto.getChannel().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid channel: " + actionDto.getChannel());
        }

        switch (channel) {
            case EMAIL -> settings.setEmailEnabled(true);
            case IN_APP -> settings.setInAppEnabled(true);
            case SMS -> settings.setSmsEnabled(true);
            case PUSH -> settings.setPushEnabled(true);
            case PHONE_CALL -> settings.setPhoneCallEnabled(true);
        }

        return settingsService.saveSettings(settings);
    }

    /**
     * Handle DISABLE_CHANNEL action
     */
    private NotificationSettings handleDisableChannelAction(NotificationType notificationType, NotificationSettingsActionDto actionDto) {
        if (actionDto.getChannel() == null) {
            throw new IllegalArgumentException("Channel is required for disable channel action");
        }

        NotificationSettings settings = getNotificationSettings(notificationType);

        final NotificationChannel channel;
        try {
            channel = NotificationChannel.fromCode(actionDto.getChannel().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid channel: " + actionDto.getChannel());
        }

        switch (channel) {
            case EMAIL -> settings.setEmailEnabled(false);
            case IN_APP -> settings.setInAppEnabled(false);
            case SMS -> settings.setSmsEnabled(false);
            case PUSH -> settings.setPushEnabled(false);
            case PHONE_CALL -> settings.setPhoneCallEnabled(false);
        }

        return settingsService.saveSettings(settings);
    }

    private NotificationSettings getNotificationSettings(NotificationType notificationType) {
        return settingsService.getSettings(notificationType)
                .orElseThrow(() -> new IllegalArgumentException(NOTIFICATION_SETTINGS_NOT_FOUND_FOR_TYPE + " " + notificationType));
    }

    /**
     * Handle DELETE action
     */
    private NotificationSettings handleDeleteAction(NotificationType notificationType) {
        NotificationSettings settings = getNotificationSettings(notificationType);

        settings.setEnabled(false);
        return settingsService.saveSettings(settings);
    }

    /**
     * Map entity to response DTO
     */
    private NotificationSettingsResponseDto mapToResponseDto(NotificationSettings settings) {
        return NotificationSettingsResponseDto.builder()
                .id(settings.getId())
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .description(settings.getDescription())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .defaultEmailTemplateId(settings.getDefaultEmailTemplateId())
                .defaultInAppTemplateId(settings.getDefaultInAppTemplateId())
                .defaultSmsTemplateId(settings.getDefaultSmsTemplateId())
                .defaultPushTemplateId(settings.getDefaultPushTemplateId())
                .defaultPhoneCallTemplateId(settings.getDefaultPhoneCallTemplateId())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
