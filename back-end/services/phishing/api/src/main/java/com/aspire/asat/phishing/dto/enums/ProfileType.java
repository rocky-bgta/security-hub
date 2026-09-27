package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the type of sender profile.
 * MANAGED - System-provided profiles (read-only for admins)
 * CUSTOM - User-created profiles (editable by admins)
 */
public enum ProfileType {
    MANAGED,    // System-provided
    CUSTOM      // User-created
}
