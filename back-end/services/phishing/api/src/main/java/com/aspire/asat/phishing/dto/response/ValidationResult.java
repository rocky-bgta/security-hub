package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for HTML validation results.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    
    /**
     * Whether the HTML is valid
     */
    private boolean isValid;
    
    /**
     * List of validation errors
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    /**
     * List of validation warnings
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    /**
     * Detected form fields in the HTML
     */
    @Builder.Default
    private List<String> detectedFormFields = new ArrayList<>();
    
    /**
     * Whether the HTML contains a login form
     */
    private boolean hasLoginForm;
    
    /**
     * Create a successful validation result
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .isValid(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }
    
    /**
     * Create a failed validation result with errors
     */
    public static ValidationResult failure(List<String> errors) {
        return ValidationResult.builder()
                .isValid(false)
                .errors(errors)
                .warnings(new ArrayList<>())
                .build();
    }
}
