package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for imported website content.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportedSiteDto {
    
    /**
     * Sanitized HTML content
     */
    private String htmlContent;
    
    /**
     * Generated or extracted thumbnail URL
     */
    private String thumbnailUrl;
    
    /**
     * Original website URL
     */
    private String originalUrl;
    
    /**
     * List of extracted asset URLs (images, CSS, JS)
     */
    private List<String> extractedAssets;
    
    /**
     * Whether a login form was detected
     */
    private boolean hasLoginForm;
    
    /**
     * Detected form field names
     */
    private List<String> detectedFormFields;
    
    /**
     * Page title extracted from HTML
     */
    private String pageTitle;
    
    /**
     * Meta description extracted from HTML
     */
    private String metaDescription;

    /**
     * Structured import status outcome.
     */
    private ImportStatus importStatus;

    /**
     * Which import path produced the final html (HTTP_PROFILE/BROWSER_FALLBACK).
     */
    private String importMode;

    /**
     * Final URL after redirects/challenges.
     */
    private String finalResolvedUrl;

    /**
     * Last fetch status code from upstream.
     */
    private Integer fetchStatusCode;

    /**
     * Coarse blocked reason for protected sites.
     */
    private String blockedReason;

    /**
     * Matched challenge/block indicators.
     */
    private List<String> blockedSignals;

    /**
     * Count of assets discovered in HTML.
     */
    private Integer assetsFound;

    /**
     * Count of assets localized successfully.
     */
    private Integer assetsLocalized;

    /**
     * Count of assets that failed localization.
     */
    private Integer assetsFailed;

    /**
     * Non-fatal import warnings.
     */
    private List<String> warnings;
}
