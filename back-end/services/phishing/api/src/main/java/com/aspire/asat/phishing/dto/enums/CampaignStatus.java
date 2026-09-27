package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the lifecycle status of a campaign.
 */
public enum CampaignStatus {
    DRAFT,       // Campaign being created/edited
    SCHEDULED,   // Campaign scheduled for future execution
    RUNNING,     // Campaign currently executing
    PAUSED,      // Campaign temporarily paused
    COMPLETED,   // Campaign finished successfully
    EXPIRED,     // Campaign reached expiration time
    CANCELLED    // Campaign cancelled (cannot be resumed)
}
