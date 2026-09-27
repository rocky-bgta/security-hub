package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;

/**
 * Service interface for HTML content sanitization.
 * Based on BR-04: Email body HTML must be sanitized to remove script tags and event handlers
 */
public interface HtmlSanitizerService {
    
    /**
     * Sanitize HTML content by removing dangerous elements and attributes
     * @param htmlContent Raw HTML content
     * @return Sanitized HTML string
     */
    String sanitize(String htmlContent);
    
    /**
     * Sanitize HTML content with detailed result
     * @param htmlContent Raw HTML content
     * @return SanitizeHtmlResult with sanitized content and details
     */
    SanitizeHtmlResult sanitizeWithDetails(String htmlContent);
    
    /**
     * Validate HTML syntax
     * @param htmlContent HTML content to validate
     * @return true if valid HTML syntax
     */
    boolean isValidHtml(String htmlContent);
    
    /**
     * Check if HTML contains dangerous elements
     * @param htmlContent HTML content to check
     * @return true if contains dangerous elements
     */
    boolean containsDangerousElements(String htmlContent);
}
