package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.dto.response.DepartmentDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for AI-powered email template generation.
 * Based on BRD Use Case 2.1.3.2: Admin uses AI to generate phishing email template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITemplateGenerateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    private String templateName;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Payload type is required")
    private PayloadTypeDto payloadType;
    
    @NotBlank(message = "Email subject is required")
    @Size(max = 200, message = "Email subject cannot exceed 200 characters")
    private String emailSubject;
    
    // --- AI Generation Parameters ---
    
    /**
     * Target industry for the phishing simulation.
     */
    private TargetIndustryDto targetIndustry;

    /**
     * Target department for the phishing simulation.
     * Mutually exclusive with targetIndustry for prompt generation context.
     */
    private DepartmentDto department;
    
    @NotNull(message = "Attacker persona is required")
    private AttackerPersonaDto attackerPersona;

    @NotNull(message = "Attack technique is required")
    private AttackTechniqueDto attackTechnique;

    @NotNull(message = "Trigger event is required")
    private TriggerEventDto triggerEvent;

    /**
     * Expected user action in the campaign.
     */
    @NotNull(message = "Expected user action is required")
    private ExpectedUserActionDto expectedUserAction;
    
    @NotNull(message = "Social engineering strategy is required")
    private SocialEngineeringStrategyDto socialEngineeringStrategy;
    
    @NotNull(message = "Campaign objective is required")
    private CampaignObjectiveDto campaignObjective;
    
    /**
     * Content transcribed from voice input (optional)
     */
    private String voiceInputContent;

    /**
     * Extra context/instructions for AI generation.
     */
    @Size(max = 10000, message = "Additional context cannot exceed 10000 characters")
    private String additionalContext;
    
    /**
     * Language of the voice/text input
     */
    @Builder.Default
    private String inputLanguage = "en";

    /**
     * Generation mode (e.g. CLONE_STYLE, BRAND_BASED, FULLY_AI). Optional for email templates.
     */
    private String generationMode;

    // --- Standard template fields ---
    
    @NotNull(message = "Difficulty level is required")
    private DifficultyDto difficultyLevel;
    
    private String serviceLocation;

    private String thumbnailUrl;
    
    private List<String> tags;
    
    private List<String> employeeDataRequired;

    // --- Provider selection / credentials ---

    /**
     * AI provider to use for generation.
     */
    @NotNull(message = "providerType is required")
    @Builder.Default
    private AiProviderType providerType = AiProviderType.OPENAI;

    /**
     * Provider-specific model (e.g., gpt-4.1-mini).
     * If null, the adapter will use its default allow-listed model.
     */
    private String model;

    /**
     * Optional generation options (tone, language, brand, length, CTA, urgency, emotional trigger, etc.)
     */
    private AiGenerationOptionsRequest generationOptions;

    /**
     * Optional landing page IDs to bind to this template.
     */
    private List<String> landingPageIds;
}
