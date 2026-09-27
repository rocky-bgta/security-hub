package com.aspire.asat.phishing.dto.enums;

import java.util.List;

/**
 * Employee data fields that can be used in email templates for personalization.
 * Based on BRD Use Case 2.1.3
 */
public enum EmployeeDataField {
    /**
     * For tracking email delivery, engagement (clicks, opens)
     */
    EMAIL_ADDRESS,
    
    /**
     * For personalized tracking
     */
    FIRST_NAME,
    
    /**
     * To track specific users within the organization
     */
    LAST_NAME,
    
    /**
     * For tracking phishing attempts targeted at specific organizations
     */
    ORGANIZATION,
    
    /**
     * For tracking voice-based phishing or phone fraud campaigns
     */
    PHONE_NUMBER,
    
    /**
     * To track geographic trends or customize content
     */
    LOCATION,
    
    /**
     * For department-level targeting and reporting
     */
    DEPARTMENT;
    
    /**
     * Predefined tags for email templates (from BRD)
     */
    public static final List<String> PREDEFINED_TAGS = List.of(
        "Security", 
        "Awareness", 
        "Phishing", 
        "Training", 
        "New Employees", 
        "GDPR", 
        "Compliance"
    );
}
