package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing breach severity levels.
 */
public enum BreachSeverity {
    LOW,      // Only email addresses compromised
    MEDIUM,   // Email + personal info compromised
    HIGH      // Passwords, financial data, or PII compromised
}
