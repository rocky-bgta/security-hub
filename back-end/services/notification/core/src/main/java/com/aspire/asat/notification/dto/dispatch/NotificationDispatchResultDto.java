package com.aspire.asat.notification.dto.dispatch;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Summary of a role-based notification fan-out: how many individual recipient
 * notifications were actually sent, and which roles were skipped (either because no
 * recipient exists for that role or the role/org/user gate blocked it).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDispatchResultDto {
    private NotificationType notificationType;
    private String targetUserId;
    private int sentCount;
    private List<NotificationRecipientRole> skippedRoles;
}
