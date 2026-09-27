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
 * MongoDB document for individual user notification preferences. This is the highest-priority
 * layer of the gate chain: an explicit user override beats the role and organization layers.
 */
@Document(collection = "user_notification_settings")
@CompoundIndex(name = "user_notification_type_idx", def = "{'user_id': 1, 'notification_type': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationSettings {

    @Id
    private String id;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("notification_type")
    private NotificationType notificationType;

    @Field("enabled")
    @Builder.Default
    private boolean enabled = true;

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
