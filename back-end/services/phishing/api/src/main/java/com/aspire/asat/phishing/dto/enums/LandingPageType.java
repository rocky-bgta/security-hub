package com.aspire.asat.phishing.dto.enums;

/**
 * Enum representing the type of landing page.
 * Based on BRD Use Case 2.1.4
 */
public enum LandingPageType {
    /**
     * Standard phishing landing page
     */
    LANDING_PAGE,
    
    /**
     * 404 Page Not Found error page
     */
    PAGE_NOT_FOUND_404,
    
    /**
     * Custom page with user-defined content
     */
    CUSTOM
}
