package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the status of a campaign recipient.
 */
public enum RecipientStatus {
    PENDING,         // Email not yet sent
    SENT,            // Email sent
    DELIVERED,       // Email delivered
    BOUNCED,         // Email bounced
    OPENED,          // Email opened
    CLICKED,         // Link clicked
    DATA_SUBMITTED,  // Data submitted on landing page
    REPORTED,        // Phishing reported by user
    // Voice/vishing statuses (appended to preserve email/SMS ordinal order)
    CALL_QUEUED,
    CALL_RINGING,
    ANSWERED,
    NO_ANSWER,
    COMPROMISED,
    CALL_FAILED,
    VOICE_ENGAGED
}
