package com.aspire.asat.notification.controller.user.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.user.UserNotificationSettingsController;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsActionDto;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsResponseDto;
import com.aspire.asat.notification.exception.ResourceNotFoundException;
import com.aspire.asat.notification.model.UserNotificationSettings;
import com.aspire.asat.notification.service.UserNotificationSettingsService;
import com.aspire.asat.notification.utils.UserCurrentContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * End-user notification preferences controller implementation.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserNotificationSettingsControllerImpl implements UserNotificationSettingsController {

    private final UserNotificationSettingsService userSettingsService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<List<UserNotificationSettingsResponseDto>>> getAllSettings() {
        CurrentUserContext context = getContextOrThrow();
        log.info("User {} requesting all notification settings", context.getUserId());

        List<UserNotificationSettingsResponseDto> settings = userSettingsService.getAllForUser(
                context.getUserId(), context.getClientAdminId(), resolveRole(context));

        return ResponseEntity.ok(new ApiResponseDto<>(
                "User notification settings retrieved successfully", HttpStatus.OK.value(), settings));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserNotificationSettingsResponseDto>> getSettingByType(NotificationType notificationType) {
        CurrentUserContext context = getContextOrThrow();
        log.info("User {} requesting notification setting for type: {}", context.getUserId(), notificationType);

        UserNotificationSettingsResponseDto setting = userSettingsService.getMergedSetting(
                context.getUserId(), context.getClientAdminId(), resolveRole(context), notificationType);

        return ResponseEntity.ok(new ApiResponseDto<>(
                setting.isCustomized() ? "User notification setting retrieved successfully" : "Using inherited settings (not customized)",
                HttpStatus.OK.value(), setting));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserNotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody UserNotificationSettingsActionDto actionDto) {
        CurrentUserContext context = getContextOrThrow();
        String userId = context.getUserId();
        NotificationType notificationType = actionDto.getNotificationType();

        log.info("User {} executing action: {} on notification type: {}", userId, actionDto.getAction(), notificationType);

        try {
            UserNotificationSettings result;
            String message;

            switch (actionDto.getAction()) {
                case ENABLE:
                    result = userSettingsService.enableForUser(userId, notificationType);
                    message = "Notification type enabled successfully";
                    break;

                case DISABLE:
                    result = userSettingsService.disableForUser(userId, notificationType);
                    message = "Notification type disabled successfully";
                    break;

                case UPDATE_CHANNELS:
                    result = userSettingsService.updateUserSetting(userId, notificationType, true,
                            actionDto.getEmailEnabled(), actionDto.getInAppEnabled(),
                            actionDto.getSmsEnabled(), actionDto.getPushEnabled(), actionDto.getPhoneCallEnabled());
                    message = "Channel settings updated successfully";
                    break;

                case DELETE:
                    userSettingsService.deleteUserSetting(userId, notificationType);
                    return ResponseEntity.ok(new ApiResponseDto<>(
                            "User notification setting deleted successfully", HttpStatus.OK.value(), null));

                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(
                            "Invalid action: " + actionDto.getAction(), HttpStatus.BAD_REQUEST.value(), null));
            }

            UserNotificationSettingsResponseDto responseDto = userSettingsService.toResponseDto(result, true, "USER");
            return ResponseEntity.ok(new ApiResponseDto<>(message, HttpStatus.OK.value(), responseDto));

        } catch (Exception e) {
            log.error("Error executing action {} on notification type {} for user {}: {}",
                    actionDto.getAction(), notificationType, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>(
                    "Error executing action: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    private CurrentUserContext getContextOrThrow() {
        try {
            CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
            if (context.getUserId() == null || context.getUserId().trim().isEmpty()) {
                throw new ResourceNotFoundException("User ID is required to manage notification preferences");
            }
            return context;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting user context: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Unable to determine current user from context", e);
        }
    }

    private NotificationRecipientRole resolveRole(CurrentUserContext context) {
        return NotificationRecipientRole.fromUserType(context.getUserType());
    }
}
