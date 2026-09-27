package com.aspire.asat.notification.dto.admin;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO describing, for one notification type and channel, the base template and what
 * each recipient role currently resolves to. Drives the role matrix in the admin panel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRoleMatrixDto {

    private NotificationType notificationType;
    private NotificationChannel channel;

    /**
     * The role-agnostic template every role without its own variant falls back to.
     */
    private NotificationTemplateResponseDto baseTemplate;

    /**
     * One entry per recipient role, in enum order.
     */
    private List<RoleTemplateEntryDto> roles;

    /**
     * Whether a role has its own template or inherits the base one.
     */
    public enum RoleTemplateStatus {
        CUSTOM,
        INHERITS_BASE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleTemplateEntryDto {

        private NotificationRecipientRole role;
        private RoleTemplateStatus status;

        /**
         * Id and name of the role template, null when the role inherits the base template.
         */
        private String templateId;
        private String templateName;
        private boolean isActive;
    }
}
