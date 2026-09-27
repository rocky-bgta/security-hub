package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for HTML validation.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateHtmlRequest {
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;
    
    /**
     * Whether to validate form structure
     */
    private boolean validateForms;
    
    /**
     * Whether to check for security issues
     */
    private boolean securityCheck;
}
