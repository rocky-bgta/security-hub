package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Owns the 4-layer notification gate chain: global -> role -> organization -> user.
 * Each layer is evaluated in order and can only further restrict delivery (fail closed);
 * an absent row at the role, organization or user layer means "inherit from above"
 * (default allow). Only the global layer is fail-closed when no row exists at all, matching
 * the platform's existing behaviour.
 *
 * <pre>
 * Global (type + channel) -&gt; disabled -&gt; skip all recipients
 *   -&gt; enabled -&gt; Role matrix (type + role) -&gt; role disabled -&gt; skip that role's recipients
 *     -&gt; role enabled/absent -&gt; Org ClientNotificationSettings (clientAdminId + type) -&gt; disabled -&gt; skip recipient
 *       -&gt; enabled/absent -&gt; UserNotificationSettings (userId + type) -&gt; disabled -&gt; skip recipient
 *         -&gt; enabled/absent -&gt; deliver
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceResolver {

    private final NotificationSettingsService globalSettingsService;
    private final NotificationRoleSettingsService roleSettingsService;
    private final ClientNotificationSettingsService clientSettingsService;
    private final UserNotificationSettingsService userSettingsService;

    /**
     * Whether the notification type itself is allowed to be sent to a recipient with the
     * given role, client admin org and user id. {@code role}, {@code clientAdminId} and
     * {@code userId} may all be null, in which case that layer is skipped (legacy behaviour).
     */
    public boolean isTypeAllowed(NotificationType type, NotificationRecipientRole role, String clientAdminId, String userId) {
        if (!globalSettingsService.isNotificationTypeEnabled(type)) {
            log.debug("Notification type {} disabled at the global layer", type);
            return false;
        }

        if (!roleSettingsService.isEnabledForRole(type, role)) {
            log.debug("Notification type {} disabled for role {}", type, role);
            return false;
        }

        if (!clientSettingsService.isNotificationTypeEnabledForClient(clientAdminId, type)) {
            log.debug("Notification type {} disabled for client admin {}", type, clientAdminId);
            return false;
        }

        if (!userSettingsService.isEnabledForUser(userId, type)) {
            log.debug("Notification type {} disabled for user {}", type, userId);
            return false;
        }

        return true;
    }

    /**
     * Whether a specific channel is allowed for the notification type + recipient, walking
     * the same global -> role -> org -> user chain.
     */
    public boolean isChannelAllowed(NotificationType type, NotificationChannel channel,
                                     NotificationRecipientRole role, String clientAdminId, String userId) {
        if (!globalSettingsService.isNotificationTypeEnabled(type) || !globalSettingsService.isChannelEnabled(type, channel)) {
            return false;
        }

        if (!roleSettingsService.isEnabledForRole(type, role) || !roleSettingsService.isChannelEnabledForRole(type, role, channel)) {
            return false;
        }

        if (!clientSettingsService.isChannelEnabledForClient(clientAdminId, type, channel)) {
            return false;
        }

        return userSettingsService.isEnabledForUser(userId, type)
                && userSettingsService.isChannelEnabledForUser(userId, type, channel);
    }
}
