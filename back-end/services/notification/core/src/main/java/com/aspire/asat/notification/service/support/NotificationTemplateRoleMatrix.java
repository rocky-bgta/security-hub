package com.aspire.asat.notification.service.support;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.notification.model.NotificationTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * All templates configured for one notification type and channel, split into the role-agnostic
 * base template and the role-specific variants that override it.
 *
 * @param baseTemplate the role-agnostic template every role without a variant falls back to,
 *                     null only for legacy data where no base row exists
 * @param variants     role-specific templates keyed by role; a missing key means that role
 *                     inherits the base template
 */
public record NotificationTemplateRoleMatrix(NotificationTemplate baseTemplate,
                                            Map<NotificationRecipientRole, NotificationTemplate> variants) {

    public Optional<NotificationTemplate> variantFor(NotificationRecipientRole role) {
        return Optional.ofNullable(variants.get(role));
    }
}
