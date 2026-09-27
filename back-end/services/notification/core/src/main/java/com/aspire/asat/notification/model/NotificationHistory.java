package com.aspire.asat.notification.model;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationPriority;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.ChannelStatus;
import com.aspire.asat.notification.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for tracking notification delivery history
 * Stores a complete audit trail of all notifications sent through the system
 */
@Document(collection = "notification_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {
    
    @Id
    private String id;
    
    @Indexed
    @Field("notification_type")
    private NotificationType notificationType;
    
    @Indexed
    @Field("user_id")
    private String userId;
    
    @Indexed
    @Field("recipient_email")
    private String recipientEmail;
    
    @Field("recipient_phone")
    private String recipientPhone;
    
    @Field("channels")
    private List<NotificationChannel> channels;
    
    @Field("channel_statuses")
    @Builder.Default
    private Map<NotificationChannel, ChannelStatus> channelStatuses = new HashMap<>();
    
    @Field("subject")
    private String subject;
    
    @Field("template_id")
    private String templateId;
    
    @Field("template_model")
    private String templateModel; // JSON string of template variables
    
    @Indexed
    @Field("client_admin_id")
    private String clientAdminId;
    
    @Field("priority")
    private NotificationPriority priority;
    
    @Field("request_metadata")
    private String requestMetadata; // JSON string of additional request metadata
    
    @Indexed
    @Field("delivery_status")
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;
    
    @Field("error_message")
    private String errorMessage;
    
    @Indexed
    @Field("sent_at")
    private LocalDateTime sentAt;
    
    @Field("completed_at")
    private LocalDateTime completedAt;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Update channel status
     */
    public void updateChannelStatus(NotificationChannel channel, ChannelStatus status) {
        if (channel == null) {
            // If a channel is null, just update the overall delivery status
            updateDeliveryStatus();
            return;
        }
        if (channelStatuses == null) {
            channelStatuses = new HashMap<>();
        }
        channelStatuses.put(channel, status);
        updateDeliveryStatus();
    }
    
    /**
     * Update the overall delivery status based on channel statuses
     */
    public void updateDeliveryStatus() {
        if (channelStatuses == null || channelStatuses.isEmpty()) {
            deliveryStatus = DeliveryStatus.PENDING;
            return;
        }
        
        long successCount = channelStatuses.values().stream()
                .filter(status -> status == ChannelStatus.SUCCESS)
                .count();
        
        long failedCount = channelStatuses.values().stream()
                .filter(status -> status == ChannelStatus.FAILED)
                .count();
        
        long pendingCount = channelStatuses.values().stream()
                .filter(status -> status == ChannelStatus.PENDING)
                .count();
        
        if (pendingCount > 0) {
            deliveryStatus = DeliveryStatus.PENDING;
        } else if (failedCount == channelStatuses.size()) {
            deliveryStatus = DeliveryStatus.FAILED;
        } else if (successCount > 0 && failedCount > 0) {
            deliveryStatus = DeliveryStatus.PARTIAL;
        } else if (successCount == channelStatuses.size()) {
            deliveryStatus = DeliveryStatus.SUCCESS;
        } else {
            deliveryStatus = DeliveryStatus.PENDING;
        }
    }
    
    /**
     * Mark the notification as completed
     */
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
        updateDeliveryStatus();
    }
}

