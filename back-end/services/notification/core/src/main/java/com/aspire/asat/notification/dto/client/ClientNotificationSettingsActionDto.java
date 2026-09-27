package com.aspire.asat.notification.dto.client;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.ClientNotificationActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for client notification settings actions
 * Supports various actions like ENABLE, DISABLE, UPDATE_CHANNELS, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNotificationSettingsActionDto {
    
    @NotNull(message = "Action is required")
    private ClientNotificationActionType action;
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    // Optional fields for the UPDATE_CHANNELS action
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
    
    // Optional: List of channels to enable/disable
    private List<NotificationChannel> enabledChannels;
    private List<NotificationChannel> disabledChannels;
}

