package com.aspire.asat.cms.client;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.enums.ReminderType;
import com.aspire.asat.cms.dto.notification.CertificateNotificationRequest;
import com.aspire.asat.cms.dto.notification.CourseCompletionNotificationRequest;
import com.aspire.asat.cms.dto.notification.SubPackageRemainderNotificationData;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aspire.asat.common.dto.notification.NotificationTemplateValue.*;

/**
 * CMS service-specific notification client
 * Handles notifications related to content management system operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CmsNotificationClient {
    private final NotificationClient notificationClient;
    private final ClientAdminServiceClient clientAdminServiceClient;

    @Value("${link.login:https://dev.aspireelearning.com/auth/login}")
    private String portalUrl;

    @Value("${link.support-email:support@securityawarenesstraining.ai }")
    private String supportEmail;

    /**
     * Send the certificate-issued notification through the role-based dispatch pipeline: one
     * call resolves the full recipient hierarchy (user, client admin, MSP, Aspire Admin) and
     * applies the global -&gt; role -&gt; org -&gt; user gate chain per recipient. Recipient display
     * names are auto-enriched from the registration recipient bundle.
     */
    public boolean sendCertificateIssuedNotification(CertificateNotificationRequest request) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, request.userName());
            templateModel.put(COURSE_TITLE, request.courseTitle());
            templateModel.put(ISSUE_DATE, request.issueDate());

            NotificationEventRequestDto event = NotificationEventRequestDto.builder()
                    .targetUserId(request.userId())
                    .notificationType(NotificationType.CERTIFICATE_ISSUED)
                    .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                    .templateModel(templateModel)
                    .build();

            boolean dispatched = notificationClient.sendRoleBasedNotification(event);
            log.info("Certificate issued notification dispatched for user {}: {}", request.userId(), dispatched);
            return dispatched;

        } catch (Exception e) {
            log.error("Failed to send certificate issued notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send the course-completion notification through the role-based dispatch pipeline: one
     * call resolves the full recipient hierarchy (user, client admin, MSP, Aspire Admin) and
     * applies the global -&gt; role -&gt; org -&gt; user gate chain per recipient. Recipient display
     * names are auto-enriched from the registration recipient bundle.
     */
    public boolean sendCourseCompletionNotifications(CourseCompletionNotificationRequest request) {
        try {
            if (request.userEmail() == null || request.userEmail().isBlank()) {
                log.warn("Cannot send course completion notification: user email is missing for userId={}", request.userId());
                return false;
            }

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(USER_NAME, request.userName());
            templateModel.put(COURSE_TITLE, request.courseTitle());
            templateModel.put(COURSE_COMPLETION_DATE, request.completionDate());
            templateModel.put(TIMESTAMP, request.completionTimestamp());
            templateModel.put(LOGIN_URL, portalUrl);
            templateModel.put(SUPPORT_EMAIL, supportEmail);

            NotificationEventRequestDto event = NotificationEventRequestDto.builder()
                    .targetUserId(request.userId())
                    .notificationType(NotificationType.COURSE_COMPLETION_USER)
                    .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
                    .templateModel(templateModel)
                    .build();

            boolean dispatched = notificationClient.sendRoleBasedNotification(event);
            log.info("Course completion notification dispatched for user {}: {}", request.userId(), dispatched);
            return dispatched;

        } catch (Exception e) {
            log.error("Failed to send course completion notifications: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send subpackage expiry reminder notification based on reminder type
     * Sends different notification types for different reminder types:
     * - REMINDER_NOT_STARED -> COURSE_NOT_STARTED_USER (to user - both IN_APP and EMAIL)
     * - REMINDER_50_PERCENT -> COURSE_NOT_STARTED_MANAGER (to managers - EMAIL, IN_APP to user)
     * - REMINDER_20_PERCENT -> COURSE_NOT_STARTED_C_LEVEL (to C-level - EMAIL, IN_APP to user)
     * - REMINDER_10_PERCENT -> SUBPACKAGE_EXPIRY_REMINDER (to HR - EMAIL, IN_APP to user)
     * - REMINDER_EXPIRY -> COURSE_EXPIRY_HR (to HR - EMAIL, IN_APP to user)
     *
     * @param data         notification data containing user info, additional emails, and reminder details
     * @param reminderType the type of reminder being sent
     * @return true if all notifications were sent successfully
     */
    public boolean sendSubPackageExpiryReminder(SubPackageRemainderNotificationData data, ReminderType reminderType) {
        try {
            // Map reminder type to notification type
            NotificationType notificationType = getNotificationTypeForReminder(reminderType);

            // Build template model based on a reminder type
            Map<String, Object> templateModel = buildTemplateModelForReminderType(data, reminderType);

            boolean allSent = true;

            // Determine recipients based on a reminder type
            Set<String> inAppRecipients = new LinkedHashSet<>();
            List<String> emailRecipients = new ArrayList<>();

            reminderTypeWiseEmail(data, reminderType, inAppRecipients, emailRecipients);

            // Send IN_APP notifications
            if (inAppRecipients.isEmpty()) {
                log.debug("No in-app recipient IDs provided for reminder type: {}", reminderType);
            } else {
                for (String recipientId : inAppRecipients) {
                    log.debug("Sending in-app notification to: {} for reminder type: {}", recipientId, reminderType);
                    if (!sendInAppNotification(recipientId, notificationType, templateModel)) {
                        allSent = false;
                    }
                }
            }

            // Send EMAIL notifications
            if (emailRecipients.isEmpty()) {
                log.debug("No email recipients for reminder type: {}", reminderType);
            } else {
                log.debug("Sending email notifications to {} recipients for reminder type: {}",
                        emailRecipients.size(), reminderType);
                if (!sendEmailsToRecipients(emailRecipients, notificationType, templateModel)) {
                    allSent = false;
                }
            }

            log.info("Subpackage reminder processed - Type: {}, In-app recipients: {}, Emails to {} recipients",
                    reminderType, inAppRecipients.size(), emailRecipients.size());

            return allSent;
        } catch (Exception e) {
            log.error("Failed to send subpackage expiry reminder notification for type {}: {}",
                    reminderType, e.getMessage(), e);
            return false;
        }
    }

    private void reminderTypeWiseEmail(SubPackageRemainderNotificationData data, ReminderType reminderType, Set<String> inAppRecipients, List<String> emailRecipients) {
        switch (reminderType) {
            case REMINDER_NOT_STARED -> {
                // Send to user only (both IN_APP and EMAIL)
                addIfPresent(inAppRecipients, data.userId());
                if (data.userEmail() != null && !data.userEmail().trim().isEmpty()) {
                    emailRecipients.add(data.userEmail());
                }
            }
            case FIRST_REMINDER -> {
                // Send IN_APP to user, EMAIL to managers
                addIfPresent(inAppRecipients, data.userId());
                emailRecipients.addAll(filterValidEmails(data.additionalEmails()));
            }
            case SECOND_REMINDER -> {
                // Send IN_APP to the user, EMAIL to C-level
                addIfPresent(inAppRecipients, data.userId());
                emailRecipients.addAll(filterValidEmails(data.additionalEmails()));
            }
            case THIRD_REMINDER -> {
                // Send IN_APP to user, EMAIL to HR
                addIfPresent(inAppRecipients, data.userId());
                emailRecipients.addAll(filterValidEmails(data.additionalEmails()));
            }
            case REMINDER_EXPIRY -> {
                // Send IN_APP to user, USER, ADMIN
                addIfPresent(inAppRecipients, data.userId());
                emailRecipients.addAll(List.of(data.userEmail(), data.clientAdminEmail()));
            }
        }
    }

    /**
     * Map reminder type to notification type
     */
    private NotificationType getNotificationTypeForReminder(ReminderType reminderType) {
        return switch (reminderType) {
            case REMINDER_NOT_STARED -> NotificationType.COURSE_NOT_STARTED_USER;
            case FIRST_REMINDER -> NotificationType.COURSE_NOT_STARTED_MANAGER;
            case SECOND_REMINDER -> NotificationType.COURSE_NOT_STARTED_C_LEVEL;
            case THIRD_REMINDER -> NotificationType.SUBPACKAGE_EXPIRY_REMINDER;
            case REMINDER_EXPIRY -> NotificationType.COURSE_EXPIRY_HR;
        };
    }

    /**
     * Build template model based on reminder type
     */
    private Map<String, Object> buildTemplateModelForReminderType(SubPackageRemainderNotificationData data, ReminderType reminderType) {
        Map<String, Object> templateModel = new HashMap<>();

        // Common fields
        templateModel.put("subPackageName", data.subPackageName());

        LocalDate expiryDate = data.expiryDate();
        if (expiryDate != null) {
            templateModel.put(EXPIRATION_DATE, expiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            templateModel.put("expiryDate", expiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        }
        templateModel.put(DAYS_REMAINING, data.daysRemaining());

        // Use actual assigned date from data, or calculate from expiry and days remaining
        LocalDate assignmentDate = data.assignedDate();
        if (assignmentDate == null && expiryDate != null && data.daysRemaining() > 0) {
            // Fallback: estimate assignment date from expiry date and days remaining
            // This is approximate and should ideally come from UserSubPackage.assignedDate
            long estimatedTotalDays = (long) (data.daysRemaining() / (1.0 - (data.reminderPercentage() / 100.0)));
            assignmentDate = expiryDate.minusDays(estimatedTotalDays);
        }

        // Reminder type specific fields
        switch (reminderType) {
            case REMINDER_NOT_STARED -> {
                // For COURSE_NOT_STARTED_USER
                templateModel.put(RECEIVER_ROLE_NAME, data.clientAdminName() != null ? data.clientAdminName() : "User");
                templateModel.put(USER_NAME, data.clientAdminName() != null ? data.clientAdminName() : "User");
                templateModel.put(ASSIGNMENT_DATE, assignmentDate != null ?
                        assignmentDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A");
            }
            case FIRST_REMINDER -> {
                // For COURSE_NOT_STARTED_MANAGER
                templateModel.put(RECEIVER_ROLE_NAME, "Sir / Madam");
                String clientAdminName = data.clientAdminName() != null ? data.clientAdminName() : "Employee";
                templateModel.put(USER_NAME, clientAdminName);
                templateModel.put(ASSIGNMENT_DATE, assignmentDate != null ?
                        assignmentDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A");
            }
            case SECOND_REMINDER -> {
                // For COURSE_NOT_STARTED_C_LEVEL
                templateModel.put(RECEIVER_ROLE_NAME, "Sir / Madam");
                templateModel.put(USER_NAME, data.clientAdminName() != null ? data.clientAdminName() : "Employee");
                long daysSinceAssignment = 21; // Default
                if (assignmentDate != null) {
                    daysSinceAssignment = ChronoUnit.DAYS.between(assignmentDate, LocalDate.now());
                }
                templateModel.put("daysSinceAssignment", daysSinceAssignment);
                templateModel.put(ASSIGNMENT_DATE, assignmentDate != null ?
                        assignmentDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A");
            }
            case THIRD_REMINDER -> {
                // For SUBPACKAGE_EXPIRY_REMINDER (existing behavior)
                templateModel.put(PACKAGE_NAME, data.subPackageName());
                int reminderPercentage = data.reminderPercentage();
                templateModel.put("reminderMessage", getReminderMessage(reminderPercentage, data.daysRemaining()));
                templateModel.put("reminderPercentage", reminderPercentage > 0 ? reminderPercentage + "%" : "Expiring Today");
            }
            case REMINDER_EXPIRY -> {
                // For COURSE_EXPIRY_HR
                templateModel.put(RECEIVER_ROLE_NAME, "HR");
                templateModel.put(USER_NAME, data.clientAdminName() != null ? data.clientAdminName() : "Employee");
                templateModel.put("expiryDate", expiryDate != null ?
                        expiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A");
            }
        }

        return templateModel;
    }

    private void addIfPresent(Set<String> set, String id) {
        if (id != null && !id.trim().isEmpty()) {
            set.add(id);
        }
    }

    private List<String> filterValidEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Collections.emptyList();
        }
        return emails.stream()
                .filter(email -> email != null && !email.trim().isEmpty())
                .distinct()
                .toList();
    }

    private boolean sendInAppNotification(String userId, NotificationType notificationType, Map<String, Object> templateModel) {
        boolean sent = notificationClient.sendInAppNotification(userId, notificationType, templateModel);
        if (!sent) {
            log.warn("Failed to send in-app notification to user: {} for type: {}", userId, notificationType);
        } else {
            log.debug("Successfully sent in-app notification to user: {} for type: {}", userId, notificationType);
        }
        return sent;
    }

    private boolean sendEmailsToRecipients(List<String> recipients, NotificationType type, Map<String, Object> templateModel) {
        boolean allSent = true;
        for (String email : recipients) {
            boolean emailSent = notificationClient.sendEmailNotification(email, type, templateModel);
            if (!emailSent) {
                log.warn("Failed to send reminder email to: {}", email);
                allSent = false;
            } else {
                log.debug("Successfully sent reminder email to: {}", email);
            }
        }
        return allSent;
    }

    /**
     * Generate a reminder message based on percentage and days remaining
     */
    private String getReminderMessage(int reminderPercentage, long daysRemaining) {
        return switch (reminderPercentage) {
            case 0 -> "Your subpackage is expiring today! Please complete it before it expires.";
            case 10 ->
                    String.format("Only %d days remaining! Your subpackage will expire soon. Please complete it before expiry.", daysRemaining);
            case 20 ->
                    String.format("Your subpackage has %d days remaining. Please continue your learning to avoid expiration.", daysRemaining);
            case 50 ->
                    String.format("Your subpackage has %d days remaining. This is a reminder to continue your learning journey.", daysRemaining);
            default -> String.format("Your subpackage has %d days remaining before expiry.", daysRemaining);
        };
    }

    /**
     * Send certificate expiring notification to client admin with Excel attachment.
     * The Excel file should already be generated and uploaded to S3 before calling this method.
     *
     * @param adminId client admin ID
     * @param adminEmail client admin email address
     * @param adminName client admin name/organization name
     * @param certificateCount number of certificates in the export
     * @param attachment S3 attachment details (bucket name and object key)
     * @return true if notification was sent successfully, false otherwise
     */
    public boolean sendCertificateExpiringNotification(String adminId, String adminEmail, String adminName, 
                                                       String companyName,
                                                       int certificateCount, AttachmentDto attachment) {
        try {
            log.info("Sending certificate expiring notification - adminId: {}, adminEmail: {}, certificateCount: {}", 
                    adminId, adminEmail, certificateCount);

            // Validate inputs
            if (adminId == null || adminId.isBlank()) {
                log.error("Client admin ID cannot be null or empty");
                return false;
            }
            if (adminEmail == null || adminEmail.isBlank()) {
                log.error("Client admin email cannot be null or empty");
                return false;
            }
            if (attachment == null) {
                log.error("Attachment cannot be null");
                return false;
            }

            // Prepare template model
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(ADMIN_NAME, adminName != null ? adminName : "Client Admin");
            templateModel.put("certificateCount", certificateCount);
            templateModel.put("exportDate", Instant.now().toString());
            templateModel.put("companyName", companyName);
            templateModel.put("expirationDate", LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            templateModel.put("portalURL", portalUrl);
            templateModel.put("supportEmail", supportEmail);

            // Send email notification with attachment
            boolean sent = notificationClient.sendCustomChannelNotification(
                    adminEmail,
                    adminId,
                    adminId,
                    NotificationType.CERTIFICATE_EXPIRING,
                    List.of(NotificationChannel.EMAIL),
                    templateModel,
                    List.of(attachment)
            );

            if (sent) {
                log.info("Certificate expiring notification sent successfully to: {}", adminEmail);
            } else {
                log.warn("Failed to send certificate expiring notification to: {}", adminEmail);
            }

            return sent;

        } catch (Exception e) {
            log.error("Failed to send certificate expiring notification - adminId: {}, Error: {}", 
                    adminId, e.getMessage(), e);
            return false;
        }
    }
}
