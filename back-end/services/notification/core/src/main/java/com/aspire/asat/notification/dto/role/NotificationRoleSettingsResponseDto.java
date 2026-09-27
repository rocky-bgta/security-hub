package com.aspire.asat.notification.dto.role;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a single (notificationType, role) row of the role-based notification
 * settings matrix.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRoleSettingsResponseDto {

    private String id;
    private NotificationType notificationType;
    private NotificationRecipientRole role;
    private boolean enabled;

    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean phoneCallEnabled;

    /**
     * True when a persisted row exists for this (type, role). False means the value
     * shown is the default fallback (enabled, global channel flags).
     */
    private boolean isCustomized;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
