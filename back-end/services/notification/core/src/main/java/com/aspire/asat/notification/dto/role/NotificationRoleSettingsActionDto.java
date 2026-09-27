package com.aspire.asat.notification.dto.role;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.ClientNotificationActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin actions on the role-based notification settings matrix.
 * Supports ENABLE, DISABLE, UPDATE_CHANNELS and DELETE (INITIALIZE is not applicable here
 * since rows are seeded automatically on startup).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRoleSettingsActionDto {

    @NotNull(message = "Action is required")
    private ClientNotificationActionType action;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Role is required")
    private NotificationRecipientRole role;

    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
}
