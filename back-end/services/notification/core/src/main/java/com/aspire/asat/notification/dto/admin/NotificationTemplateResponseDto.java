package com.aspire.asat.notification.dto.admin;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponseDto {
    
    private String id;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private NotificationRecipientRole recipientRole;
    private String templateName;
    
    // Template content
    private String subjectTemplate;
    private String htmlTemplate;
    private String textTemplate;
    private String titleTemplate;
    private String messageTemplate;
    
    // Template metadata
    private boolean isActive;
    private boolean isDefault;
    private String organizationId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
