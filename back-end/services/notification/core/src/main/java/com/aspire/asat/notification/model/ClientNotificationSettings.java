package com.aspire.asat.notification.model;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document for managing client-specific notification settings
 * Allows each client (organization) to enable/disable notification types independently
 */
@Document(collection = "client_notification_settings")
@CompoundIndex(name = "client_notification_type_idx", def = "{'client_admin_id': 1, 'notification_type': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNotificationSettings {
    
    @Id
    private String id;
    
    @Field("client_admin_id")
    @Indexed
    private String clientAdminId;
    
    @Field("notification_type")
    private NotificationType notificationType;
    
    @Field("enabled")
    @Builder.Default
    private boolean enabled = true;
    
    // Channel-specific settings
    @Field("email_enabled")
    @Builder.Default
    private boolean emailEnabled = true;
    
    @Field("in_app_enabled")
    @Builder.Default
    private boolean inAppEnabled = true;
    
    @Field("sms_enabled")
    @Builder.Default
    private boolean smsEnabled = false;
    
    @Field("push_enabled")
    @Builder.Default
    private boolean pushEnabled = false;
    
    @Field("phone_call_enabled")
    @Builder.Default
    private boolean phoneCallEnabled = false;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Check if a specific channel is enabled for this notification type
     */
    public boolean isChannelEnabled(NotificationChannel channel) {
        if (!enabled) {
            return false;
        }
        
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case IN_APP -> inAppEnabled;
            case SMS -> smsEnabled;
            case PUSH -> pushEnabled;
            case PHONE_CALL -> phoneCallEnabled;
        };
    }
    
    /**
     * Enable/disable a specific channel for this notification type
     */
    public void setChannelEnabled(NotificationChannel channel, boolean enabled) {
        switch (channel) {
            case EMAIL -> this.emailEnabled = enabled;
            case IN_APP -> this.inAppEnabled = enabled;
            case SMS -> this.smsEnabled = enabled;
            case PUSH -> this.pushEnabled = enabled;
            case PHONE_CALL -> this.phoneCallEnabled = enabled;
        }
    }
}

