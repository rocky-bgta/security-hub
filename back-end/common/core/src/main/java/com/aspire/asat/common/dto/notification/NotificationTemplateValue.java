package com.aspire.asat.common.dto.notification;

/**
 * Constants for notification template variables
 * Centralized location for all template variable names used across the notification system
 */
public class NotificationTemplateValue {

    private NotificationTemplateValue(){}

    // ========================================
    // COMMON TEMPLATE VARIABLES
    // ========================================
    
    // User Information
    public static final String USER_NAME = "userName";
    public static final String EMAIL = "email";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_ID = "userId";
    public static final String USER_TYPE = "userType";
    public static final String PASSWORD = "password";
    public static final String MSP_EMAIL = "mspEmail";
    public static final String MSP_NAME = "mspName";

    public static final String RESET_URL = "resetUrl";
    public static final String EXPIRY_TIME = "expiryTime";
    
    // Admin Information
    public static final String ADMIN_NAME = "adminName";
    public static final String ADMIN_ID = "adminId";
    public static final String CLIENT_ADMIN_NAME = "clientAdminName";
    public static final String ASPIRE_ADMIN_NAME = "aspireAdminName";
    public static final String ORGANIZATION_NAME = "organizationName";
    
    // Dates and Timestamps
    public static final String REGISTRATION_DATE = "registrationDate";
    public static final String TIMESTAMP = "timestamp";
    /**
     * UTC instant (ISO-8601) for an event. Notification enricher converts this into
     * {@link #TIMESTAMP} using the organization timezone from the recipient bundle.
     */
    public static final String EVENT_TIMESTAMP = "eventTimestamp";
    public static final String CURRENT_YEAR = "currentYear";

    // Branding and System
    public static final String COMPANY_NAME = "companyName";
    public static final String LOGO_URL = "logoUrl";
    public static final String LOGIN_URL = "loginUrl";
    public static final String SUPPORT_EMAIL = "supportEmail";
    public static final String SUPPORT_CONTACT = "supportContact";
    public static final String BASE_URL = "baseUrl";

    // ========================================
    // WELCOME EMAIL TEMPLATE VARIABLES
    // ========================================
    
    public static final String PLATFORM_FEATURES = "platformFeatures";
    public static final String MESSAGE = "message";

    // ========================================
    // USER MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    // User Profile Updates
    public static final String UPDATED_FIELDS = "updatedFields";
    
    // User Suspension
    public static final String REASON = "reason";
    
    // Password Management
    public static final String RESET_TOKEN = "resetToken";
    public static final String CHANGE_TIMESTAMP = "changeTimestamp";
    
    // Security
    public static final String ATTEMPT_COUNT = "attemptCount";
    public static final String LAST_ATTEMPT_TIME = "lastAttemptTime";

    // ========================================
    // COURSE MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    // Course Information
    public static final String COURSE_TITLE = "courseTitle";
    public static final String COURSE_NAME = "courseName";
    public static final String COURSE_CODE = "courseCode";
    public static final String COURSE_DESCRIPTION = "courseDescription";
    public static final String COURSE_URL = "courseUrl";
    
    // Course Management
    public static final String PERMISSIONS = "permissions";
    public static final String CREATED_BY = "createdBy";
    public static final String UPDATED_BY = "updatedBy";
    
    // Course Assignment
    public static final String ASSIGNMENT_DATE = "assignmentDate";
    public static final String ASSIGNED_BY = "assignedBy";
    public static final String COURSE_DUE_DATE = "dueDate";
    public static final String ESTIMATED_DURATION = "estimatedDuration";
    
    // Course Completion
    public static final String COURSE_COMPLETION_DATE = "completionDate";
    public static final String SCORE = "score";
    public static final String TIME_SPENT = "timeSpent";
    public static final String CERTIFICATE_ELIGIBLE = "certificateEligible";

    // ========================================
    // PACKAGE MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    // Package Information
    public static final String PACKAGE_NAME = "packageName";
    public static final String PACKAGE_DETAILS = "packageDetails";
    public static final String CONTENTS = "contents";
    public static final String PRICING = "pricing";

    // Package Assignment
    public static final String RECEIVER_ROLE_NAME = "receiverName";
    public static final String DEPARTMENT_NAME = "departmentName";
    // Package Management
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String DAYS_REMAINING = "daysRemaining";
    
    // Campaign Activity
    public static final String CAMPAIGN_TYPE = "campaignType";
    public static final String USER_ENGAGEMENT = "userEngagement";

    // ========================================
    // PAYMENT MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    // Payment Information
    public static final String AMOUNT = "amount";
    public static final String METHOD = "method";
    public static final String FAILURE_REASON = "failureReason";
    public static final String PAYMENT_DUE_DATE = "dueDate";
    public static final String REFUND_REASON = "refundReason";

    // ========================================
    // CERTIFICATE MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    // Certificate Information
    public static final String CERTIFICATE_ID = "certificateId";
    public static final String ISSUE_DATE = "issueDate";
    public static final String VALID_UNTIL = "validUntil";
    public static final String CERTIFICATE_URL = "certificateUrl";
    
    // Leaderboard
    public static final String TOP_PERFORMERS = "topPerformers";
    public static final String UPDATE_TIMESTAMP = "updateTimestamp";

    // ========================================
    // REGISTRATION PROCESS TEMPLATE VARIABLES
    // ========================================
    
    // Email Verification
    public static final String VERIFICATION_TOKEN = "verificationToken";
    public static final String VERIFICATION_URL = "verificationUrl";
    public static final String VERIFICATION_DATE = "verificationDate";
    
    // Profile Setup
    public static final String PROFILE_SETUP_URL = "profileSetupUrl";
    public static final String PROFILE_COMPLETION_DATE = "completionDate";
    
    // Onboarding
    public static final String ONBOARDING_DATE = "onboardingDate";
    public static final String NEXT_STEPS = "nextSteps";
    
    // Registration Process
    public static final String REGISTRATION_URL = "registrationUrl";
    public static final String CANCELLATION_REASON = "cancellationReason";
    public static final String CANCELLATION_DATE = "cancellationDate";
    public static final String ACTIVATION_DATE = "activationDate";

    // ========================================
    // POLICY MANAGEMENT TEMPLATE VARIABLES
    // ========================================
    
    public static final String POLICY_NAME = "policyName";
    public static final String POLICY_DESCRIPTION = "policyDescription";
    public static final String COMPLIANCE_STATUS = "complianceStatus";
    public static final String EFFECTIVE_DATE = "effectiveDate";

    // ========================================
    // SYSTEM OPERATIONAL TEMPLATE VARIABLES
    // ========================================
    
    public static final String SYSTEM_STATUS = "systemStatus";
    public static final String MAINTENANCE_WINDOW = "maintenanceWindow";
    public static final String FEATURE_NAME = "featureName";
    public static final String FEATURE_DESCRIPTION = "featureDescription";
    public static final String SECURITY_ALERT_TYPE = "securityAlertType";
    public static final String ALERT_DETAILS = "alertDetails";

    // ========================================
    // GENERAL TEMPLATE VARIABLES
    // ========================================
    
    public static final String TASK_NAME = "taskName";
    public static final String TASK_DUE_DATE = "taskDueDate";
    public static final String TICKET_ID = "ticketId";
    public static final String TICKET_STATUS = "ticketStatus";
    public static final String SUBSCRIPTION_TYPE = "subscriptionType";
    public static final String RENEWAL_DATE = "renewalDate";
}
