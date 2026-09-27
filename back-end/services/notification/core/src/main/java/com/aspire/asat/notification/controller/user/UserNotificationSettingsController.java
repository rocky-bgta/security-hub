package com.aspire.asat.notification.controller.user;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsActionDto;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Interface for the end-user notification preferences controller. Individual users can
 * override the role and organization level notification settings for themselves; this is
 * the highest-priority layer of the gate chain.
 */
@Tag(name = "User Notification Settings", description = "APIs for individual users to manage their notification preferences")
@RequestMapping(WebApiUrlConstants.USER_NOTIFICATION_SETTINGS_API)
public interface UserNotificationSettingsController {

    @GetMapping
    @Operation(summary = "Get all notification settings for the current user",
               description = "Retrieve the merged view (effective value + source layer) for every notification type")
    ResponseEntity<ApiResponseDto<List<UserNotificationSettingsResponseDto>>> getAllSettings();

    @GetMapping("/{notificationType}")
    @Operation(summary = "Get notification setting for a specific type",
               description = "Retrieve the merged setting (effective value + source layer) for a single notification type")
    ResponseEntity<ApiResponseDto<UserNotificationSettingsResponseDto>> getSettingByType(
            @PathVariable NotificationType notificationType);

    @PostMapping("/action")
    @Operation(summary = "Execute action on the current user's notification settings",
               description = "Execute ENABLE, DISABLE, UPDATE_CHANNELS or DELETE for a notification type")
    ResponseEntity<ApiResponseDto<UserNotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody UserNotificationSettingsActionDto actionDto);
}
