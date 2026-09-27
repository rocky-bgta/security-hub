package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.EmailType;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for email template list and details.
 * Contains truncated email body preview for list views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    
    private String templateId;
    
    private String templateName;
    
    private String description;

    private String emailSubject;
    
    /**
     * Truncated plain-text preview empty string for list views; null when {@link #emailBody} is set.
     */
    private String emailBodyPreview;
    
    private EmailType emailType;

    private TemplateType templateType;

    private String smsBody;
    
    private EmailTemplateStatus status;

    private PayloadTypeDto payloadType;
    
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
    
    private List<String> tags;
    
    private List<String> employeeDataRequired;
    
    private String thumbnailUrl;

    private List<String> attachments;
    
    private String language;
    
    private int popularity;
    
    private boolean isGlobal;
    
    private boolean isPremium;

    private TemplateGenerationType templateGenerationType;
    
    /**
     * Whether current user can edit this template
     * (Based on permissions - Admin cannot edit Super Admin templates)
     */
    private boolean canEdit;
    
    /**
     * Whether current user can delete this template
     * (Based on permissions - Admin cannot delete Super Admin templates)
     */
    private boolean canDelete;
    
    private String createdByRole;
    
    private Instant createdAt;
    
    private Instant updatedAt;

    /**
     * IDs of landing pages bound to this template.
     */
    private List<String> landingPageIds;
}
