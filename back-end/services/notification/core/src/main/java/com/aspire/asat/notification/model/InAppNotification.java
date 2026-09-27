package com.aspire.asat.notification.model;

import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document for in-app notifications
 */
@Document(collection = "in_app_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotification {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("title")
    private String title;
    
    @Field("message")
    private String message;
    
    @Field("notification_type")
    private NotificationType notificationType;
    
    @Field("is_read")
    @Builder.Default
    private boolean isRead = false;
    
    @Field("metadata")
    private String metadata; // Store as JSON string
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("read_at")
    private LocalDateTime readAt;
    
    /**
     * Mark the notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Mark the notification as unread
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
}
