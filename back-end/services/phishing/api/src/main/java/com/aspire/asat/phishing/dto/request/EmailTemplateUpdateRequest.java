package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.dto.response.CampaignObjectiveDto;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.DepartmentDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.TargetIndustryDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.dto.response.SocialEngineeringStrategyDto;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
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
 * Request DTO for updating an existing email template.
 * Based on BRD Use Case 2.1.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateUpdateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name cannot exceed 100 characters")
    private String templateName;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private String emailSubject;
    
    private String emailBody;

    private String smsBody;

    private TemplateType templateType;
    
    private String emailBodyText;
    
    /**
     * Optional template status update.
     */
    private EmailTemplateStatus status;

    private PayloadTypeDto payloadType;

    @NotNull(message = "Difficulty level is required")
    private DifficultyDto difficultyLevel;

    private ToneDto tone;

    private DepartmentDto department;

    private TargetIndustryDto targetIndustry;

    private ConstraintsDataDto constraints;

    private ExpectedUserActionDto expectedUserAction;

    private TriggerEventDto triggerEvent;

    private UrgencyLevelDto urgencyLevel;

    private SocialEngineeringStrategyDto socialEngineeringStrategy;

    private EmotionalTriggerDto emotionalTrigger;

    private CampaignObjectiveDto campaignObjective;

    private AttackerPersonaDto attackerPersona;

    private AttackTechniqueDto attackTechnique;

    private BrandDto brand;

    private CallToActionDto callToAction;
    
    private String serviceLocation;

    private String thumbnailUrl;

    /**
     * When set, replaces stored attachment URLs (empty list clears). Omit to leave unchanged.
     */
    @Size(max = 5, message = "Maximum 5 attachments allowed")
    private List<String> attachments;
    
    private List<String> tags;
    
    private List<String> employeeDataRequired;
    
    private String language;

    /**
     * When set, replaces bound landing page IDs (empty list clears all). Omit to leave unchanged.
     */
    private List<String> landingPageIds;
}
