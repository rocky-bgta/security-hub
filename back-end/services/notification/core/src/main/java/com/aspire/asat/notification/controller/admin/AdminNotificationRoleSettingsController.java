package com.aspire.asat.notification.controller.admin;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsActionDto;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Interface for the admin role-based notification settings matrix controller.
 * Lets Aspire Admin enable/disable a notification type for a whole recipient role
 * (Aspire Admin, MSP, Client Admin, User) independent of the global and org-level settings.
 */
@Tag(name = "Admin Notification Role Settings", description = "Admin APIs for managing the role-based notification settings matrix")
@RequestMapping(WebApiUrlConstants.ADMIN_NOTIFICATION_ROLE_SETTINGS_API)
public interface AdminNotificationRoleSettingsController {

    @GetMapping
    @Operation(summary = "Get the role-based notification settings matrix",
               description = "Retrieve (notification type, role) rows with optional role and notificationType filters and pagination")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationRoleSettingsResponseDto>>>> getMatrix(
            @Parameter(description = "Filter by recipient role (ASPIRE_ADMIN, MSP, CLIENT_ADMIN, USER)")
            @RequestParam(value = "role", required = false) String role,
            @Parameter(description = "Filter by notification type (e.g. WELCOME_EMAIL, COURSE_COMPLETION)")
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize);

    @GetMapping("/{notificationType}")
    @Operation(summary = "Get role settings for a specific notification type",
               description = "Retrieve the role rows (Aspire Admin, MSP, Client Admin, User) for a single notification type")
    ResponseEntity<ApiResponseDto<List<NotificationRoleSettingsResponseDto>>> getSettingsForType(
            @Parameter(description = "Notification type") @PathVariable NotificationType notificationType);

    @PostMapping("/action")
    @Operation(summary = "Execute action on a role notification setting",
               description = "Execute ENABLE, DISABLE, UPDATE_CHANNELS or DELETE for a (notification type, role) pair")
    ResponseEntity<ApiResponseDto<NotificationRoleSettingsResponseDto>> executeAction(
            @Valid @RequestBody NotificationRoleSettingsActionDto actionDto);
}
