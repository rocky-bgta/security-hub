package com.aspire.asat.common.dto.notification;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationPriority;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Enhanced DTO for notification requests
 * Supports multiple channels and notification types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    
    // Basic fields
    @NotEmpty(message = "Recipient address cannot be empty")
    private String to; // Email address, phone number, or user ID (flexible based on channel)
    
    /**
     * Phone number for SMS or PHONE_CALL channels
     * If provided, this takes precedence over 'to' field for SMS/PHONE_CALL channels
     */
    private String phoneNumber;
    
    private String userId; // For in-app notifications
    
    private String clientAdminId; // Optional: For client-specific notification settings

    /**
     * Optional: the role of the recipient this specific request targets (USER, CLIENT_ADMIN,
     * MSP, ASPIRE_ADMIN). When null, the role layer of the preference chain is skipped and
     * legacy single-recipient behavior is preserved.
     */
    private NotificationRecipientRole recipientRole;

    private String subject;
    
    // Notification type and channels
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    @NotEmpty(message = "At least one channel must be specified")
    private List<NotificationChannel> channels;
    
    // Template and content
    private String templateId; // Optional - for custom templates
    private Map<String, Object> templateModel;
    
    // Attachments and metadata
    private List<AttachmentDto> attachments;
    private Map<String, Object> metadata;
    
    // Scheduling
    private LocalDateTime scheduledAt;
    private String timezone;
    
    // Priority and delivery options
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    @Builder.Default
    private boolean allowRetry = true;
    @Builder.Default
    private int maxRetryAttempts = 3;
    

}
