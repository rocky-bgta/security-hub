package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the type of email activity tracked.
 */
public enum ActivityType {
    EMAIL_SENT,          // Email was sent
    EMAIL_DELIVERED,     // Email was delivered
    EMAIL_BOUNCED,       // Email bounced
    EMAIL_NOT_OPENED_BUT_REPORTED,
    EMAIL_OPENED,        // Email was opened
    LINK_CLICKED,        // Link in email was clicked
    ATTACHMENT_OPENED,   // Attachment was opened
    DATA_SUBMITTED,      // Data was submitted on landing page
    EMAIL_REPORTED,      // Phishing email was reported
    SMS_SENT,            // SMS was sent
    SMS_DELIVERED,       // SMS was delivered
    SMS_FAILED,          // SMS delivery failed
    VOICE_INITIATED,
    VOICE_ANSWERED,
    VOICE_NO_ANSWER,
    VOICE_COMPROMISED,
    VOICE_FAILED,
    VOICE_ENGAGED,
    VOICE_REPORTED
}
