package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.user.UserNotificationSettingsResponseDto;
import com.aspire.asat.notification.model.ClientNotificationSettings;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.model.UserNotificationSettings;
import com.aspire.asat.notification.repository.ClientNotificationSettingsRepository;
import com.aspire.asat.notification.repository.UserNotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for managing individual user notification preferences — the highest-priority
 * layer of the gate chain (global -> role -> org -> user). An absent row is treated as
 * "allowed" (inherit from the layers below), matching the org-level layer's behaviour.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationSettingsService {

    private final UserNotificationSettingsRepository repository;
    private final NotificationSettingsService globalSettingsService;
    private final NotificationRoleSettingsService roleSettingsService;
    private final ClientNotificationSettingsRepository clientNotificationSettingsRepository;

    public boolean isEnabledForUser(String userId, NotificationType notificationType) {
        if (userId == null || userId.trim().isEmpty()) {
            return true;
        }
        return repository.findByUserIdAndNotificationType(userId, notificationType)
                .map(UserNotificationSettings::isEnabled)
                .orElse(true);
    }

    public boolean isChannelEnabledForUser(String userId, NotificationType notificationType, NotificationChannel channel) {
        if (userId == null || userId.trim().isEmpty()) {
            return true;
        }
        Optional<UserNotificationSettings> setting = repository.findByUserIdAndNotificationType(userId, notificationType);
        if (setting.isEmpty()) {
            return true;
        }
        return setting.get().isChannelEnabled(channel);
    }

    public Optional<UserNotificationSettings> getSetting(String userId, NotificationType notificationType) {
        return repository.findByUserIdAndNotificationType(userId, notificationType);
    }

    /**
     * Build the merged view of every notification type for a user, showing the effective
     * value plus which layer (USER, ORGANIZATION, ROLE, GLOBAL) it is currently sourced from.
     */
    public List<UserNotificationSettingsResponseDto> getAllForUser(String userId, String clientAdminId, NotificationRecipientRole role) {
        return List.of(NotificationType.values()).stream()
                .map(type -> getMergedSetting(userId, clientAdminId, role, type))
                .toList();
    }

    public UserNotificationSettingsResponseDto getMergedSetting(String userId, String clientAdminId,
                                                                  NotificationRecipientRole role, NotificationType notificationType) {
        Optional<UserNotificationSettings> userSetting = repository.findByUserIdAndNotificationType(userId, notificationType);
        if (userSetting.isPresent()) {
            return toResponseDto(userSetting.get(), true, "USER");
        }

        if (clientAdminId != null && !clientAdminId.trim().isEmpty()) {
            Optional<ClientNotificationSettings> orgSetting =
                    clientNotificationSettingsRepository.findByClientAdminIdAndNotificationType(clientAdminId, notificationType);
            if (orgSetting.isPresent()) {
                return mapOrgToResponseDto(userId, orgSetting.get());
            }
        }

        if (role != null) {
            Optional<NotificationRoleSettings> roleSetting = roleSettingsService.getSetting(notificationType, role);
            if (roleSetting.isPresent()) {
                return mapRoleToResponseDto(userId, roleSetting.get());
            }
        }

        Optional<NotificationSettings> globalSetting = globalSettingsService.getSettings(notificationType);
        return globalSetting.map(settings -> mapGlobalToResponseDto(userId, settings))
                .orElseGet(() -> UserNotificationSettingsResponseDto.builder()
                        .userId(userId)
                        .notificationType(notificationType)
                        .enabled(true)
                        .emailEnabled(true)
                        .inAppEnabled(true)
                        .isCustomized(false)
                        .source("GLOBAL")
                        .build());
    }

    public UserNotificationSettings updateUserSetting(String userId, NotificationType notificationType,
                                                        boolean enabled, Boolean emailEnabled, Boolean inAppEnabled,
                                                        Boolean smsEnabled, Boolean pushEnabled, Boolean phoneCallEnabled) {
        UserNotificationSettings setting = repository.findByUserIdAndNotificationType(userId, notificationType)
                .map(existing -> {
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
                    Optional<NotificationSettings> globalSetting = globalSettingsService.getSettings(notificationType);
                    return UserNotificationSettings.builder()
                            .userId(userId)
                            .notificationType(notificationType)
                            .enabled(enabled)
                            .emailEnabled(emailEnabled != null ? emailEnabled : globalSetting.map(NotificationSettings::isEmailEnabled).orElse(true))
                            .inAppEnabled(inAppEnabled != null ? inAppEnabled : globalSetting.map(NotificationSettings::isInAppEnabled).orElse(true))
                            .smsEnabled(smsEnabled != null ? smsEnabled : globalSetting.map(NotificationSettings::isSmsEnabled).orElse(false))
                            .pushEnabled(pushEnabled != null ? pushEnabled : globalSetting.map(NotificationSettings::isPushEnabled).orElse(false))
                            .phoneCallEnabled(phoneCallEnabled != null ? phoneCallEnabled : globalSetting.map(NotificationSettings::isPhoneCallEnabled).orElse(false))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        UserNotificationSettings saved = repository.save(setting);
        log.info("Updated user notification setting for user: {}, type: {}, enabled: {}", userId, notificationType, saved.isEnabled());
        return saved;
    }

    public UserNotificationSettings enableForUser(String userId, NotificationType notificationType) {
        return updateUserSetting(userId, notificationType, true, null, null, null, null, null);
    }

    public UserNotificationSettings disableForUser(String userId, NotificationType notificationType) {
        return updateUserSetting(userId, notificationType, false, null, null, null, null, null);
    }

    public void deleteUserSetting(String userId, NotificationType notificationType) {
        repository.deleteByUserIdAndNotificationType(userId, notificationType);
        log.info("Deleted user notification setting for user: {}, type: {}", userId, notificationType);
    }

    public UserNotificationSettingsResponseDto toResponseDto(UserNotificationSettings settings, boolean isCustomized, String source) {
        return UserNotificationSettingsResponseDto.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(isCustomized)
                .source(source)
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private UserNotificationSettingsResponseDto mapOrgToResponseDto(String userId, ClientNotificationSettings settings) {
        return UserNotificationSettingsResponseDto.builder()
                .userId(userId)
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(false)
                .source("ORGANIZATION")
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private UserNotificationSettingsResponseDto mapRoleToResponseDto(String userId, NotificationRoleSettings settings) {
        return UserNotificationSettingsResponseDto.builder()
                .userId(userId)
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(false)
                .source("ROLE")
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private UserNotificationSettingsResponseDto mapGlobalToResponseDto(String userId, NotificationSettings settings) {
        return UserNotificationSettingsResponseDto.builder()
                .userId(userId)
                .notificationType(settings.getNotificationType())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(false)
                .source("GLOBAL")
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private static <T> void applyIfNonNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
