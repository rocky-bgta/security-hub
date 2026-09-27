package com.aspire.asat.common.enums.notification;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Comprehensive list of notification types available in the ASAT platform
 * Each notification type belongs to a category and can be enabled/disabled by admin
 */
@Getter
public enum NotificationType {
    // User Management
    NEW_USER_REGISTERED("new_user_registered", "New User Registered",
            NotificationCategory.USER_MANAGEMENT, true, true),
    WELCOME_EMAIL("welcome_email", "Welcome Email",
            NotificationCategory.USER_MANAGEMENT, true, true),
    WELCOME_EMAIL_CLIENT_USER("welcome_email_client_user", "Welcome Email",
                  NotificationCategory.USER_MANAGEMENT, true, true),
    WELCOME_CLIENT_EMAIL("welcome_client_email", "Welcome Email to Client Admin",
                  NotificationCategory.USER_MANAGEMENT, true, true),
    WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD("welcome_client_email", "Welcome Email to Client Admin",
            NotificationCategory.USER_MANAGEMENT, true, true),
    USER_PROFILE_UPDATED("user_profile_updated", "User Profile Updated",
            NotificationCategory.USER_MANAGEMENT, true, false),
    PASSWORD_RESET_REQUEST("password_reset_request", "Password Reset Request",
            NotificationCategory.USER_MANAGEMENT, true, true),
    PASSWORD_RESET_BY_ASPIRE("password_reset_request", "Password Reset Request",
                           NotificationCategory.USER_MANAGEMENT, true, true),
    USER_SUSPENDED("user_suspended", "User Suspended/Deactivated",
            NotificationCategory.USER_MANAGEMENT, true, false),
    USER_SUSPENSION("user_suspension", "User Suspended/Deactivated",
                   NotificationCategory.USER_MANAGEMENT, true, true),
    USER_ACTIVATED("user_activated", "User Activeated/Reactivated",
            NotificationCategory.USER_MANAGEMENT, true, true),
    USER_PASSWORD_CHANGE("user_password_change", "User Password Changed",
            NotificationCategory.USER_MANAGEMENT, true, true),
    USER_PASSWORD_CHANGE_ADMIN("user_password_change_admin", "User Password Changed (Admin)",
            NotificationCategory.USER_MANAGEMENT, true, false),
    BULK_USER_IMPORT_SUMMARY("bulk_user_import_summary", "Bulk User Import Summary",
            NotificationCategory.USER_MANAGEMENT, true, true),

    // Content Management
    NEW_COURSE_CREATED("new_course_created", "New Course Created",
            NotificationCategory.CONTENT_MANAGEMENT, true, false),
    COURSE_UPDATED("course_updated", "Course Updated",
            NotificationCategory.CONTENT_MANAGEMENT, true, false),
    COURSE_ASSIGNED("course_assigned", "Course Assigned",
            NotificationCategory.CONTENT_MANAGEMENT, true, false),
    COURSE_COMPLETION("course_completion", "Course Completion",
            NotificationCategory.CONTENT_MANAGEMENT, true, false),
    COURSE_COMPLETION_USER("course_completion_user", "Course Completion (User)",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),

    COURSE_NOT_STARTED_USER("course_not_started_user", "Course Not Started - User Reminder",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),
    COURSE_NOT_STARTED_MANAGER("course_not_started_manager", "Course Not Started - Manager Reminder",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),
    MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL("mandatory_training_assigned_manager_hr_c_level", "Mandatory Training Assigned - Manager/HR/C-Level Notification",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),
    COURSE_NOT_STARTED_C_LEVEL("course_not_started_c_level", "Course Not Started - C-Level Alert",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),
    COURSE_EXPIRY_HR("course_expiry_hr", "Course Expiry - HR Final Reminder",
            NotificationCategory.CONTENT_MANAGEMENT, true, true),

    // Package Management uses as end User which is known as Course
    NEW_PACKAGE_CREATED("new_package_created", "New Package Created",
            NotificationCategory.PACKAGE_MANAGEMENT, true, false),
    PACKAGE_ASSIGNED("package_assigned", "Package Assigned",
            NotificationCategory.PACKAGE_MANAGEMENT, true, false),
    PACKAGE_ASSIGNED_USER("package_assigned_user", "Package Assigned (User)",
            NotificationCategory.PACKAGE_MANAGEMENT, true, true),
    PACKAGE_ASSIGNED_AND_USER_CREDENTIAL("package_assigned_and_user_credential", "Package Assigned (User) and credentials send",
            NotificationCategory.PACKAGE_MANAGEMENT, true, true),
    PACKAGE_EXPIRY("package_expiry", "Package Expiry",
            NotificationCategory.PACKAGE_MANAGEMENT, true, false),
    SUBPACKAGE_EXPIRY_REMINDER("subpackage_expiry_reminder", "SubPackage Expiry Reminder",
            NotificationCategory.PACKAGE_MANAGEMENT, true, true),
    CAMPAIGN_ACTIVITY("campaign_activity", "Campaign Activity",
            NotificationCategory.PACKAGE_MANAGEMENT, true, false),
    CLIENT_ADMIN_PACKAGE_ASSIGNED("client_admin_package_assign", "Client Admin Package Assigned",
            NotificationCategory.PACKAGE_MANAGEMENT, true, true),

