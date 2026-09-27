package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for HTML content sanitization.
 * Based on BR-04: Email body HTML must be sanitized to remove script tags and event handlers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanitizeHtmlRequest {
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;
    
    /**
     * Whether to return sanitization details
     */
    @Builder.Default
    private boolean returnDetails = false;
}
