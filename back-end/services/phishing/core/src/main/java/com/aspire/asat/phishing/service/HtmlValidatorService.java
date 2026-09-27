package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.ValidationResult;

/**
 * Service interface for HTML validation.
 * Based on Task-05 Landing Page Creation
 */
public interface HtmlValidatorService {
    
    /**
     * Validate HTML content for syntax and structure
     * @param htmlContent HTML content to validate
     * @return ValidationResult with errors and warnings
     */
    ValidationResult validate(String htmlContent);
    
    /**
     * Validate HTML and check for form fields
     * @param htmlContent HTML content to validate
     * @param validateForms Whether to validate form structure
     * @param securityCheck Whether to check for security issues
     * @return ValidationResult with form fields detected
     */
    ValidationResult validateWithOptions(String htmlContent, boolean validateForms, boolean securityCheck);
    
    /**
     * Extract form field names from HTML
     * @param htmlContent HTML content
     * @return List of form field names
     */
    java.util.List<String> extractFormFields(String htmlContent);
    
    /**
     * Check if HTML contains a login form
     * @param htmlContent HTML content
     * @return true if login form detected
     */
    boolean hasLoginForm(String htmlContent);
}
