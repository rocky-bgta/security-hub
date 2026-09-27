package com.aspire.asat.notification.controller.admin.impl;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.admin.AdminNotificationTemplateDynamicController;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateActionDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateRoleMatrixDto;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.service.NotificationTemplateService;
import com.aspire.asat.notification.service.support.NotificationTemplateRoleMatrix;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dynamic admin controller implementation for managing notification templates with single action endpoint
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationTemplateDynamicControllerImpl implements AdminNotificationTemplateDynamicController {

    /**
     * Filter aliases selecting the role-agnostic base templates.
     */
    private static final Set<String> BASE_ROLE_FILTERS = Set.of("BASE", "NONE");

    private final NotificationTemplateService templateService;

    /**
     * Get all notification templates with pagination
     */
    @GetMapping
    @Operation(summary = "Get all notification templates", 
               description = "Retrieve all notification templates with pagination support")
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> getAllTemplates(
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @RequestParam(value = "recipientRole", required = false) String recipientRole,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize) {

        log.info("Admin requesting notification templates with channel: {}, notificationType: {}, recipientRole: {}, offset: {}, pageSize: {}",
                channel, notificationType, recipientRole, offset, pageSize);

        try {
            NotificationChannel channelFilter = parseChannelFilter(channel);
            NotificationType notificationTypeFilter = parseNotificationTypeFilter(notificationType);
            if (notificationTypeFilter != null) {
                NotificationType.requireAdminVisible(notificationTypeFilter);
            }
            boolean roleFilterPresent = recipientRole != null && !recipientRole.isBlank();
            NotificationRecipientRole roleFilter = parseRecipientRoleFilter(recipientRole);

            List<NotificationTemplate> templates = templateService.getAllTemplates(
                    offset, pageSize, notificationTypeFilter, channelFilter, roleFilter, roleFilterPresent);
            long total = templateService.countAllTemplates(
                    notificationTypeFilter, channelFilter, roleFilter, roleFilterPresent);
            List<NotificationTemplateResponseDto> responseDtos = templates.stream()
                    .map(this::mapToResponseDto)
                    .toList();

            AllResponseDto<List<NotificationTemplateResponseDto>> paginatedResponse =
                    new AllResponseDto<>(offset, pageSize, total, responseDtos);

            ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>> response = new ApiResponseDto<>(
                    "Notification templates retrieved successfully",
                    HttpStatus.OK.value(),
                    paginatedResponse
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        }
    }

    /**
     * Get the base template and per-role status for one notification type and channel.
     */
    @GetMapping("/variants")
    @Operation(summary = "Get the role matrix for a notification type and channel",
               description = "Retrieve the base template plus one entry per recipient role marked CUSTOM or INHERITS_BASE")
    public ResponseEntity<ApiResponseDto<NotificationTemplateRoleMatrixDto>> getRoleMatrix(
            @RequestParam("notificationType") String notificationType,
            @RequestParam("channel") String channel) {

        log.info("Admin requesting role matrix for notificationType: {}, channel: {}", notificationType, channel);

        try {
            NotificationType notificationTypeValue = requireNotificationType(notificationType);
            NotificationType.requireAdminVisible(notificationTypeValue);
            NotificationChannel channelValue = requireChannel(channel);

            NotificationTemplateRoleMatrix matrix = templateService.getRoleMatrix(notificationTypeValue, channelValue);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Notification template role matrix retrieved successfully",
                    HttpStatus.OK.value(),
                    mapToRoleMatrixDto(notificationTypeValue, channelValue, matrix)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(e.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        }
    }

    /**
     * Get notification template details by ID.
     */
    @GetMapping("/{templateId}")
    @Operation(summary = "Get notification template by ID",
               description = "Retrieve full details of a notification template by its ID")
    public ResponseEntity<ApiResponseDto<NotificationTemplateResponseDto>> getTemplateById(
            @PathVariable("templateId") String templateId) {

        log.info("Admin requesting notification template details for id: {}", templateId);

        return templateService.getTemplateById(templateId)
                .map(template -> ResponseEntity.ok(new ApiResponseDto<>(
                        "Notification template retrieved successfully",
                        HttpStatus.OK.value(),
                        mapToResponseDto(template))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>(
                        "Template not found with ID: " + templateId,
                        HttpStatus.NOT_FOUND.value(),
                        null)));
    }

    /**
     * Dynamic action endpoint for notification templates
     */
    @PostMapping("/action")
    @Operation(summary = "Execute action on notification templates", 
               description = "Execute various actions on notification templates with single active constraint")
    public ResponseEntity<ApiResponseDto<Object>> executeAction(
            @Valid @RequestBody NotificationTemplateActionDto actionDto) {
        
        log.info("Admin executing action: {} on template: {}", 
                actionDto.getAction(), actionDto.getTemplateId());
        
        try {
            Object result = null;
            String message = "";
            
            switch (actionDto.getAction()) {
                case UPDATE:
                    result = handleUpdateAction(actionDto);
                    message = "Template updated successfully";
                    break;
                    
                case PREVIEW:
                    result = handlePreviewAction(actionDto);
                    message = "Template preview generated successfully";
                    break;

                case CREATE_ROLE_VARIANT:
                    result = handleCreateRoleVariantAction(actionDto);
                    message = "Role template created successfully";
                    break;

                case DELETE:
                    result = handleDeleteAction(actionDto);
                    message = "Role template deleted successfully";
                    break;

                default:
                    ApiResponseDto<Object> errorResponse = new ApiResponseDto<>(
                            "Invalid action: " + actionDto.getAction(),
                            HttpStatus.BAD_REQUEST.value(),
                            null
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (result == null) {
                ApiResponseDto<Object> notFoundResponse = new ApiResponseDto<>(
                        "Template not found with ID: " + actionDto.getTemplateId(),
                        HttpStatus.NOT_FOUND.value(),
                        null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
            
            ApiResponseDto<Object> response = new ApiResponseDto<>(
                    message,
                    HttpStatus.OK.value(),
                    result
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            ApiResponseDto<Object> errorResponse = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error executing action {} on template {}: {}", 
                    actionDto.getAction(), actionDto.getTemplateId(), e.getMessage(), e);
            
            ApiResponseDto<Object> errorResponse = new ApiResponseDto<>(
                    "Error executing action: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Handle UPDATE action.
     *
     * <p>Only the editable content fields (and the template name) may be changed.
     * Identity/management fields (notification type, channel, recipient role, active/default
     * flags, organization) are intentionally not updatable.
     */
    private NotificationTemplateResponseDto handleUpdateAction(NotificationTemplateActionDto actionDto) {
        if (actionDto.getTemplateId() == null) {
            throw new IllegalArgumentException("Template ID is required for update action");
        }
        
        NotificationTemplate existingTemplate = templateService.getTemplateById(actionDto.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + actionDto.getTemplateId()));

        NotificationType.requireAdminVisible(existingTemplate.getNotificationType());
        
        if (actionDto.getRecipientRole() != null
                && actionDto.getRecipientRole() != existingTemplate.getRecipientRole()) {
            throw new IllegalArgumentException("Recipient role cannot be changed; create a role variant instead");
        }

        if (actionDto.getTemplateName() != null) {
            existingTemplate.setTemplateName(actionDto.getTemplateName());
        }
        if (actionDto.getSubjectTemplate() != null) {
            existingTemplate.setSubjectTemplate(actionDto.getSubjectTemplate());
        }
        if (actionDto.getHtmlTemplate() != null) {
            existingTemplate.setHtmlTemplate(actionDto.getHtmlTemplate());
        }
        if (actionDto.getTextTemplate() != null) {
            existingTemplate.setTextTemplate(actionDto.getTextTemplate());
        }
        if (actionDto.getTitleTemplate() != null) {
            existingTemplate.setTitleTemplate(actionDto.getTitleTemplate());
        }
        if (actionDto.getMessageTemplate() != null) {
            existingTemplate.setMessageTemplate(actionDto.getMessageTemplate());
        }
        
        NotificationTemplate savedTemplate = templateService.updateTemplate(existingTemplate);
        return mapToResponseDto(savedTemplate);
    }

    /**
     * Handle CREATE_ROLE_VARIANT action.
     *
     * <p>Creates a role-specific copy of the existing base template for a notification type and
     * channel. Content fields omitted from the request are inherited from that base template.
     */
    private NotificationTemplateResponseDto handleCreateRoleVariantAction(NotificationTemplateActionDto actionDto) {
        NotificationType notificationType = requireNotificationType(actionDto.getNotificationType());
        NotificationType.requireAdminVisible(notificationType);
        NotificationChannel channel = requireChannel(actionDto.getChannel());

        NotificationTemplate contentOverrides = NotificationTemplate.builder()
                .templateName(actionDto.getTemplateName())
                .subjectTemplate(actionDto.getSubjectTemplate())
                .htmlTemplate(actionDto.getHtmlTemplate())
                .textTemplate(actionDto.getTextTemplate())
                .titleTemplate(actionDto.getTitleTemplate())
                .messageTemplate(actionDto.getMessageTemplate())
                .organizationId(actionDto.getOrganizationId())
                .build();

        NotificationTemplate variant = templateService.createRoleVariant(
                notificationType, channel, actionDto.getRecipientRole(), contentOverrides);
        return mapToResponseDto(variant);
    }

    /**
     * Handle DELETE action.
     *
     * <p>Only role variants can be deleted; the affected role then falls back to the base template.
     * Returns null when the template does not exist so the caller can answer 404.
     */
    private NotificationTemplateResponseDto handleDeleteAction(NotificationTemplateActionDto actionDto) {
        if (actionDto.getTemplateId() == null) {
            throw new IllegalArgumentException("Template ID is required for delete action");
        }

        Optional<NotificationTemplate> existingTemplate = templateService.getTemplateById(actionDto.getTemplateId());
        if (existingTemplate.isEmpty()) {
            return null;
        }

        NotificationType.requireAdminVisible(existingTemplate.get().getNotificationType());

        templateService.deleteRoleVariant(actionDto.getTemplateId());
        return mapToResponseDto(existingTemplate.get());
    }

    /**
     * Handle PREVIEW action, rendering with the supplied sample values when present.
     */
    private String handlePreviewAction(NotificationTemplateActionDto actionDto) {
        if (actionDto.getTemplateId() == null) {
            throw new IllegalArgumentException("Template ID is required for preview action");
        }
        
        NotificationTemplate template = templateService.getTemplateById(actionDto.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + actionDto.getTemplateId()));

        Map<String, Object> previewModel = actionDto.getPreviewModel();

        // Generate preview content based on a channel
        if (template.getChannel() == NotificationChannel.EMAIL) {
            return templateService.generateHtmlContent(template, previewModel);
        } else {
            return templateService.generateMessage(template, previewModel);
        }
    }

    /**
     * Map entity to response DTO
     */
    private NotificationTemplateResponseDto mapToResponseDto(NotificationTemplate template) {
        return NotificationTemplateResponseDto.builder()
                .id(template.getId())
                .notificationType(template.getNotificationType())
                .channel(template.getChannel())
                .recipientRole(template.getRecipientRole())
                .templateName(template.getTemplateName())
                .subjectTemplate(template.getSubjectTemplate())
                .htmlTemplate(template.getHtmlTemplate())
                .textTemplate(template.getTextTemplate())
                .titleTemplate(template.getTitleTemplate())
                .messageTemplate(template.getMessageTemplate())
                .isActive(template.isActive())
                .isDefault(template.isDefault())
                .organizationId(template.getOrganizationId())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    /**
     * Map the resolved role matrix to its response DTO, listing every role in enum order.
     */
    private NotificationTemplateRoleMatrixDto mapToRoleMatrixDto(NotificationType notificationType,
                                                                 NotificationChannel channel,
                                                                 NotificationTemplateRoleMatrix matrix) {
        List<NotificationTemplateRoleMatrixDto.RoleTemplateEntryDto> roles =
                Arrays.stream(NotificationRecipientRole.values())
                        .map(role -> matrix.variantFor(role)
                                .map(variant -> NotificationTemplateRoleMatrixDto.RoleTemplateEntryDto.builder()
                                        .role(role)
                                        .status(NotificationTemplateRoleMatrixDto.RoleTemplateStatus.CUSTOM)
                                        .templateId(variant.getId())
                                        .templateName(variant.getTemplateName())
                                        .isActive(variant.isActive())
                                        .build())
                                .orElseGet(() -> NotificationTemplateRoleMatrixDto.RoleTemplateEntryDto.builder()
                                        .role(role)
                                        .status(NotificationTemplateRoleMatrixDto.RoleTemplateStatus.INHERITS_BASE)
                                        .build()))
                        .toList();

        return NotificationTemplateRoleMatrixDto.builder()
                .notificationType(notificationType)
                .channel(channel)
                .baseTemplate(matrix.baseTemplate() == null ? null : mapToResponseDto(matrix.baseTemplate()))
                .roles(roles)
                .build();
    }

    private static NotificationChannel parseChannelFilter(String channel) {
        if (channel == null || channel.isBlank()) {
            return null;
        }
        try {
            return NotificationChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid channel filter: " + channel);
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

    /**
     * Parse the recipient role filter. {@code BASE} (or {@code NONE}) selects the role-agnostic
     * templates and is therefore represented by a null role.
     */
    private static NotificationRecipientRole parseRecipientRoleFilter(String recipientRole) {
        if (recipientRole == null || recipientRole.isBlank()) {
            return null;
        }
        String normalized = recipientRole.trim().toUpperCase();
        if (BASE_ROLE_FILTERS.contains(normalized)) {
            return null;
        }
        try {
            return NotificationRecipientRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid recipientRole filter: " + recipientRole);
        }
    }

    private static NotificationType requireNotificationType(String notificationType) {
        NotificationType parsed = parseNotificationTypeFilter(notificationType);
        if (parsed == null) {
            throw new IllegalArgumentException("Notification type is required for this action");
        }
        return parsed;
    }

    private static NotificationChannel requireChannel(String channel) {
        NotificationChannel parsed = parseChannelFilter(channel);
        if (parsed == null) {
            throw new IllegalArgumentException("Channel is required for this action");
        }
        return parsed;
    }
}
