package com.aspire.asat.auth.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auth service-specific notification client
 * Handles notifications related to authentication and user management
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuthNotificationClient {
    private final NotificationClient notificationClient;


    /**
     * Send password reset notification to user
     *
     * @param userEmail  user's email address
     * @param userId     user's ID
     * @param userName   user's name
     * @param resetUrl   password reset URL
     * @param expiryTime when the token expires (e.g., "1 hour")
     */
    public void sendPasswordResetNotification(String userEmail, String userId, String clientAdminId, String userName,
                                               String resetUrl, String expiryTime) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(NotificationTemplateValue.USER_NAME, userName);
            templateModel.put(NotificationTemplateValue.RESET_URL, resetUrl);
            templateModel.put(NotificationTemplateValue.EXPIRY_TIME, expiryTime);
            templateModel.put(NotificationTemplateValue.MESSAGE, "Use the link below to reset your password.");

            log.info("Sending password reset notification to user: {}", userName);

            notificationClient.sendMultiChannelNotification(
                    userEmail, userId, clientAdminId, NotificationType.PASSWORD_RESET_REQUEST, templateModel);

            log.info("Password reset notification sent successfully to user: {}", userName);

        } catch (Exception e) {
            log.error("Failed to send password reset notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Request record for password change notifications to encapsulate parameters.
     */
    public record PasswordChangeNotificationData(
            String userEmail,
            String userId,
            String userName,
            String adminEmail,
            String adminId,
            String adminName,
            String changeTimestamp,
            String eventTimestamp,
            String userType
    ) {
        /**
         * Backward-compatible constructor without eventTimestamp.
         */
        public PasswordChangeNotificationData(
                String userEmail,
                String userId,
                String userName,
                String adminEmail,
                String adminId,
                String adminName,
                String changeTimestamp,
                String userType) {
            this(userEmail, userId, userName, adminEmail, adminId, adminName, changeTimestamp, null, userType);
        }
    }

    /**
     * Send password change notification to both user and admin through the role-based dispatch
     * pipeline: one call resolves the full recipient hierarchy (user, client admin, MSP, Aspire
     * Admin) and applies the global -&gt; role -&gt; org -&gt; user gate chain per recipient.
     * Recipient display names are auto-enriched from the registration recipient bundle.
     *
     * @param req encapsulated request containing user and admin details
     */
    @Async
    public void sendPasswordChangeNotificationToUserAndAdmin(PasswordChangeNotificationData req) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(NotificationTemplateValue.USER_NAME, req.userName());
            templateModel.put(NotificationTemplateValue.USER_EMAIL, req.userEmail());
            templateModel.put(NotificationTemplateValue.USER_TYPE, req.userType());
            templateModel.put(NotificationTemplateValue.TIMESTAMP, req.changeTimestamp());
            if (req.eventTimestamp() != null && !req.eventTimestamp().isBlank()) {
                templateModel.put(NotificationTemplateValue.EVENT_TIMESTAMP, req.eventTimestamp());
            }
            templateModel.put(NotificationTemplateValue.MESSAGE, "Your password has been successfully changed.");

            log.info("Dispatching password changed notification for user: {} (admin: {})", req.userName(), req.adminName());

            NotificationEventRequestDto event = NotificationEventRequestDto.builder()
                    .targetUserId(req.userId())
                    .notificationType(NotificationType.USER_PASSWORD_CHANGE)
                    .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                    .templateModel(templateModel)
                    .build();

            boolean dispatched = notificationClient.sendRoleBasedNotification(event);
            log.info("Password change notification event dispatched for user {}: {}", req.userId(), dispatched);
        } catch (Exception e) {
            log.error("Failed to send password change notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification for failed login attempts
     *
     * @param userEmail       user's email address
     * @param userId          user's ID
     * @param userName        user's name
     * @param attemptCount    number of failed attempts
     * @param lastAttemptTime when the last attempt occurred
     * @return true if notification was sent successfully
     */
    public boolean sendFailedLoginAttemptNotification(String userEmail, String userId, String userName,
                                                      int attemptCount, String lastAttemptTime) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            if( !userName.isEmpty()){
                userName = " " + userName;
            } else {
                userName = "" ;
            }

            templateModel.put(NotificationTemplateValue.USER_NAME, userName);
            templateModel.put(NotificationTemplateValue.ATTEMPT_COUNT, String.valueOf(attemptCount));
            templateModel.put(NotificationTemplateValue.LAST_ATTEMPT_TIME, lastAttemptTime);
            templateModel.put(NotificationTemplateValue.MESSAGE, "Multiple failed login attempts detected for your account.");

            log.info("Sending failed login attempt notification for user: {}", userName);

            return notificationClient.sendHighPriorityNotification(
                    userEmail, userId, NotificationType.SECURITY_ALERTS, templateModel);

        } catch (Exception e) {
            log.error("Failed to send failed login attempt notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send password reset notification to user when admin resets their password
     * Includes the new 8-character password in the email
     *
     * @param userEmail    user's email address
     * @param userId       user's ID
     * @param clientAdminId client admin ID
     * @param userName     user's name
     * @param newPassword  the new 8-character password
     */
    @Async
    public void sendAdminPasswordResetNotification(String userEmail, String userId, String clientAdminId,
                                                    String userName, String newPassword) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(NotificationTemplateValue.USER_NAME, userName);
            templateModel.put("password", newPassword);
            templateModel.put(NotificationTemplateValue.MESSAGE, "Your password has been reset by an administrator. Please use the new password provided below to login.");

            log.info("Sending admin password reset notification to user: {}", userName);

            notificationClient.sendMultiChannelNotification(
                    userEmail, userId, clientAdminId, NotificationType.PASSWORD_RESET_BY_ASPIRE, templateModel);

            log.info("Admin password reset notification sent successfully to user: {}", userName);
        } catch (Exception e) {
            log.error("Failed to send admin password reset notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send in-app notification to client admin when admin resets a user's password
     *
     * @param adminEmail    client admin's email address
     * @param adminId       client admin's ID
     * @param clientAdminId client admin ID (same as adminId for client admin)
     * @param adminName     client admin's name
     * @param userName      user's name whose password was reset
     * @param userEmail     user's email whose password was reset
     */
    @Async
    public void sendAdminPasswordResetNotificationToClientAdmin(String adminEmail, String adminId,
                                                                 String clientAdminId, String adminName,
                                                                 String userName, String userEmail) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(NotificationTemplateValue.ADMIN_NAME, adminName);
            templateModel.put(NotificationTemplateValue.USER_NAME, userName);
            templateModel.put(NotificationTemplateValue.USER_EMAIL, userEmail);
            templateModel.put(NotificationTemplateValue.MESSAGE, "A user's password has been reset by an administrator.");

            log.info("Sending admin password reset notification to client admin: {}", adminName);
            notificationClient.sendInAppNotification(adminId, NotificationType.PASSWORD_RESET_BY_ASPIRE, templateModel);

            log.info("Admin password reset notification sent successfully to client admin: {}", adminName);
        } catch (Exception e) {
            log.error("Failed to send admin password reset notification to client admin: {}", e.getMessage(), e);
        }
    }
}
