package com.aspire.asat.universal.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Universal service notification client for handling support ticket notifications.
 * Sends both email and in-app notifications when support tickets are created, resolved, or rejected.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UniversalNotificationClient {

    private final NotificationClient notificationClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            .withZone(ZoneId.systemDefault());

    /**
     * Send email and in-app notification when a support ticket is created.
     *
     * @param recipientEmail  Email address of the recipient (assigned admin)
     * @param recipientUserId User ID of the recipient (for in-app notification)
     * @param recipientName   Name of the recipient
     * @param ticketId        The ticket ID (e.g., TKT-20250210-001)
     * @param clientAdminId   Client admin ID for notification settings
     */
    public void sendTicketCreatedNotification(String recipientEmail, String recipientUserId, String recipientName,
                                              String ticketId, String clientAdminId) {
        Map<String, Object> templateModel = buildTemplateModel(
                recipientName,
                ticketId,
                formatStatus(TicketStatus.OPEN),
                null
        );

        sendNotification(recipientEmail, recipientUserId, clientAdminId, ticketId, "created", templateModel);
    }

    /**
     * Send email and in-app notification when a support ticket status is updated.
     *
     * @param recipientEmail  Email address of the recipient
     * @param recipientUserId User ID of the recipient (for in-app notification)
     * @param recipientName   Name of the recipient
     * @param ticketId        The ticket ID (e.g., TKT-20250210-001)
     * @param status          The new ticket status
     * @param clientAdminId   Client admin ID for notification settings
     */
    public void sendTicketStatusUpdateNotification(String recipientEmail, String recipientUserId, String recipientName,
                                                   String ticketId, TicketStatus status, String clientAdminId) {
        String resolutionDate = isTerminalStatus(status) ? DATE_FORMATTER.format(Instant.now()) : null;

        Map<String, Object> templateModel = buildTemplateModel(
                recipientName,
                ticketId,
                formatStatus(status),
                resolutionDate
        );

        sendNotification(recipientEmail, recipientUserId, clientAdminId, ticketId, "status update", templateModel);
    }

    /**
     * Send notification using the appropriate channel(s) based on available recipient information.
     * Sends multi-channel (email + in-app) if both are available, otherwise falls back to single channel.
     *
     * @param recipientEmail  Email address of the recipient
     * @param recipientUserId User ID of the recipient (for in-app notification)
     * @param clientAdminId   Client admin ID for notification settings
     * @param ticketId        The ticket ID for logging
     * @param eventType       Description of the event (for logging)
     * @param templateModel   Template variables for the notification
     */
    private void sendNotification(String recipientEmail, String recipientUserId, String clientAdminId,
                                  String ticketId, String eventType, Map<String, Object> templateModel) {
        try {
            boolean hasEmail = recipientEmail != null && !recipientEmail.isBlank();
            boolean hasUserId = recipientUserId != null && !recipientUserId.isBlank();

            if (!hasEmail && !hasUserId) {
                log.warn("Cannot send ticket {} notification: both email and userId are null or empty for ticketId: {}",
                        eventType, ticketId);
                return;
            }

            log.info("Sending ticket {} notification - ticketId: {}, email: {}, userId: {}",
                    eventType, ticketId, recipientEmail, recipientUserId);

            boolean sent;
            if (hasEmail && hasUserId) {
                sent = notificationClient.sendMultiChannelNotification(
                        recipientEmail, recipientUserId, clientAdminId,
                        NotificationType.HELP_DESK_TICKET_UPDATES, templateModel);
            } else if (hasEmail) {
                sent = notificationClient.sendEmailNotification(
                        recipientEmail, NotificationType.HELP_DESK_TICKET_UPDATES, templateModel);
            } else {
                sent = notificationClient.sendInAppNotification(
                        recipientUserId, NotificationType.HELP_DESK_TICKET_UPDATES, templateModel);
            }

            if (sent) {
                log.info("Ticket {} notification sent successfully for ticketId: {}", eventType, ticketId);
            } else {
                log.warn("Failed to send ticket {} notification for ticketId: {}", eventType, ticketId);
            }

        } catch (Exception e) {
            log.error("Error sending ticket {} notification for ticketId: {} - Error: {}",
                    eventType, ticketId, e.getMessage(), e);
        }
    }

    /**
     * Build template model for help desk ticket notification.
     */
    private Map<String, Object> buildTemplateModel(String adminName, String ticketId,
                                                    String status, String resolutionDate) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("adminName", adminName != null ? adminName : "User");
        templateModel.put("ticketId", ticketId);
        templateModel.put("status", status);
        templateModel.put("resolutionDate", resolutionDate != null ? resolutionDate : "N/A");
        return templateModel;
    }

    /**
     * Format status for display (e.g., IN_PROGRESS -> In Progress).
     */
    private String formatStatus(TicketStatus status) {
        if (status == null) {
            return "Unknown";
        }
        String name = status.name().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * Check if the status is a terminal status (closed).
     */
    private boolean isTerminalStatus(TicketStatus status) {
        return status == TicketStatus.CLOSED;
    }
}
