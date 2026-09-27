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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating a landing page.
 * Based on Task-04 Landing Page Library
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageUpdateRequest {
    
    @NotBlank(message = "Page name is required")
    @Size(min = 3, max = 100, message = "Page name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
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
     * Optional status update (Draft/Active/Inactive).
     */
    private LandingPageStatus status;
    
    /**
     * HTML content body. When omitted (null), the existing content is left unchanged.
     */
    private String htmlContent;

    private String thumbnailUrl;
    
    private List<String> tags;
    
    private boolean captureSubmittedData;
    
    private List<String> captureFields;
    
    private String redirectUrl;

    /**
     * Optional FK to a verified domain for custom tracking hostname.
     */
    private String trackingDomainId;

    /**
     * When set, replaces bound email/SMS template IDs (empty list clears all). Omit to leave unchanged.
     */
    private List<String> emailTemplateIds;
}
