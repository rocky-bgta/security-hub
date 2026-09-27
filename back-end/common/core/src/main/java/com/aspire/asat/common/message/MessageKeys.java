package com.aspire.asat.common.message;

/**
 * Constants for centralized toast message keys in {@code messages/toast-messages_en.yml}.
 */
public final class MessageKeys {

    private MessageKeys() {
    }

    // Auth
    public static final String AUTH_EMAIL_VERIFIED = "auth.email.verified";
    public static final String AUTH_PASSWORD_UPDATED = "auth.password.updated";

    // Branding
    public static final String BRANDING_SETTINGS_SAVED = "branding.settings.saved";
    public static final String BRANDING_LOGO_UPDATED = "branding.logo.updated";

    // File
    public static final String FILE_SIZE_LIMIT_EXCEEDED = "file.size.limit.exceeded";
    public static final String FILE_EXCEEDS_SIZE_LIMIT = "file.exceeds.size.limit";

    // User
    public static final String USER_ALREADY_EXISTS = "user.already.exists";
    public static final String USER_COMPANY_EMAIL_REQUIRED = "user.company.email.required";
    public static final String USER_CREATED_SUCCESS = "user.created.success";

    // Import
    public static final String IMPORT_USERS_PARTIAL_SUCCESS = "import.users.partial.success";
    public static final String IMPORT_WEBSITE_SUCCESS = "import.website.success";

    // Product
    public static final String PRODUCT_ASSIGNED_SUCCESS = "product.assigned.success";

    // Billing
    public static final String BILLING_PURCHASE_COMPLETED = "billing.purchase.completed";

    // General
    public static final String GENERAL_OPERATION_COMPLETED = "general.operation.completed";

    // Package
    public static final String PACKAGE_SUB_CREATED = "package.sub.created";
    public static final String PACKAGE_SUB_UPDATED = "package.sub.updated";
    public static final String PACKAGE_SUB_ASSIGNED = "package.sub.assigned";

    // Template
    public static final String TEMPLATE_ASSIGNED_SUCCESS = "template.assigned.success";
    public static final String TEMPLATE_UPDATED = "template.updated";
    public static final String TEMPLATE_DUPLICATED = "template.duplicated";
    public static final String TEMPLATE_DELETED = "template.deleted";
    public static final String TEMPLATE_CREATED = "template.created";
    public static final String TEMPLATE_GENERATED = "template.generated";

    // Support
    public static final String SUPPORT_TICKET_CREATED = "support.ticket.created";
    public static final String SUPPORT_COMMENT_ADDED = "support.comment.added";
    public static final String SUPPORT_COMMENT_FAILED = "support.comment.failed";
    public static final String SUPPORT_TICKET_STATUS_UPDATED = "support.ticket.status.updated";

    // Leaderboard
    public static final String LEADERBOARD_ENTRY_CREATED = "leaderboard.entry.created";
    public static final String LEADERBOARD_ENTRY_UPDATED = "leaderboard.entry.updated";
    public static final String LEADERBOARD_ENTRY_REMOVED = "leaderboard.entry.removed";

    // Survey
    public static final String SURVEY_CREATED = "survey.created";
    public static final String SURVEY_UPDATED = "survey.updated";

    // System
    public static final String SYSTEM_JSON_PARSE_ERROR = "system.json.parse.error";
    public static final String SYSTEM_RESOURCE_NOT_FOUND = "system.resource.not.found";
    public static final String SYSTEM_UNEXPECTED_ERROR = "system.unexpected.error";

    // Policy
    public static final String POLICY_CREATED = "policy.created";

    // Notification
    public static final String NOTIFICATION_SETTINGS_UPDATED = "notification.settings.updated";

    // Profile
    public static final String PROFILE_UPDATED = "profile.updated";

    // Domain
    public static final String DOMAIN_LOCKED_ERROR = "domain.locked.error";
    public static final String DOMAIN_VERIFIED = "domain.verified";
    public static final String DOMAIN_LOCKED = "domain.locked.success";
    public static final String DOMAIN_UNLOCKED = "domain.unlocked";
    public static final String DOMAIN_DELETED = "domain.deleted";

    // Verification
    public static final String VERIFICATION_EMAIL_SENT = "verification.email.sent";

    // Campaign
    public static final String CAMPAIGN_DELETED = "campaign.deleted";
    public static final String CAMPAIGN_PAUSED = "campaign.paused";
    public static final String CAMPAIGN_RESUMED = "campaign.resumed";
    public static final String CAMPAIGN_CANCELLED = "campaign.cancelled";
    public static final String CAMPAIGN_LAUNCHED = "campaign.launched";
    public static final String CAMPAIGN_NAME_ALREADY_EXISTS = "campaign.name.already.exists";
    public static final String CAMPAIGN_SAVED_DRAFT = "campaign.saved.draft";

    // Analytics
    public static final String ANALYTICS_STATISTICS_REFRESHED = "analytics.statistics.refreshed";

    // Landing Page
    public static final String LANDING_PAGE_GENERATED = "landing.page.generated";
    public static final String LANDING_PAGE_DUPLICATED = "landing.page.duplicated";
    public static final String LANDING_PAGE_DELETED = "landing.page.deleted";
    public static final String LANDING_PAGE_UPDATED = "landing.page.updated";
    public static final String LANDING_PAGE_CREATED = "landing.page.created";

    // Sender Profile
    public static final String SENDER_PROFILE_DUPLICATED = "sender.profile.duplicated";
    public static final String SENDER_PROFILE_DELETED = "sender.profile.deleted";
    public static final String SENDER_PROFILE_CREATED = "sender.profile.created";
    public static final String SENDER_PROFILE_UPDATED = "sender.profile.updated";

    // Reports
    public static final String REPORTS_EXPORTED = "reports.exported";

    // AI Configuration
    public static final String AI_CONFIGURATION_UPDATED = "ai.configuration.updated";

    // Role & Permission
    public static final String ROLE_PERMISSION_UPDATED = "role.permission.updated";
}
