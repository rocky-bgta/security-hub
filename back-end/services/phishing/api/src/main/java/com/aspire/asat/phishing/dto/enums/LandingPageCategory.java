package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the category of landing page.
 * Based on BRD Use Case 2.1.4
 */
public enum LandingPageCategory {
    /**
     * Business/Corporate related pages
     */
    BUSINESS,
    
    /**
     * Social media platform lookalikes
     */
    SOCIAL_MEDIA,
    
    /**
     * Email service provider lookalikes
     */
    EMAIL_PROVIDER,
    
    /**
     * Cloud application lookalikes
     */
    CLOUD_APP,
    
    /**
     * Financial institution lookalikes
     */
    FINANCIAL,
    
    /**
     * Government agency lookalikes
     */
    GOVERNMENT,
    
    /**
     * Healthcare organization lookalikes
     */
    HEALTHCARE
}
