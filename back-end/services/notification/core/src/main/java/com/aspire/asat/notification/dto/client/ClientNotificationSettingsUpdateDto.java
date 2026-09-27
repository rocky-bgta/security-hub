package com.aspire.asat.notification.dto.client;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating client notification channel settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNotificationSettingsUpdateDto {
    
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
    
    // Alternative: specify enabled/disabled channels
    private List<NotificationChannel> enabledChannels;
    private List<NotificationChannel> disabledChannels;
}

