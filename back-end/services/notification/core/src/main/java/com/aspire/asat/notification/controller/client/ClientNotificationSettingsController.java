package com.aspire.asat.notification.controller.client;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.client.ClientNotificationSettingsActionDto;
import com.aspire.asat.notification.dto.client.ClientNotificationSettingsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
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
 * Interface for client notification settings controller
 * Allows client admins to manage notification preferences for their organization
 */
@Tag(name = "Client Notification Settings", description = "APIs for client admins to manage notification preferences")
@RequestMapping(WebApiUrlConstants.CLIENT_NOTIFICATION_SETTINGS_API)
public interface ClientNotificationSettingsController {

    @GetMapping
    @Operation(summary = "Get all notification settings for the client", 
               description = "Retrieve all notification settings for the authenticated client admin with pagination")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientNotificationSettingsResponseDto>>>> getAllSettings(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "50", required = false) Integer pageSize);

    @GetMapping("/{notificationType}")
    @Operation(summary = "Get notification setting for a specific type",
               description = "Retrieve notification setting for a specific notification type for the authenticated client")
    ResponseEntity<ApiResponseDto<ClientNotificationSettingsResponseDto>> getSettingByType(
            @PathVariable NotificationType notificationType);


    @PostMapping("/action")
    @Operation(summary = "Execute action on notification settings",
               description = "Execute various actions like ENABLE, DISABLE, UPDATE_CHANNELS, INITIALIZE, DELETE")
    ResponseEntity<ApiResponseDto<ClientNotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody ClientNotificationSettingsActionDto actionDto);
}

