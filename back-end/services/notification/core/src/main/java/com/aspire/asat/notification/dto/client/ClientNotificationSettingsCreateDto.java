package com.aspire.asat.notification.dto.client;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating client notification settings
 * Allows client admins to create custom notification preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNotificationSettingsCreateDto {
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @Builder.Default
    private Boolean enabled = true;
    
    // Channel-specific settings
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
    
    // Alternative: specify enabled/disabled channels
    private List<NotificationChannel> enabledChannels;
    private List<NotificationChannel> disabledChannels;
}

