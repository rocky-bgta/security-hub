package com.aspire.asat.common.dto.notification;

import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for in-app notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotificationDto {
    
    @NotEmpty(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Title is required")
    private String title;
    
    @NotEmpty(message = "Message is required")
    private String message;
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @Builder.Default
    private boolean isRead = false;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
