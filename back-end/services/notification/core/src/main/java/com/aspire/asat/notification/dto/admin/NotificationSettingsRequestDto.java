package com.aspire.asat.notification.dto.admin;

import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating notification settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequestDto {
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    private boolean enabled;
    
    private String description;
    
    // Channel-specific settings
    private boolean emailEnabled;
    private boolean inAppEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean phoneCallEnabled;
    
    // Default template IDs
    private String defaultEmailTemplateId;
    private String defaultInAppTemplateId;
    private String defaultSmsTemplateId;
    private String defaultPushTemplateId;
    private String defaultPhoneCallTemplateId;
}
