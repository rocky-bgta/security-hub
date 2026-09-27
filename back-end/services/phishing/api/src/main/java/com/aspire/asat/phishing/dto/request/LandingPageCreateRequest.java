package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DepartmentDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new landing page.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Page type is required")
    private LandingPageType pageType;
    
    private LandingPageCategoryDto category;

    private DifficultyDto difficultyLevel;

    private DepartmentDto department;

    private DepartmentDto targetDepartment;

    private TargetIndustryDto targetIndustry;

    private ConstraintsDataDto constraints;

    private UrgencyLevelDto urgencyLevel;

    private EmotionalTriggerDto emotionalTrigger;

    private DataCaptureTypeDto dataCaptureType;

    /**
     * Landing page status for availability.
     */
    @Builder.Default
    private LandingPageStatus status = LandingPageStatus.ACTIVE;
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;

    /**
     * Indicates how this landing page was generated.
     */
    @Builder.Default
    private TemplateGenerationType templateGenerationType = TemplateGenerationType.MANUAL;
    
    /**
     * URL to thumbnail image
     */
    private String thumbnailUrl;
    
    /**
     * Original website URL (for imported pages)
     */
    private String websiteUrl;
    
    /**
     * Tags for categorization
     */
    private List<String> tags;
    
    /**
     * Whether to capture form submissions
     */
    private boolean captureSubmittedData;
    
    /**
     * List of form fields to capture
     */
    private List<String> captureFields;
    
    /**
     * URL to redirect user after form submission
     */
    private String redirectUrl;

    /**
     * Optional FK to a verified domain for custom tracking hostname.
     */
    private String trackingDomainId;

    /**
     * Optional email/SMS template IDs to bind to this landing page.
     */
    private List<String> emailTemplateIds;
}
