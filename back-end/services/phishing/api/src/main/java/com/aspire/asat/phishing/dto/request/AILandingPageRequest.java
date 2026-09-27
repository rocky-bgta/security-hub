package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
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
 * Request DTO for AI-powered landing page generation.
 * Based on Task-05 Landing Page Creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AILandingPageRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
    private String name;
    
    @Size(max = 255)
    private String description;
    
    @NotNull(message = "Page type is required")
    private LandingPageType pageType;

    private LandingPageCategoryDto category;

    private DifficultyDto difficultyLevel;
    
    // --- AI Generation Parameters ---
    
    /**
     * Generation mode: CLONE_STYLE, BRAND_BASED, FULLY_AI
     */
    private String generationMode;
    
    /**
     * Layout style: MINIMAL, CORPORATE, MOBILE_FIRST, DARK_MODE
     */
    private String layoutStyle;

    private DepartmentDto department;

    private DepartmentDto targetDepartment;

    private TargetIndustryDto targetIndustry;

    private UrgencyLevelDto urgencyLevel;

    private EmotionalTriggerDto emotionalTrigger;

    private DataCaptureTypeDto dataCaptureType;

    @Size(max = 2000)
    private String voiceInput;

    @Size(max = 10000)
    private String additionalContext;
    
    /**
     * Language of voice input (supports 10 languages)
     */
    private String inputLanguage;
    
    // --- Page Configuration ---

    /**
     * AI provider to use for generation.
     */
    @NotNull(message = "providerType is required")
    @Builder.Default
    private AiProviderType providerType = AiProviderType.OPENAI;

    /**
     * Landing page status for availability.
     */
    @Builder.Default
    private LandingPageStatus status = LandingPageStatus.ACTIVE;

    /**
     * Indicates how this landing page was generated.
     */
    @Builder.Default
    private TemplateGenerationType templateGenerationType = TemplateGenerationType.AI;

    /**
     * Provider-specific model (e.g., gpt-4.1-mini).
     * If null, adapter will use a default allow-listed model.
     */
    private String model;

    /**
     * URL to thumbnail image
     */
    private String thumbnailUrl;

    /**
     * Tags for categorization
     */
    private List<String> tags;

    /**
     * Optional provider-agnostic generation options.
     */
    private AiGenerationOptionsRequest generationOptions;

    /**
     * Optional email/SMS template IDs to bind to this landing page.
     */
    private List<String> emailTemplateIds;
}
