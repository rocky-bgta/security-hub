package com.aspire.asat.notification.model;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document for managing notification templates
 * Each notification type can have different templates for different channels
 */
@Document(collection = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    
    @Id
    private String id;
    
    @Field("notification_type")
    private NotificationType notificationType;
    
    @Field("channel")
    private NotificationChannel channel;

    /**
     * Optional recipient role this template is customized for (Aspire Admin, MSP, Client
     * Admin, User). Null means role-agnostic -- used as the fallback for any role that
     * doesn't have its own template.
     */
    @Field("recipient_role")
    private NotificationRecipientRole recipientRole;

    @Field("template_name")
    private String templateName;
    
    // Template content
    @Field("subject_template")
    private String subjectTemplate; // For email
    
    @Field("html_template")
    private String htmlTemplate; // For email
    
    @Field("text_template")
    private String textTemplate; // For SMS/text
    
    @Field("title_template")
    private String titleTemplate; // For in-app/push
    
    @Field("message_template")
    private String messageTemplate; // For in-app/push
    
    // Template metadata
    @Field("is_active")
    @Builder.Default
    private boolean isActive = true;
    
    @Field("is_default")
    @Builder.Default
    private boolean isDefault = false;
    
    @Field("organization_id")
    private String organizationId; // For multi-tenant customization
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Get the appropriate template content based on channel
     */
    public String getTemplateContent() {
        return switch (channel) {
            case EMAIL -> htmlTemplate;
            case IN_APP, PUSH -> messageTemplate;
            case SMS, PHONE_CALL -> textTemplate;
        };
    }
    
    /**
     * Get the appropriate title/subject based on channel
     */
    public String getTemplateTitle() {
        return switch (channel) {
            case EMAIL -> subjectTemplate;
            case IN_APP, SMS -> titleTemplate;
            case PUSH -> titleTemplate;
            case PHONE_CALL -> titleTemplate;
        };
    }
}
