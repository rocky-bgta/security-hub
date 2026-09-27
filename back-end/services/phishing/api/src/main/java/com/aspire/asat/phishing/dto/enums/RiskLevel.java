package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing user risk levels based on phishing behavior.
 */
public enum RiskLevel {
    LOW,       // No clicks or single click
    MEDIUM,    // Clicked once, no data submitted
    HIGH,      // Clicked 2+ times or submitted data once
    CRITICAL   // Multiple data submissions or repeat offender
}
