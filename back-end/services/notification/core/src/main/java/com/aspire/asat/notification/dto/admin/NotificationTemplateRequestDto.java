package com.aspire.asat.notification.dto.admin;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating notification templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRequestDto {
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    /**
     * Optional recipient role this template is customized for. Null (the default)
     * means role-agnostic; it is used as the fallback for roles without their own template.
     */
    private NotificationRecipientRole recipientRole;

    @NotEmpty(message = "Template name is required")
    private String templateName;
    
    // Template content
    private String subjectTemplate; // For email
    
    private String htmlTemplate; // For email
    
    private String textTemplate; // For SMS/text
    
    private String titleTemplate; // For in-app/push
    
    private String messageTemplate; // For in-app/push
    
    // Template metadata
    private boolean isActive;
    private boolean isDefault;
    private String organizationId; // For multi-tenant customization
}
