package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.ImportSiteRequest;
import com.aspire.asat.phishing.dto.response.ImportedSiteDto;

/**
 * Service interface for website importing.
 * Based on Task-05 Landing Page Creation
 */
public interface WebsiteImportService {
    
    /**
     * Import a website from URL
     * @param request Import request containing URL and options
     * @return ImportedSiteDto with sanitized HTML and metadata
     */
    ImportedSiteDto importWebsite(ImportSiteRequest request);
    
    /**
     * Validate if a URL is accessible
     * @param url URL to validate
     * @return true if accessible
     */
    boolean isUrlAccessible(String url);
    
    /**
     * Extract page title from HTML
     * @param htmlContent HTML content
     * @return Page title or null
     */
    String extractPageTitle(String htmlContent);
    
    /**
     * Extract meta description from HTML
     * @param htmlContent HTML content
     * @return Meta description or null
     */
    String extractMetaDescription(String htmlContent);
}
