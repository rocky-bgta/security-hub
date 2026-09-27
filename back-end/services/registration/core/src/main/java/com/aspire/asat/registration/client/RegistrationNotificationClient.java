package com.aspire.asat.registration.client;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.util.DatetimeUtils;
import com.aspire.asat.registration.client.service.AuthServiceClient;
import com.aspire.asat.registration.data.auth.request.PasswordResetRequestDto;
import com.aspire.asat.registration.data.auth.response.PasswordResetTokenResponseDto;
import com.aspire.asat.registration.dto.notification.BulkImportSummaryDto;
import com.aspire.asat.registration.dto.notification.NewUserNotificationDto;
import com.aspire.asat.registration.dto.notification.PackageAssignmentNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.aspire.asat.common.dto.notification.NotificationTemplateValue.*;

/**
 * Registration-service-specific notification client
 * Handles notifications related to user registration and onboarding processes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationNotificationClient {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private final NotificationClient notificationClient;
    private final AuthServiceClient authServiceClient;

    @Value("${service.auth.password-reset-token-expiration-second: 28800}")
    private Long tokenExpirySeconds;

    @Value("${service.auth.password-reset}")
    private String resetBaseUrl;
    /**
     * Send welcome email notification after successful registration with client admin ID
     *
     * @param userEmail user's email address
     * @param userId    user's ID
     * @param clientAdminId client admin ID for client-specific notification settings (optional)
     * @param userName  user's name
     * @param password  temporary password
     */
    public void sendWelcomeEmailNotification(String userEmail, String userId, String clientAdminId, String userName, String password) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, userName);
            templateModel.put(EMAIL, userEmail);
            templateModel.put(USER_ID, userId);
            templateModel.put(PASSWORD, password);

            log.info("Sending welcome email notification for user: {} (client: {})", userName, clientAdminId);

            notificationClient.sendMultiChannelNotification(
                    userEmail, userId, clientAdminId, NotificationType.WELCOME_EMAIL, templateModel);
        } catch (Exception e) {
            log.error("Failed to send welcome email notification for user {}: {}", userName, e.getMessage(), e);
        }
    }


    /**
     * Send welcome email notification after successful registration with client admin ID
     *
     * @param userEmail user's email address
     * @param userId    user's ID
     * @param clientAdminId client admin ID for client-specific notification settings (optional)
     * @param userName  user's name
     */
    public void sendWelcomeClientEmailWithoutPasswordNotification(String userEmail, String userId, String clientAdminId, String userName, String mspName, String mspEmail) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, userName);
            templateModel.put(EMAIL, userEmail);
            templateModel.put(USER_ID, userId);
            templateModel.put(MSP_NAME, mspName);
            templateModel.put(MSP_EMAIL, mspEmail);

            log.info("Sending welcome email notification for user: {} (client: {})", userName, clientAdminId);

            notificationClient.sendMultiChannelNotification(
                    userEmail, userId, clientAdminId, NotificationType.WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD, templateModel);
        } catch (Exception e) {
            log.error("Failed to send welcome email notification for user {}: {}", userName, e.getMessage(), e);
        }
    }


    /**
     * Send welcome email notification after successful registration with client admin ID
     *
     * @param userEmail user's email address
     * @param userId    user's ID
     * @param clientAdminId client admin ID for client-specific notification settings (optional)
     * @param userName  user's name
     * @param password  temporary password
     */
    public void sendWelcomeEmaiWithoutPasswordlNotification(String userEmail, String userId, String clientAdminId, String userName, String password) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, userName);
            templateModel.put(EMAIL, userEmail);
            templateModel.put(USER_ID, userId);
            templateModel.put(PASSWORD, password);

            log.info("Sending welcome email notification for user: {} (client: {})", userName, clientAdminId);

            notificationClient.sendMultiChannelNotification(
                    userEmail, userId, clientAdminId, NotificationType.WELCOME_EMAIL_CLIENT_USER, templateModel);
        } catch (Exception e) {
            log.error("Failed to send welcome email notification for user {}: {}", userName, e.getMessage(), e);
        }
    }

    
    /**
     * Send a welcome email notification with attachments and client admin ID
     */
    @Async
    public void sendWelcomeEmailNotification(String userEmail, String userId, String clientAdminId, String userName, String password, List<AttachmentDto> attachments) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, userName);
            templateModel.put(EMAIL, userEmail);
            templateModel.put(USER_ID, userId);
            templateModel.put(PASSWORD, password);

            log.info("Sending welcome email notification with attachment for user: {} (client: {})", userName, clientAdminId);

            notificationClient.sendCustomChannelNotification(
                userEmail,
                userId,
                clientAdminId,
                NotificationType.WELCOME_EMAIL,
                List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                templateModel,
                attachments
            );
        } catch (Exception e) {
            log.error("Failed to send welcome email notification with attachment for user {}: {}", userName, e.getMessage(), e);
        }
    }

    /**
     * Send a welcome email notification with attachments, client admin ID, and custom template model (for coupon information)
     */
    @Async
    public void sendWelcomeEmailNotificationWithCoupon(String userEmail, String userId, String clientAdminId, String userName, String password, Map<String, Object> templateModel, List<AttachmentDto> attachments) {
        try {
            log.info("Sending welcome email notification with attachment and custom template model for user: {} (client: {})", userName, clientAdminId);

            notificationClient.sendCustomChannelNotification(
                userEmail,
                userId,
                clientAdminId,
                NotificationType.WELCOME_CLIENT_EMAIL,
                List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                templateModel,
                attachments
            );
        } catch (Exception e) {
            log.error("Failed to send welcome email notification with attachment and custom template model for user {}: {}", userName, e.getMessage(), e);
        }
    }
    
    /**
     * Send product reassignment/admin notification with attachments and client admin ID
     */
    public void sendProductReassignmentNotification(String to, String userId, String clientAdminId, Map<String, Object> templateModel, List<AttachmentDto> attachments) {
        try {
            log.info("Sending product reassignment notification to: {} (client: {})", to, clientAdminId);
            notificationClient.sendCustomChannelNotification(
                to,
                userId,
                clientAdminId,
                NotificationType.CLIENT_ADMIN_PACKAGE_ASSIGNED,
                List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                templateModel,
                attachments
            );
        } catch (Exception e) {
            log.error("Failed to send product reassignment notification to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send a notification when a new user registration is completed
     *
     * @param requestDto notification request data
     */
    public void sendNewUserNotificationToAdmin(NewUserNotificationDto requestDto) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, requestDto.getUserName());
            templateModel.put(EMAIL, requestDto.getUserEmail());
            templateModel.put(ADMIN_NAME, requestDto.getAdminName());
            templateModel.put(REGISTRATION_DATE, DatetimeUtils.getDateOnlyFormat(requestDto.getRegistrationDate()));

            log.info("Sending registration completed notification for user: {} (client: {})", 
                    requestDto.getUserName(), requestDto.getClientAdminId());

            notificationClient.sendMultiChannelNotification(
                    requestDto.getAdminEmail(), 
                    requestDto.getUserId(), 
                    requestDto.getClientAdminId(), 
                    NotificationType.NEW_USER_REGISTERED, 
                    templateModel);
        } catch (Exception e) {
            log.error("Failed to send new user notification to admin for user {}: {}", requestDto.getUserName(), e.getMessage(), e);
        }
    }

    /**
     * Send package assignment notification to user and admin through the role-based dispatch
     * pipeline: one call resolves the full recipient hierarchy (user, client admin, MSP, Aspire
     * Admin) and applies the global -&gt; role -&gt; org -&gt; user gate chain per recipient.
     * Recipient display names are auto-enriched from the registration recipient bundle.
     *
     * @param requestDto notification request data
     */
    @Async
    public void sendPackageAssignedNotification(PackageAssignmentNotificationDto requestDto, boolean isSendCredentials) {
        try {
            Map<String, Object> userTemplateModel = new HashMap<>();
            userTemplateModel.put(USER_NAME, requestDto.getUserName());
            userTemplateModel.put(PACKAGE_NAME, requestDto.getPackageName());
            userTemplateModel.put(PACKAGE_DETAILS, requestDto.getPackageDetails());
            userTemplateModel.put(ASSIGNMENT_DATE, requestDto.getAssignmentDate());
            userTemplateModel.put(EMAIL, requestDto.getUserEmail());
            userTemplateModel.put(USER_ID, requestDto.getUserId());
            userTemplateModel.put(PASSWORD, requestDto.getTempPassword());
            // Calculate expiry time in human-readable format
            String expiryTime = formatExpiryTime(tokenExpirySeconds);
            userTemplateModel.put(NotificationTemplateValue.EXPIRY_TIME, expiryTime);

            NotificationType userNotificationType;

            // Check if credentials should be sent
            if (isSendCredentials) {
                try {
                    // Auth Service call to generate password reset token
                    PasswordResetRequestDto passwordResetRequest = PasswordResetRequestDto.builder()
                            .username(requestDto.getUserEmail())
                            .build();
                    PasswordResetTokenResponseDto resetToken = authServiceClient.generatePasswordResetToken(passwordResetRequest);

                    if (resetToken != null && resetToken.getToken() != null) {
                        // Build reset URL
                        String resetUrl = resetBaseUrl + "?token=" + resetToken.getToken();
                        userTemplateModel.put(NotificationTemplateValue.RESET_URL, resetUrl);
                        userTemplateModel.put(MESSAGE, "Use the link below to change your password.");
                        log.info("Successfully generated password reset token and added reset URL for user: {}", requestDto.getUserEmail());
                    } else {
                        log.warn("Failed to generate password reset token for user: {}. Token is null.", requestDto.getUserEmail());
                    }
                } catch (Exception e) {
                    log.error("Failed to generate token for user course assignment notification for user: {}: {}",
                            requestDto.getUserEmail(), e.getMessage(), e);
                }

                userNotificationType = NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL;
            } else {
                userNotificationType = NotificationType.PACKAGE_ASSIGNED_USER;
            }

            NotificationEventRequestDto event = NotificationEventRequestDto.builder()
                    .targetUserId(requestDto.getUserId())
                    .notificationType(userNotificationType)
                    .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                    .templateModel(userTemplateModel)
                    .build();

            boolean dispatched = notificationClient.sendRoleBasedNotification(event);
            log.info("Package assignment notification event dispatched for user {} (credentials: {}): {}",
                    requestDto.getUserId(), isSendCredentials, dispatched);
        } catch (Exception e) {
            log.error("Failed to send package assignment notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send package assignment notification to additional recipients
     * (secondary emails, third-level emails, fourth HR emails)
     *
     * @param requestDto notification request data
     * @param additionalEmails list of additional email addresses to notify
     */
    @Async
    public void sendPackageAssignNotificationForAdditional(PackageAssignmentNotificationDto requestDto, List<String> additionalEmails, String receiverRoleName) {
        if (additionalEmails == null || additionalEmails.isEmpty()) {
            return;
        }

        for (String email : additionalEmails) {
            String trimmedEmail = email != null ? email.trim() : null;
            
            // Skip null, empty, or invalid email addresses
            if (trimmedEmail == null || trimmedEmail.isEmpty() || !isValidEmail(trimmedEmail)) {
                if (trimmedEmail != null && !trimmedEmail.isEmpty()) {
                    log.warn("Skipping invalid email address for additional recipient notification: {}", trimmedEmail);
                }
                continue;
            }

            try {
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put(USER_NAME, requestDto.getUserName());
                templateModel.put(COURSE_NAME, requestDto.getPackageName());
                templateModel.put(RECEIVER_ROLE_NAME, receiverRoleName);
                templateModel.put(ASSIGNMENT_DATE, requestDto.getAssignmentDate());
                templateModel.put(EXPIRATION_DATE, requestDto.getExpirationDate());
                templateModel.put(DEPARTMENT_NAME, requestDto.getDepartmentName());
                templateModel.put(COMPANY_NAME, requestDto.getCompanyName());
                templateModel.put(CURRENT_YEAR, String.valueOf(java.time.LocalDate.now().getYear()));
                templateModel.put(NotificationTemplateValue.LOGO_URL, requestDto.getLogoUrl() != null ? requestDto.getLogoUrl() : null);

                boolean notificationSent = notificationClient.sendEmailNotification(
                        trimmedEmail,
                        NotificationType.MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL,
                        templateModel);

                log.info("Package assignment notification sent to additional recipient: {} - Success: {} (client: {})",
                        trimmedEmail, notificationSent, requestDto.getClientAdminId());

            } catch (Exception e) {
                log.error("Failed to send package assignment notification to additional recipient {}: {}",
                        trimmedEmail, e.getMessage(), e);
            }
        }
    }

    public void sendBulkImportSummaryNotification(BulkImportSummaryDto requestDto) {
        try {
            // Format imported users list as text
            StringBuilder importedUsersList = new StringBuilder();
            if (requestDto.getImportedUsers() != null && !requestDto.getImportedUsers().isEmpty()) {
                importedUsersList.append(String.format("%-30s %-30s %-15s %-20s\n", "Name", "Email", "Phone", "Department"));
                importedUsersList.append("-".repeat(95)).append("\n");
                for (BulkImportSummaryDto.ImportedUserInfo user : requestDto.getImportedUsers()) {
                    importedUsersList.append(String.format("%-30s %-30s %-15s %-20s\n", 
                        user.getFullName() != null ? user.getFullName() : "N/A",
                        user.getEmail() != null ? user.getEmail() : "N/A",
                        user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A",
                        user.getDepartment() != null ? user.getDepartment() : "N/A"));
                }
            } else {
                importedUsersList.append("No users were successfully imported.\n");
            }

            // Format skipped users list as text
            StringBuilder skippedUsersList = new StringBuilder();
            if (requestDto.getSkippedUsers() != null && !requestDto.getSkippedUsers().isEmpty()) {
                skippedUsersList.append(String.format("%-30s %-30s %-30s\n", "Name", "Email", "Reason"));
                skippedUsersList.append("-".repeat(95)).append("\n");
                for (BulkImportSummaryDto.SkippedUserInfo user : requestDto.getSkippedUsers()) {
                    skippedUsersList.append(String.format("%-30s %-30s %-30s\n",
                        user.getFullName() != null ? user.getFullName() : "N/A",
                        user.getEmail() != null ? user.getEmail() : "N/A",
                        user.getReason() != null ? user.getReason() : "Unknown"));
                }
            } else {
                skippedUsersList.append("No users were skipped.\n");
            }

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(ADMIN_NAME, requestDto.getAdminName());
            templateModel.put("totalImported", requestDto.getTotalImported());
            templateModel.put("totalSkipped", requestDto.getTotalSkipped());
            templateModel.put("importedUsersList", importedUsersList.toString());
            templateModel.put("skippedUsersList", skippedUsersList.toString());
            templateModel.put("importDate", requestDto.getImportDate());

            log.info("Sending bulk import summary notification to admin: {} (client: {})", 
                    requestDto.getAdminEmail(), requestDto.getClientAdminId());

            boolean notificationSent = notificationClient.sendMultiChannelNotification(
                requestDto.getAdminEmail(), 
                requestDto.getAdminId(), 
                requestDto.getClientAdminId(), // Admin's clientAdminId for preference management
                NotificationType.BULK_USER_IMPORT_SUMMARY, 
                templateModel);

            log.info("Bulk import summary notification sent: {} (client: {})", 
                    notificationSent, requestDto.getClientAdminId());

        } catch (Exception e) {
            log.error("Failed to send bulk import summary notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send user status change notification (suspend or activate)
     * Notifies the user when their account status is changed to SUSPEND or ACTIVE
     *
     * @param userEmail user's email address
     * @param status new status (SUSPEND or ACTIVE)
     * @param suspendReasonName name of the suspend reason (optional, only for SUSPEND status)
     * @param userName user's full name
     */
    @Async
    public void sendUserStatusChangeNotification(
            String userEmail,
            String status,
            String suspendReasonName,
            String userName,
            String companyName) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, userName);
            templateModel.put(EMAIL, userEmail);
            templateModel.put(COMPANY_NAME, companyName);
            templateModel.put(CURRENT_YEAR, String.valueOf(java.time.LocalDate.now().getYear()));
            templateModel.put(TIMESTAMP, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(java.time.LocalDateTime.now()));

            NotificationType notificationType = null;
            // Add status-specific information
            if ("SUSPEND".equalsIgnoreCase(status)) {
                templateModel.put("status", "Suspended");
                templateModel.put("statusMessage", "Your account has been suspended.");
                if (suspendReasonName != null && !suspendReasonName.isBlank()) {
                    templateModel.put(REASON, suspendReasonName);
                }
                notificationType = NotificationType.USER_SUSPENSION;

            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                templateModel.put("status", "Activated");
                templateModel.put("statusMessage", "Your account has been activated.");
                notificationType  = NotificationType.USER_ACTIVATED;
            }

            log.info("Sending user status change notification for user: {} (status: {},)",
                    userName, status);

            notificationClient.sendEmailNotification(
                    userEmail,
                    notificationType,
                    templateModel
            );

            log.info("User status change notification sent successfully to: {} (status: {})", 
                    userEmail, status);

        } catch (Exception e) {
            log.error("Failed to send user status change notification for user {}: {}", 
                    userName, e.getMessage(), e);
        }
    }

    /**
     * Validates email format using standard email regex pattern
     *
     * @param email the email address to validate
     * @return true if email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Format expiry time in human-readable format
     *
     * @param seconds number of seconds
     * @return formatted string (e.g., "1 hour", "30 minutes")
     */
    private String formatExpiryTime(Long seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return hours == 1? "1 hour" : hours + " hours";
        } else {
            return minutes == 1 ? "1 minute" : minutes + " minutes";
        }
    }

}
