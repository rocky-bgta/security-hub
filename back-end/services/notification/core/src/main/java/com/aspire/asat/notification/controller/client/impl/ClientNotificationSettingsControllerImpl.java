package com.aspire.asat.notification.controller.client.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.controller.client.ClientNotificationSettingsController;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.client.ClientNotificationSettingsActionDto;
import com.aspire.asat.notification.dto.client.ClientNotificationSettingsResponseDto;
import com.aspire.asat.notification.enums.ClientNotificationActionType;
import com.aspire.asat.notification.exception.ResourceNotFoundException;
import com.aspire.asat.notification.model.ClientNotificationSettings;
import com.aspire.asat.notification.service.ClientNotificationSettingsService;
import com.aspire.asat.notification.utils.UserCurrentContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Client notification settings controller implementation
 * Allows client admins to manage their organization's notification preferences
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientNotificationSettingsControllerImpl implements ClientNotificationSettingsController {

    private final ClientNotificationSettingsService clientSettingsService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientNotificationSettingsResponseDto>>>> getAllSettings(
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "50", required = false) Integer pageSize) {

        String clientAdminId = getClientAdminIdFromContext();
        log.info("Client {} requesting all notification settings with offset: {}, pageSize: {}", 
                clientAdminId, offset, pageSize);

        // Get merged settings (client overrides + global defaults)
        List<ClientNotificationSettingsResponseDto> allSettings = clientSettingsService.getAllClientSettingsWithDefaults(clientAdminId);
        
        // Apply pagination manually
        int total = allSettings.size();
        int start = Math.min(offset, total);
        int end = Math.min(start + pageSize, total);
        List<ClientNotificationSettingsResponseDto> paginatedSettings = allSettings.subList(start, end);

        AllResponseDto<List<ClientNotificationSettingsResponseDto>> paginatedResponse =
                new AllResponseDto<>(offset, pageSize, (long) total, paginatedSettings);

        ApiResponseDto<AllResponseDto<List<ClientNotificationSettingsResponseDto>>> response = new ApiResponseDto<>(
                "Client notification settings retrieved successfully (showing defaults if not customized)",
                HttpStatus.OK.value(),
                paginatedResponse
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientNotificationSettingsResponseDto>> getSettingByType(
            @PathVariable NotificationType notificationType) {

        String clientAdminId = getClientAdminIdFromContext();
        log.info("Client {} requesting notification setting for type: {}", clientAdminId, notificationType);

        // Get merged setting (client override if exists, otherwise global default)
        ClientNotificationSettingsResponseDto responseDto = clientSettingsService.getMergedSetting(clientAdminId, notificationType);
        
        ApiResponseDto<ClientNotificationSettingsResponseDto> response = new ApiResponseDto<>(
                responseDto.isCustomized() 
                    ? "Client notification setting retrieved successfully" 
                    : "Using default admin settings (not customized)",
                HttpStatus.OK.value(),
                responseDto
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientNotificationSettingsResponseDto>> executeAction(
            @Valid @RequestBody ClientNotificationSettingsActionDto actionDto) {

        String clientAdminId = getClientAdminIdFromContext();
        log.info("Client {} executing action: {} on notification type: {}",
                clientAdminId, actionDto.getAction(), actionDto.getNotificationType());

        try {
            ClientNotificationSettings result;
            String message;

            switch (actionDto.getAction()) {
                case ENABLE:
                    result = clientSettingsService.enableNotificationType(clientAdminId, actionDto.getNotificationType());
                    message = "Notification type enabled successfully";
                    break;

                case DISABLE:
                    result = clientSettingsService.disableNotificationType(clientAdminId, actionDto.getNotificationType());
                    message = "Notification type disabled successfully";
                    break;

                case UPDATE_CHANNELS:
                    result = handleUpdateChannelsAction(clientAdminId, actionDto);
                    message = "Channel settings updated successfully";
                    break;

                case INITIALIZE:
                    // DEPRECATED: Initialize action is no longer needed
                    // Settings are automatically created when client modifies them
                    ApiResponseDto<ClientNotificationSettingsResponseDto> deprecatedResponse = new ApiResponseDto<>(
                            "Initialize action is deprecated. Settings are automatically created when you modify them.",
                            HttpStatus.METHOD_NOT_ALLOWED.value(),
                            null
                    );
                    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(deprecatedResponse);

                case DELETE:
                    clientSettingsService.deleteClientSetting(clientAdminId, actionDto.getNotificationType());
                    message = "Client notification setting deleted successfully";
                    result = null;
                    break;

                default:
                    ApiResponseDto<ClientNotificationSettingsResponseDto> errorResponse = new ApiResponseDto<>(
                            "Invalid action: " + actionDto.getAction(),
                            HttpStatus.BAD_REQUEST.value(),
                            null
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (result == null && actionDto.getAction() != ClientNotificationActionType.DELETE) {
                ApiResponseDto<ClientNotificationSettingsResponseDto> notFoundResponse = new ApiResponseDto<>(
                        "Notification setting not found for type: " + actionDto.getNotificationType(),
                        HttpStatus.NOT_FOUND.value(),
                        null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            if (result == null) {
                // For DELETE action, return success without data
                ApiResponseDto<ClientNotificationSettingsResponseDto> successResponse = new ApiResponseDto<>(
                        message,
                        HttpStatus.OK.value(),
                        null
                );
                return ResponseEntity.ok(successResponse);
            }

            ClientNotificationSettingsResponseDto responseDto = mapToResponseDto(result);
            ApiResponseDto<ClientNotificationSettingsResponseDto> response = new ApiResponseDto<>(
                    message,
                    HttpStatus.OK.value(),
                    responseDto
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing action {} on notification type {} for client {}: {}",
                    actionDto.getAction(), actionDto.getNotificationType(), clientAdminId, e.getMessage(), e);

            ApiResponseDto<ClientNotificationSettingsResponseDto> errorResponse = new ApiResponseDto<>(
                    "Error executing action: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Handle UPDATE_CHANNELS action
     */
    private ClientNotificationSettings handleUpdateChannelsAction(String clientAdminId, ClientNotificationSettingsActionDto actionDto) {
        return clientSettingsService.updateClientSetting(
                clientAdminId,
                actionDto.getNotificationType(),
                true, // Keep enabled
                actionDto.getEmailEnabled(),
                actionDto.getInAppEnabled(),
                actionDto.getSmsEnabled(),
                actionDto.getPushEnabled(),
                actionDto.getPhoneCallEnabled()
        );
    }

    /**
     * Get client admin ID from current user context
     */
    private String getClientAdminIdFromContext() {
        try {
            CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
            String clientAdminId = context.getUserId();
            
            if (clientAdminId == null || clientAdminId.trim().isEmpty()) {
                log.warn("No clientAdminId found in user context. User ID: {}", context.getUserId());
                throw new ResourceNotFoundException("Client admin ID is required. This endpoint is only accessible to client admins.");
            }
            
            return clientAdminId;
        } catch (Exception e) {
            log.error("Error extracting client admin ID from context: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Unable to determine client admin ID from context", e);
        }
    }

    /**
     * Map entity to response DTO
     */
    private ClientNotificationSettingsResponseDto mapToResponseDto(ClientNotificationSettings settings) {
        return ClientNotificationSettingsResponseDto.builder()
                .id(settings.getId())
                .clientAdminId(settings.getClientAdminId())
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(true)
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

