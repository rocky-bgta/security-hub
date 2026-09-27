package com.aspire.asat.notification.service;

import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing notification settings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingsService {
    
    private final NotificationSettingsRepository repository;
    
    /**
     * Check if a notification type is enabled
     */
    public boolean isNotificationTypeEnabled(NotificationType notificationType) {
        Optional<NotificationSettings> settings = repository.findByNotificationType(notificationType);
        return settings.map(NotificationSettings::isEnabled).orElse(false);
    }
    
    /**
     * Check if a specific channel is enabled for a notification type
     */
    public boolean isChannelEnabled(NotificationType notificationType, NotificationChannel channel) {
        Optional<NotificationSettings> settings = repository.findByNotificationType(notificationType);
        if (settings.isEmpty()) {
            return false;
        }
        
        NotificationSettings setting = settings.get();
        return setting.isChannelEnabled(channel);
    }
    
    /**
     * Get notification settings by type
     */
    public Optional<NotificationSettings> getSettings(NotificationType notificationType) {
        return repository.findByNotificationType(notificationType);
    }

    
    /**
     * Get all enabled notification settings
     */
    public List<NotificationSettings> getAllEnabledSettings() {
        return repository.findByEnabledTrue();
    }
    
    /**
     * Get all notification settings
     */
    public List<NotificationSettings> getAllSettings() {
        return repository.findAllByOrderByCreatedAtAsc();
    }
    
    /**
     * Get settings for priority notification types
     */
    public List<NotificationSettings> getPrioritySettings() {
        return repository.findByPriorityTypes(NotificationType.PRIORITY_TYPES);
    }

    
    /**
     * Enable/disable a specific channel for a notification type
     */
    public void setChannelEnabled(NotificationType notificationType, NotificationChannel channel, boolean enabled) {
        Optional<NotificationSettings> settingsOpt = repository.findByNotificationType(notificationType);
        if (settingsOpt.isPresent()) {
            NotificationSettings settings = settingsOpt.get();
            settings.setChannelEnabled(channel, enabled);
            repository.save(settings);
            log.info("Channel {} {} for notification type {}", 
                channel, enabled ? "enabled" : "disabled", notificationType);
        } else {
            log.warn("Notification settings not found for type: {}", notificationType);
        }
    }
    
    /**
     * Save or update notification settings without enforcing single active constraint
     */
    public NotificationSettings saveSettings(NotificationSettings settings) {
        // If this setting is being enabled, disable all others
//        if (settings.isEnabled()) {
//            disableAllOtherSettings(settings.getNotificationType());
//        }
        return repository.save(settings);
    }
    
    /**
     * Disable all other notification settings except the specified one
     */
    private void disableAllOtherSettings(NotificationType currentType) {
        List<NotificationSettings> allSettings = repository.findAll();
        for (NotificationSettings setting : allSettings) {
            if (!setting.getNotificationType().equals(currentType) && setting.isEnabled()) {
                setting.setEnabled(false);
                repository.save(setting);
                log.info("Disabled notification settings for type: {}", setting.getNotificationType());
            }
        }
    }
    
    /**
     * Enable a specific notification type (disables all others)
     */
    public NotificationSettings enableNotificationType(NotificationType notificationType) {
        // First disable all other settings
        disableAllOtherSettings(notificationType);
        
        // Then enable the requested one
        Optional<NotificationSettings> settingsOpt = repository.findByNotificationType(notificationType);
        if (settingsOpt.isPresent()) {
            NotificationSettings settings = settingsOpt.get();
            settings.setEnabled(true);
            return repository.save(settings);
        } else {
            // Create new settings if they don't exist
            NotificationSettings newSettings = NotificationSettings.builder()
                .id(java.util.UUID.randomUUID().toString())
                .notificationType(notificationType)
                .enabled(true)
                .description("Auto-generated settings for " + notificationType.getDisplayName())
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .pushEnabled(false)
                .phoneCallEnabled(false)
                .build();
            return repository.save(newSettings);
        }
    }
    
    /**
     * Disable a specific notification type
     */
    public NotificationSettings disableNotificationType(NotificationType notificationType) {
        Optional<NotificationSettings> settingsOpt = repository.findByNotificationType(notificationType);
        if (settingsOpt.isPresent()) {
            NotificationSettings settings = settingsOpt.get();
            settings.setEnabled(false);
            return repository.save(settings);
        }
        return null;
    }
    
    /**
     * Initialize default settings for a notification type if not exists
     */
    public void initializeDefaultSettings(NotificationType notificationType) {
        Optional<NotificationSettings> existing = repository.findByNotificationType(notificationType);
        if (existing.isEmpty()) {
            NotificationSettings settings = NotificationSettings.builder()
                .id(java.util.UUID.randomUUID().toString())
                .notificationType(notificationType)
                .enabled(notificationType.isEnabledByDefault())
                .description("Auto-generated settings for " + notificationType.getDisplayName())
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .pushEnabled(false)
                .phoneCallEnabled(false)
                .build();
            
            repository.save(settings);
            log.info("Initialized default settings for notification type: {}", notificationType);
        }
    }
}