    // Payment Management
    PAYMENT_SUCCESS("payment_success", "Payment Success",
            NotificationCategory.PAYMENT_MANAGEMENT, true, true),
    PAYMENT_FAILURE("payment_failure", "Payment Failure",
            NotificationCategory.PAYMENT_MANAGEMENT, true, true),
    PENDING_PAYMENT("pending_payment", "Pending Payment",
            NotificationCategory.PAYMENT_MANAGEMENT, true, false),
    REFUND_REQUESTED("refund_requested", "Refund Requested",
            NotificationCategory.PAYMENT_MANAGEMENT, true, false),

    // Policy Management
    NEW_POLICY_CREATED("new_policy_created", "New Policy Created",
            NotificationCategory.POLICY_MANAGEMENT, true, false),
    POLICY_UPDATED("policy_updated", "Policy Updated",
            NotificationCategory.POLICY_MANAGEMENT, true, false),
    POLICY_COMPLIANCE_REMINDER("policy_compliance_reminder", "Policy Compliance Reminder",
            NotificationCategory.POLICY_MANAGEMENT, true, false),

    // Certificate Management
    CERTIFICATE_ISSUED("certificate_issued", "Certificate Issued",
            NotificationCategory.CERTIFICATE_MANAGEMENT, true, true),
    CERTIFICATE_ISSUED_ADMIN("certificate_issued_admin", "Certificate Issued (Admin)",
            NotificationCategory.CERTIFICATE_MANAGEMENT, true, false),
    LEADERBOARD_UPDATE("leaderboard_update", "Leaderboard Update",
            NotificationCategory.CERTIFICATE_MANAGEMENT, true, false),
    CERTIFICATE_EXPIRY("certificate_expiry", "Certificate Expiry",
            NotificationCategory.CERTIFICATE_MANAGEMENT, true, false),
    CERTIFICATE_EXPIRING("certificate_expiring", "Certificate Expiring Soon",
                       NotificationCategory.CERTIFICATE_MANAGEMENT, true, true),
    CERTIFICATE_REVOKED("certificate_revoked", "Certificate Revoked",
            NotificationCategory.CERTIFICATE_MANAGEMENT, true, false),

    // System/Operational
    SYSTEM_HEALTH_ALERTS("system_health_alerts", "System Health Alerts",
            NotificationCategory.SYSTEM_OPERATIONAL, true, false),
    SECURITY_ALERTS("security_alerts", "Security Alerts",
            NotificationCategory.SYSTEM_OPERATIONAL, true, true),
    UPDATES_PATCHES("updates_patches", "Updates or Patches",
            NotificationCategory.SYSTEM_OPERATIONAL, true, false),
    FEATURE_UPDATE_CHANGE("feature_update_change", "Feature Update/Change",
            NotificationCategory.SYSTEM_OPERATIONAL, true, false),
    DOMAIN_VERIFICATION("domain_verification", "Domain Verification",
            NotificationCategory.SYSTEM_OPERATIONAL, true, true),

    // General
    REMINDER_PENDING_TASKS("reminder_pending_tasks", "Reminder for Pending Tasks",
            NotificationCategory.GENERAL, true, false),
    SCHEDULED_MAINTENANCE("scheduled_maintenance", "Scheduled System Maintenance",
            NotificationCategory.GENERAL, true, false),
    HELP_DESK_TICKET_UPDATES("help_desk_ticket_updates", "Help Desk Ticket Updates",
            NotificationCategory.GENERAL, true, true),
    SUBSCRIPTION_RENEWAL_REMINDER("subscription_renewal_reminder", "Subscription Renewal Reminder",
            NotificationCategory.GENERAL, true, false);

    private final String code;
    private final String displayName;
    private final NotificationCategory category;
    private final boolean enabledByDefault;
    /**
     * When false, the type is hidden from the admin role-settings matrix and
     * notification-templates APIs (still seeded and usable for runtime/legacy).
     */
    private final boolean adminVisible;

    NotificationType(String code, String displayName, NotificationCategory category,
                     boolean enabledByDefault, boolean adminVisible) {
        this.code = code;
        this.displayName = displayName;
        this.category = category;
        this.enabledByDefault = enabledByDefault;
        this.adminVisible = adminVisible;
    }

    /**
     * Priority notification types for initial implementation
     */
    public static final List<NotificationType> PRIORITY_TYPES = Arrays.asList(
            NEW_USER_REGISTERED,
            WELCOME_EMAIL,
            WELCOME_EMAIL_CLIENT_USER,
            WELCOME_CLIENT_EMAIL,
            WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD,
            USER_SUSPENDED,
            USER_SUSPENSION,
            COURSE_ASSIGNED,
            COURSE_COMPLETION,
            PACKAGE_ASSIGNED,
            CERTIFICATE_ISSUED,
            PASSWORD_RESET_BY_ASPIRE
    );

    private static final Set<NotificationType> ADMIN_VISIBLE_TYPES =
            Collections.unmodifiableSet(EnumSet.copyOf(
                    Arrays.stream(values())
                            .filter(NotificationType::isAdminVisible)
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationType.class)))));

    /**
     * Notification types exposed in the admin role-settings and template management APIs.
     */
    public static Set<NotificationType> adminVisibleTypes() {
        return ADMIN_VISIBLE_TYPES;
    }

    /**
     * Ensures the type is managed in the admin portal APIs; otherwise throws.
     */
    public static void requireAdminVisible(NotificationType notificationType) {
        if (notificationType == null || !notificationType.isAdminVisible()) {
            throw new IllegalArgumentException(
                    "Notification type is not managed in the admin portal"
                            + (notificationType == null ? "" : ": " + notificationType.name()));
        }
    }

    /**
     * Get notification type by code
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type code: " + code);
    }

}
