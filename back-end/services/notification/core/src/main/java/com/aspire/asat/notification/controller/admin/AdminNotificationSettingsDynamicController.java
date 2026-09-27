package com.aspire.asat.notification.controller.admin;

import com.aspire.asat.notification.constant.WebApiUrlConstants;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationSettingsActionDto;
import com.aspire.asat.notification.dto.admin.NotificationSettingsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Interface for dynamic admin notification settings controller
 * Handles dynamic actions on notification settings with a single active constraint
 */
@Tag(name = "Admin Notification Settings Dynamic", description = "Dynamic admin APIs for managing notification settings")
@RequestMapping(WebApiUrlConstants.ADMIN_NOTIFICATION_SETTINGS_DYNAMIC_API)
public interface AdminNotificationSettingsDynamicController {

    @GetMapping
    @Operation(summary = "Get all notification settings", 
               description = "Retrieve all notification settings with pagination support")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationSettingsResponseDto>>>> getAllSettings(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize);

    @PostMapping("/action")
    @Operation(summary = "Execute action on notification settings", 
               description = "Execute various actions on notification settings with single active constraint")
    ResponseEntity<ApiResponseDto<NotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody NotificationSettingsActionDto actionDto);
}