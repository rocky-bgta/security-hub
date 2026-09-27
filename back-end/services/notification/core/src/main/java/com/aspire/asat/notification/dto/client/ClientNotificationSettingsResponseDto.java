package com.aspire.asat.notification.dto.client;

import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for client notification settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNotificationSettingsResponseDto {
    
    private String id;
    private String clientAdminId;
    private NotificationType notificationType;
    private boolean enabled;
    
    // Channel-specific settings
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean phoneCallEnabled;
    
    // Indicates if this is a client-customized setting or default admin setting
    private boolean isCustomized;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

