package com.aspire.asat.common.dto.notification;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationPriority;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Entry point for role-based notification fan-out. Callers supply only the target end-user
 * and the event; the notification service resolves the full recipient hierarchy (user,
 * client admin, MSP, Aspire Admin) and applies the global -> role -> org -> user gate chain
 * per recipient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventRequestDto {

    /**
     * The end user the event is about. Used to resolve the recipient hierarchy
     * (user -> client admin -> MSP -> Aspire Admin) via the registration service.
     */
    @NotEmpty(message = "Target user id cannot be empty")
    private String targetUserId;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotEmpty(message = "At least one channel must be specified")
    private List<NotificationChannel> channels;

    /**
     * Base template model shared by all recipients. Merged with per-role overrides below.
     */
    private Map<String, Object> templateModel;

    /**
     * Optional per-role overrides/additions to the base template model
     * (e.g. a different greeting for MSP vs. Client Admin).
     */
    private Map<NotificationRecipientRole, Map<String, Object>> templateModelByRole;

    /**
     * Optional explicit subset of roles to notify. When null, all four roles are
     * considered and gated individually by the role settings matrix.
     */
    private List<NotificationRecipientRole> recipientRoles;

    private List<AttachmentDto> attachments;
    private Map<String, Object> metadata;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
}
