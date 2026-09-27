package com.aspire.asat.common.client;

import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;

import java.util.List;
import java.util.Map;

/**
 * Client interface for sending notifications to the notification service
 * Provides a standardized way for all services to send notifications
 */
public interface NotificationClient {

    /**
     * Send a notification through multiple channels
     *
     * @param requestDto the notification request containing all necessary data
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendNotification(NotificationRequestDto requestDto);

    /**
     * Send a simple email notification
     *
     * @param to email address of the recipient
     * @param notificationType type of notification
     * @param templateModel data for template rendering
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendEmailNotification(String to, NotificationType notificationType, Map<String, Object> templateModel);

    /**
     * Send a simple in-app notification
     *
     * @param userId user ID for the notification
     * @param notificationType type of notification
     * @param templateModel data for template rendering
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendInAppNotification(String userId, NotificationType notificationType, Map<String, Object> templateModel);

    /**
     * Send a multi-channel notification (email + in-app)
     *
     * @param to email address of the recipient
     * @param userId user ID for in-app notification
     * @param notificationType type of notification
     * @param templateModel data for template rendering
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendMultiChannelNotification(String to, String userId, NotificationType notificationType, Map<String, Object> templateModel);

    /**
     * Send a high-priority notification
     *
     * @param to email address of the recipient
     * @param userId user ID for in-app notification
     * @param notificationType type of notification
     * @param templateModel data for template rendering
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendHighPriorityNotification(String to, String userId, NotificationType notificationType, Map<String, Object> templateModel);

    /**
     * Send a multi-channel notification with client admin ID (for client-specific settings)
     *
     * @param to email address of the recipient
     * @param userId user ID for in-app notification
     * @param clientAdminId client admin ID for client-specific notification settings
     * @param notificationType type of notification
     * @param templateModel data for template rendering
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendMultiChannelNotification(String to, String userId, String clientAdminId, NotificationType notificationType, Map<String, Object> templateModel);
    
    /**
     * Send a notification with custom channels and client admin ID
     *
     * @param to email address of the recipient
     * @param userId user ID for in-app notification
     * @param clientAdminId client admin ID for client-specific notification settings
     * @param notificationType type of notification
     * @param channels list of channels to use
     * @param templateModel data for template rendering
     * @param attachments list of attachments
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendCustomChannelNotification(String to, String userId, String clientAdminId, NotificationType notificationType,
                                          List<NotificationChannel> channels, Map<String, Object> templateModel, List<AttachmentDto> attachments);

    /**
     * Trigger role-based notification fan-out for a single event. The notification service
     * resolves the full recipient hierarchy (user, client admin, MSP, Aspire Admin) for the
     * given target user and applies the global -> role -> org -> user gate chain per recipient,
     * so callers only need to provide the end user the event is about.
     *
     * @param eventRequestDto the notification event containing the target user, type, channels and template data
     * @return true if the dispatch request was accepted successfully, false otherwise
     */
    boolean sendRoleBasedNotification(NotificationEventRequestDto eventRequestDto);
}
