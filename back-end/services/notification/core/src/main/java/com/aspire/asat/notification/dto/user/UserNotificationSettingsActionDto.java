package com.aspire.asat.notification.dto.user;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.ClientNotificationActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for actions on a user's individual notification preferences.
 * Supports ENABLE, DISABLE, UPDATE_CHANNELS and DELETE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationSettingsActionDto {

    @NotNull(message = "Action is required")
    private ClientNotificationActionType action;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
}
