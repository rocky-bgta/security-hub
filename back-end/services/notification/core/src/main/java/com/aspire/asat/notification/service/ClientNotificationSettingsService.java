package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.exception.AspireException;
import com.aspire.asat.notification.dto.client.ClientNotificationSettingsResponseDto;
import com.aspire.asat.notification.model.ClientNotificationSettings;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.repository.ClientNotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing client-specific notification settings
 * Handles enable/disable of notification types per client organization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientNotificationSettingsService {

    private final ClientNotificationSettingsRepository repository;
    private final NotificationSettingsService globalSettingsService;


    public boolean isNotificationTypeEnabledForClient(String clientAdminId, NotificationType notificationType) {
        // Step 1: First check Global Notification Settings - must be enabled
        boolean globalEnabled = globalSettingsService.isNotificationTypeEnabled(notificationType);
        if (!globalEnabled) {
            // Global setting is disabled, reject immediately
            return false;
        }

        // Step 2: Global is enabled, now check client-specific settings if clientAdminId is provided
        if (clientAdminId == null || clientAdminId.trim().isEmpty()) {
            // No client context, global is enabled so return true
            return true;
        }

        // Step 3: Check if a client-specific setting exists (optional feature)
        Optional<ClientNotificationSettings> clientSetting = repository.findByClientAdminIdAndNotificationType(clientAdminId, notificationType);

        // Client-wise setting exists, use client preference (override global)
        return clientSetting.map(ClientNotificationSettings::isEnabled).orElse(true);

        // Client-wise setting does NOT exist, skip client check and use global (already enabled)
    }


    public boolean isChannelEnabledForClient(String clientAdminId, NotificationType notificationType, NotificationChannel channel) {
        // Step 1: First check Global Notification Settings - must be enabled for both type and channel
        boolean globalTypeEnabled = globalSettingsService.isNotificationTypeEnabled(notificationType);
        if (!globalTypeEnabled) {
            // Global notification type is disabled, reject immediately
            return false;
        }

        boolean globalChannelEnabled = globalSettingsService.isChannelEnabled(notificationType, channel);
        if (!globalChannelEnabled) {
            // Global channel is disabled, reject immediately
            return false;
        }

        // Step 2: Global is enabled, now check client-specific settings if clientAdminId is provided
        if (clientAdminId == null || clientAdminId.trim().isEmpty()) {
            // No client context, global is enabled so return true
            return true;
        }

        // Step 3: Check if a client-specific setting exists (optional feature)
        Optional<ClientNotificationSettings> clientSetting = repository.findByClientAdminIdAndNotificationType(clientAdminId, notificationType);

        if (clientSetting.isPresent()) {
            // Client-wise setting exists, use client preference (override global)
            ClientNotificationSettings setting = clientSetting.get();
            // Check both notification type enabled and channel enabled
            return setting.isEnabled() && setting.isChannelEnabled(channel);
        }

        // Client-wise setting does NOT exist, skip client check and use global (already enabled)
        return true;
    }

    public List<ClientNotificationSettingsResponseDto> getAllClientSettingsWithDefaults(String clientAdminId) {
        // Get all global settings
        List<NotificationSettings> globalSettings = globalSettingsService.getAllSettings();

        // Get all client-specific settings
        List<ClientNotificationSettings> clientSettings = repository.findByClientAdminId(clientAdminId);
        Map<NotificationType, ClientNotificationSettings> clientSettingsMap = clientSettings.stream()
                .collect(Collectors.toMap(ClientNotificationSettings::getNotificationType, s -> s));

        // Build merged response - show client settings if they exist, otherwise global defaults
        return globalSettings.stream()
                .map(global -> {
                    ClientNotificationSettings clientSetting = clientSettingsMap.get(global.getNotificationType());
                    if (clientSetting != null) {
                        // Client has custom setting, use it
                        return mapToResponseDto(clientSetting, true);
                    } else {
                        // No client setting, return global default
                        return mapGlobalToResponseDto(global, clientAdminId);
                    }
                })
                .toList();
    }

    /**
     * Get merged setting for a specific notification type
     * Returns client setting if exists, otherwise global default
     */
    public ClientNotificationSettingsResponseDto getMergedSetting(String clientAdminId, NotificationType notificationType) {
        Optional<ClientNotificationSettings> clientSetting = repository.findByClientAdminIdAndNotificationType(clientAdminId, notificationType);

        if (clientSetting.isPresent()) {
            // Client has custom setting
            return mapToResponseDto(clientSetting.get(), true);
        } else {
            // Return global default
            Optional<com.aspire.asat.notification.model.NotificationSettings> globalSetting =
                    globalSettingsService.getSettings(notificationType);

            if (globalSetting.isPresent()) {
                return mapGlobalToResponseDto(globalSetting.get(), clientAdminId);
            } else {
                // Global setting doesn't exist, return null or throw exception
                throw new AspireException("Notification type " + notificationType + " not found in global settings");
            }
        }
    }

    /**
     * Update or create a client notification setting
     * When creating a new setting, initializes from global defaults if values are not provided
     */
    public ClientNotificationSettings updateClientSetting(
            String clientAdminId,
            NotificationType notificationType,
            boolean enabled,
            Boolean emailEnabled,
            Boolean inAppEnabled,
            Boolean smsEnabled,
            Boolean pushEnabled,
            Boolean phoneCallEnabled) {

        boolean exists = repository.existsByClientAdminIdAndNotificationType(clientAdminId, notificationType);

        ClientNotificationSettings setting = repository
                .findByClientAdminIdAndNotificationType(clientAdminId, notificationType)
                .map(existing -> {
                    // Update existing client setting
                    existing.setEnabled(enabled);
                    applyIfNonNull(emailEnabled, existing::setEmailEnabled);
                    applyIfNonNull(inAppEnabled, existing::setInAppEnabled);
                    applyIfNonNull(smsEnabled, existing::setSmsEnabled);
                    applyIfNonNull(pushEnabled, existing::setPushEnabled);
                    applyIfNonNull(phoneCallEnabled, existing::setPhoneCallEnabled);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    // Create a new client setting-initialize from global defaults if values not provided
                    Optional<NotificationSettings> globalSetting =
                            globalSettingsService.getSettings(notificationType);

                    boolean defaultEmailEnabled = emailEnabled != null ? emailEnabled :
                            globalSetting.map(NotificationSettings::isEmailEnabled).orElse(true);
                    boolean defaultInAppEnabled = inAppEnabled != null ? inAppEnabled :
                            globalSetting.map(NotificationSettings::isInAppEnabled).orElse(true);
                    boolean defaultSmsEnabled = smsEnabled != null ? smsEnabled :
                            globalSetting.map(NotificationSettings::isSmsEnabled).orElse(false);
                    boolean defaultPushEnabled = pushEnabled != null ? pushEnabled :
                            globalSetting.map(NotificationSettings::isPushEnabled).orElse(false);
                    boolean defaultPhoneCallEnabled = phoneCallEnabled != null ? phoneCallEnabled :
                            globalSetting.map(NotificationSettings::isPhoneCallEnabled).orElse(false);

                    return ClientNotificationSettings.builder()
                            .clientAdminId(clientAdminId)
                            .notificationType(notificationType)
                            .enabled(enabled)
                            .emailEnabled(defaultEmailEnabled)
                            .inAppEnabled(defaultInAppEnabled)
                            .smsEnabled(defaultSmsEnabled)
                            .pushEnabled(defaultPushEnabled)
                            .phoneCallEnabled(defaultPhoneCallEnabled)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        ClientNotificationSettings saved = repository.save(setting);
        log.info("{} client notification setting for client: {}, type: {}, enabled: {}",
                exists ? "Updated" : "Created",
                clientAdminId, notificationType, saved.isEnabled());

        return saved;
    }

    private static <T> void applyIfNonNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Enable a notification type for a client
     */
    public ClientNotificationSettings enableNotificationType(String clientAdminId, NotificationType notificationType) {
        return updateClientSetting(clientAdminId, notificationType, true, null, null, null, null, null);
    }

    /**
     * Disable a notification type for a client
     */
    public ClientNotificationSettings disableNotificationType(String clientAdminId, NotificationType notificationType) {
        return updateClientSetting(clientAdminId, notificationType, false, null, null, null, null, null);
    }

    /**
     * Delete specific notification setting for a client
     */
    public void deleteClientSetting(String clientAdminId, NotificationType notificationType) {
        repository.deleteByClientAdminIdAndNotificationType(clientAdminId, notificationType);
        log.info("Deleted notification setting for client: {}, type: {}", clientAdminId, notificationType);
    }

    /**
     * Map ClientNotificationSettings to ResponseDto
     */
    private ClientNotificationSettingsResponseDto mapToResponseDto(ClientNotificationSettings setting, boolean isCustomized) {
        return ClientNotificationSettingsResponseDto.builder()
                .id(setting.getId())
                .clientAdminId(setting.getClientAdminId())
                .notificationType(setting.getNotificationType())
                .enabled(setting.isEnabled())
                .emailEnabled(setting.isEmailEnabled())
                .inAppEnabled(setting.isInAppEnabled())
                .smsEnabled(setting.isSmsEnabled())
                .pushEnabled(setting.isPushEnabled())
                .phoneCallEnabled(setting.isPhoneCallEnabled())
                .isCustomized(isCustomized)
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    /**
     * Map global NotificationSettings to ClientNotificationSettingsResponseDto
     */
    private ClientNotificationSettingsResponseDto mapGlobalToResponseDto(NotificationSettings globalSetting,
            String clientAdminId) {
        return ClientNotificationSettingsResponseDto.builder()
                .id(null) // No ID for global settings
                .clientAdminId(clientAdminId)
                .notificationType(globalSetting.getNotificationType())
                .enabled(globalSetting.isEnabled())
                .emailEnabled(globalSetting.isEmailEnabled())
                .inAppEnabled(globalSetting.isInAppEnabled())
                .smsEnabled(globalSetting.isSmsEnabled())
                .pushEnabled(globalSetting.isPushEnabled())
                .isCustomized(false)
                .createdAt(globalSetting.getCreatedAt())
                .updatedAt(globalSetting.getUpdatedAt())
                .build();
    }
}

