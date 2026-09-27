package com.aspire.asat.notification.controller.admin;

import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateActionDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateRoleMatrixDto;
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
 * Interface for dynamic admin notification templates controller
 * Handles dynamic actions on notification templates with single active constraint
 */
@Tag(name = "Admin Notification Templates Dynamic", description = "Dynamic admin APIs for managing notification templates")
@RequestMapping("/api/v1/admin/notification-templates")
public interface AdminNotificationTemplateDynamicController {

    @GetMapping
    @Operation(summary = "Get all notification templates",
               description = "Retrieve notification templates with optional channel, notificationType and recipientRole filters and pagination support")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> getAllTemplates(
            @Parameter(description = "Filter by notification channel (e.g. EMAIL, IN_APP, SMS)")
            @RequestParam(value = "channel", required = false) String channel,
            @Parameter(description = "Filter by notification type (e.g. NEW_USER_REGISTERED, WELCOME_EMAIL)")
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @Parameter(description = "Filter by recipient role (ASPIRE_ADMIN, MSP, CLIENT_ADMIN, USER) or BASE for the role-agnostic templates")
            @RequestParam(value = "recipientRole", required = false) String recipientRole,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize);

    @GetMapping("/variants")
    @Operation(summary = "Get the role matrix for a notification type and channel",
               description = "Retrieve the base template plus one entry per recipient role, marked CUSTOM when the role "
                       + "has its own template or INHERITS_BASE when it falls back to the base template")
    ResponseEntity<ApiResponseDto<NotificationTemplateRoleMatrixDto>> getRoleMatrix(
            @Parameter(description = "Notification type (e.g. WELCOME_EMAIL)", required = true)
            @RequestParam("notificationType") String notificationType,
            @Parameter(description = "Notification channel (e.g. EMAIL, IN_APP, SMS)", required = true)
            @RequestParam("channel") String channel);

    @GetMapping("/{templateId}")
    @Operation(summary = "Get notification template by ID",
               description = "Retrieve full details of a notification template by its ID")
    ResponseEntity<ApiResponseDto<NotificationTemplateResponseDto>> getTemplateById(
            @Parameter(description = "Notification template ID")
            @PathVariable("templateId") String templateId);

    @PostMapping("/action")
    @Operation(summary = "Execute action on notification templates", 
               description = "Execute various actions on notification templates with single active constraint")
    ResponseEntity<ApiResponseDto<Object>> executeAction(
            @Valid @RequestBody NotificationTemplateActionDto actionDto);
}