package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO for importing a website.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSiteRequest {
    
    @NotBlank(message = "URL is required")
    @URL(message = "Please enter a valid URL")
    private String websiteUrl;
    
    /**
     * Whether to include images/assets
     */
    private boolean includeAssets;
    
    /**
     * Maximum depth for crawling (default: 1)
     */
    private int crawlDepth;
}
