package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for full landing page preview.
 * Includes complete HTML content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPagePreviewDto {
    
    private String pageId;
    private String name;
    private String description;
    private LandingPageType pageType;
    private LandingPageCategoryDto category;
    private DifficultyDto difficultyLevel;

    private LandingPageStatus status;

    private String thumbnailUrl;
    private String websiteUrl;
    private List<String> tags;
    private int popularity;
    private boolean isPremium;
    private TemplateGenerationType templateGenerationType;
    
    /**
     * Full HTML content of the landing page
     */
    private String htmlContent;
    
    /**
     * Whether this page captures form submissions
     */
    private boolean captureSubmittedData;
    
    /**
     * List of form fields to capture
     */
    private List<String> captureFields;
    
    /**
     * Redirect URL after form submission
     */
    private String redirectUrl;

    private String trackingDomainId;
}
