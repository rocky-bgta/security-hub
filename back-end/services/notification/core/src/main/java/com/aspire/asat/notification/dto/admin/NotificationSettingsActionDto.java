package com.aspire.asat.notification.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dynamic action requests on notification settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsActionDto {
    
    @NotNull(message = "Action is required")
    private Action action;
    
    @NotEmpty(message = "Notification type is required")
    private String notificationType;
    
    private String channel; // Optional, for channel-specific actions
    private String description; // Optional, for update actions
    
    // Settings data for update/create actions
    private Boolean enabled;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean phoneCallEnabled;
    private String defaultEmailTemplateId;
    private String defaultInAppTemplateId;
    private String defaultSmsTemplateId;
    private String defaultPushTemplateId;
    private String defaultPhoneCallTemplateId;
    
    /**
     * Available actions for notification settings
     */
    public enum Action {
        CREATE,
        UPDATE,
        ENABLE,
        DISABLE,
        ENABLE_CHANNEL,
        DISABLE_CHANNEL,
        DELETE
    }
}
