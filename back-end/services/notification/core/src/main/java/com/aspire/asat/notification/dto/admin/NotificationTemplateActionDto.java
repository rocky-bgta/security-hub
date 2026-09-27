package com.aspire.asat.notification.dto.admin;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for dynamic action requests on notification templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateActionDto {
    
    @NotNull(message = "Action is required")
    private Action action;
    
    private String templateId; // Optional, for existing template actions
    private String notificationType; // Required for CREATE_ROLE_VARIANT
    private String channel; // Required for CREATE_ROLE_VARIANT
    private String templateName; // Optional, defaults to the base template name suffixed with the role

    /**
     * Recipient role the template is customized for. Required for CREATE_ROLE_VARIANT; on UPDATE
     * it may only repeat the role the template already has, since the role is part of a template's
     * identity and clearing it would orphan the role-agnostic fallback.
     */
    private NotificationRecipientRole recipientRole;

    // Template content for create/update actions
    private String subjectTemplate;
    private String htmlTemplate;
    private String textTemplate;
    private String titleTemplate;
    private String messageTemplate;
    private Boolean isActive;
    private Boolean isDefault;
    private String organizationId;

    /**
     * Optional sample values used by PREVIEW to fill the {{placeholders}}. When absent the
     * placeholders are rendered as-is.
     */
    private Map<String, Object> previewModel;
    
    /**
     * Available actions for notification templates.
     *
     * <p>Admins may update or preview existing templates, add a role-specific variant of an
     * existing base template, and delete such a variant. Creating arbitrary templates for
     * notification types the platform does not send, and deleting base templates, stay
     * unsupported so every notification type always resolves to a template.
     */
    public enum Action {
        UPDATE,
        PREVIEW,
        CREATE_ROLE_VARIANT,
        DELETE
    }
}
