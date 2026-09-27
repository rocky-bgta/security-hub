package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.role.NotificationRoleSettingsResponseDto;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import com.aspire.asat.notification.repository.NotificationRoleSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for managing the role-based notification settings matrix
 * (Aspire Admin, MSP, Client Admin, User) that sits between the global
 * notification settings and the per-organization/per-user settings.
 *
 * An absent row for a (type, role) pair is treated as "allowed" (inherit from above),
 * matching the org- and user-level layers. In practice rows are seeded for every
 * (type, role) pair on startup, so absence is a safety fallback rather than the norm.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRoleSettingsService {

    private final NotificationRoleSettingsRepository repository;

    public boolean isEnabledForRole(NotificationType notificationType, NotificationRecipientRole role) {
        if (role == null) {
            return true;
        }
        return repository.findByNotificationTypeAndRole(notificationType, role)
                .map(NotificationRoleSettings::isEnabled)
                .orElse(true);
    }

    public boolean isChannelEnabledForRole(NotificationType notificationType, NotificationRecipientRole role, NotificationChannel channel) {
        if (role == null) {
            return true;
        }
        Optional<NotificationRoleSettings> settings = repository.findByNotificationTypeAndRole(notificationType, role);
        if (settings.isEmpty()) {
            return true;
        }
        return settings.get().isChannelEnabled(channel);
    }

    public Optional<NotificationRoleSettings> getSetting(NotificationType notificationType, NotificationRecipientRole role) {
        return repository.findByNotificationTypeAndRole(notificationType, role);
    }

    public List<NotificationRoleSettings> getSettingsForType(NotificationType notificationType) {
        return repository.findByNotificationType(notificationType);
    }

    public List<NotificationRoleSettings> getMatrix() {
        return repository.findAllByOrderByNotificationTypeAscRoleAsc();
    }

    /**
     * Paginated matrix lookup with optional {@code notificationType} and {@code role} filters.
     * Results are ordered by notification type, then role.
     * Only {@linkplain NotificationType#adminVisibleTypes() admin-visible} types are returned.
     */
    public Page<NotificationRoleSettings> getMatrix(NotificationType notificationType,
                                                    NotificationRecipientRole role,
                                                    int offset,
                                                    int pageSize) {
        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;
        PageRequest pageRequest = PageRequest.of(safeOffset, safePageSize);

        if (notificationType != null) {
            NotificationType.requireAdminVisible(notificationType);
            if (role != null) {
                return repository.findByNotificationTypeAndRole(notificationType, role, pageRequest);
            }
            return repository.findByNotificationTypeOrderByRoleAsc(notificationType, pageRequest);
        }
        if (role != null) {
            return repository.findByNotificationTypeInAndRoleOrderByNotificationTypeAsc(
                    NotificationType.adminVisibleTypes(), role, pageRequest);
        }
        return repository.findByNotificationTypeInOrderByNotificationTypeAscRoleAsc(
                NotificationType.adminVisibleTypes(), pageRequest);
    }

    /**
     * Create the row for a (type, role) pair if missing, using the supplied channel defaults.
     * Existing rows are left untouched (idempotent seeding).
     */
    public NotificationRoleSettings seedIfMissing(NotificationType notificationType, NotificationRecipientRole role,
                                                   boolean enabled, boolean emailEnabled, boolean inAppEnabled,
                                                   boolean smsEnabled, boolean pushEnabled, boolean phoneCallEnabled) {
        return repository.findByNotificationTypeAndRole(notificationType, role)
                .orElseGet(() -> {
                    NotificationRoleSettings settings = NotificationRoleSettings.builder()
                            .notificationType(notificationType)
                            .role(role)
                            .enabled(enabled)
                            .emailEnabled(emailEnabled)
                            .inAppEnabled(inAppEnabled)
                            .smsEnabled(smsEnabled)
                            .pushEnabled(pushEnabled)
                            .phoneCallEnabled(phoneCallEnabled)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return repository.save(settings);
                });
    }

    public NotificationRoleSettings updateRoleSetting(NotificationType notificationType, NotificationRecipientRole role,
                                                        boolean enabled, Boolean emailEnabled, Boolean inAppEnabled,
                                                        Boolean smsEnabled, Boolean pushEnabled, Boolean phoneCallEnabled) {
        NotificationRoleSettings settings = repository.findByNotificationTypeAndRole(notificationType, role)
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
                .orElseGet(() -> NotificationRoleSettings.builder()
                        .notificationType(notificationType)
                        .role(role)
                        .enabled(enabled)
                        .emailEnabled(emailEnabled != null ? emailEnabled : true)
                        .inAppEnabled(inAppEnabled != null ? inAppEnabled : true)
                        .smsEnabled(smsEnabled != null ? smsEnabled : false)
                        .pushEnabled(pushEnabled != null ? pushEnabled : false)
                        .phoneCallEnabled(phoneCallEnabled != null ? phoneCallEnabled : false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        NotificationRoleSettings saved = repository.save(settings);
        log.info("Updated role notification setting for type: {}, role: {}, enabled: {}",
                notificationType, role, saved.isEnabled());
        return saved;
    }

    public NotificationRoleSettings enableForRole(NotificationType notificationType, NotificationRecipientRole role) {
        return updateRoleSetting(notificationType, role, true, null, null, null, null, null);
    }

    public NotificationRoleSettings disableForRole(NotificationType notificationType, NotificationRecipientRole role) {
        return updateRoleSetting(notificationType, role, false, null, null, null, null, null);
    }

    public void deleteRoleSetting(NotificationType notificationType, NotificationRecipientRole role) {
        repository.deleteByNotificationTypeAndRole(notificationType, role);
        log.info("Deleted role notification setting for type: {}, role: {}", notificationType, role);
    }

    public NotificationRoleSettingsResponseDto toResponseDto(NotificationRoleSettings settings, boolean isCustomized) {
        return NotificationRoleSettingsResponseDto.builder()
                .id(settings.getId())
                .notificationType(settings.getNotificationType())
                .role(settings.getRole())
                .enabled(settings.isEnabled())
                .emailEnabled(settings.isEmailEnabled())
                .inAppEnabled(settings.isInAppEnabled())
                .smsEnabled(settings.isSmsEnabled())
                .pushEnabled(settings.isPushEnabled())
                .phoneCallEnabled(settings.isPhoneCallEnabled())
                .isCustomized(isCustomized)
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
