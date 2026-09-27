package com.aspire.asat.phishing.dto.enums;

/**
 * Types of phishing email templates.
 * Based on BRD Use Case 2.1.3
 */
public enum EmailType {
    /**
     * General phishing email for broad audience
     */
    STANDARD_PHISH,
    
    /**
     * Targeted phishing email for specific individuals
     */
    SPEAR_PHISH
}
