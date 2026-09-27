package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the type of phishing campaign.
 */
public enum CampaignType {
    SIMULATED_PHISHING,       // Email phishing simulation only
    PHISHING_WITH_TRAINING,   // Email phishing with training module
    SMISHING_SIMULATION,      // SMS smishing simulation only
    SMISHING_WITH_TRAINING,   // SMS smishing with training module
    VISHING_SIMULATION,       // Voice vishing simulation only
    VISHING_WITH_TRAINING     // Voice vishing with training module
}
