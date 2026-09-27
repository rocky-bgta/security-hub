package com.aspire.asat.notification.dto.user;

import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a user's effective notification setting for a single type, showing
 * both the effective value and which layer of the gate chain (USER, ORGANIZATION, ROLE,
 * GLOBAL) it currently comes from.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationSettingsResponseDto {

    private String id;
    private String userId;
    private NotificationType notificationType;
    private boolean enabled;

    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean phoneCallEnabled;

    private boolean isCustomized;

    /**
     * The layer the displayed values were sourced from: USER, ORGANIZATION, ROLE or GLOBAL.
     */
    private String source;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
