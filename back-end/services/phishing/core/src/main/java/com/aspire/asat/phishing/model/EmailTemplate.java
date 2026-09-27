package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.EmailType;
import com.aspire.asat.phishing.dto.enums.TemplateGenerationType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.response.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Email Template entity for phishing campaigns.
 * Based on BRD Use Case 2.1.3: Admin Creates and Manages Phishing Email Templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_templates")
public class EmailTemplate {

    @Id
    private String id;

    /**
     * Multi-tenant identifier - obtained from UserCurrentContextService
     */
    @Indexed
    @NotBlank
    private String clientId;

    /**
     * Template name for identification
     */
    @TextIndexed
    @NotBlank
    @Size(max = 100)
    private String templateName;

    /**
     * Optional description of the template
     */
    @Size(max = 500)
    private String description;

    /**
     * Organization name represented by this template.
     */
    @Size(max = 200)
    private String organizationName;

    /**
     * Organization domain represented by this template.
     */
    @Size(max = 255)
    private String organizationDomain;

    /**
     * Email subject line.
     * Allowed to be blank while the template is in {@link EmailTemplateStatus#PROCESSING} state
     * (async AI generation has not yet populated the body).
     */
    @TextIndexed
    @Size(max = 200)
    private String emailSubject;

    /**
     * Whether template is available for use.
     */
    @Indexed
    @Builder.Default
    private EmailTemplateStatus status = EmailTemplateStatus.ACTIVE;

    /**
     * HTML content of the email body.
     * Allowed to be blank while the template is in {@link EmailTemplateStatus#PROCESSING} state
     * (async AI generation has not yet populated the body).
     */
    private String emailBody;

    /**
     * Plain text version of the email body
     */
    private String emailBodyText;

    /**
     * Template channel type: EMAIL or SMS.
     */
    @Builder.Default
    private TemplateType templateType = TemplateType.EMAIL;

    /**
     * Plain text SMS body (max 160 chars after placeholder replacement). Used when templateType is SMS.
     */
    @Size(max = 160)
    private String smsBody;

    /**
     * Type of phishing email: STANDARD_PHISH or SPEAR_PHISH
     */
    @NotNull
    private EmailType emailType;

    /**
     * Payload/attack vector type
     */
    @NotNull
    private PayloadTypeDto payloadType;

    /**
     * Tone context used during AI template generation.
     */
    private ToneDto tone;


    /**
     * Difficulty level: BEGINNER, INTERMEDIATE, ADVANCED, SPEAR_PHISHING
     */
    @NotNull
    private DifficultyDto difficultyLevel;

    /**
     * Target department context used during AI template generation.
     */
    @Indexed
    private DepartmentDto department;

    /**
     * Target industry context used during AI template generation.
     */
    private TargetIndustryDto targetIndustry;

    /**
     * Generation constraints applied for this template.
     */
    private ConstraintsDataDto constraints;

    /**
     * Expected recipient action in the phishing scenario.
     */
    private ExpectedUserActionDto expectedUserAction;

    /**
     * Trigger event context used for AI-generated phishing narrative.
     */
    private TriggerEventDto triggerEvent;

    /**
     * Urgency Level context used for AI-generated phishing narrative.
     */
    private UrgencyLevelDto urgencyLevel;

    /**
     * Social engineering strategy applied for this template.
     */
    private SocialEngineeringStrategyDto socialEngineeringStrategy;

    /**
     * Emotional Trigger applied for this template.
     */
    private EmotionalTriggerDto emotionalTrigger;

    /**
     * Campaign Objective applied for this template.
     */
    private CampaignObjectiveDto campaignObjective;

    /**
     * Attack persona context used in the generated template.
     */
    private AttackerPersonaDto attackerPersona;

    /**
     * Attack technique context used in the generated template.
     */
    private AttackTechniqueDto attackTechnique;

    /**
     * Brand.
     */
    private BrandDto brand;

    /**
     * callToAction.
     */
    private CallToActionDto callToAction;

    /**
     * Target geographic location/region
     */
    @Indexed
    private String serviceLocation;

    /**
     * Template tags for categorization
     * From predefined list: Security, Awareness, Phishing, Training, New Employees, GDPR, Compliance
     */
    @TextIndexed
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Employee data fields required for personalization
     */
    @Builder.Default
    private List<String> employeeDataRequired = new ArrayList<>();

    /**
     * Thumbnail/preview image URL
     */
    private String thumbnailUrl;

    /**
     * URLs of files uploaded to object storage by the client (e.g. S3).
     */
    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    /**
     * Template language (en, es, fr, etc.)
     */
    @Indexed
    private String language;

    /**
     * Usage count (how many times used in campaigns)
     */
    @Builder.Default
    private int popularity = 0;

    /**
     * Whether template is available to all clients (Super Admin only)
     */
    @Builder.Default
    private boolean isGlobal = false;

    /**
     * Whether template requires premium subscription
     */
    @Builder.Default
    private boolean isPremium = false;

    /**
     * Role of the user who created this template: SUPER_ADMIN, ADMIN
     */
    private String createdByRole;

    // Audit fields
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    //    @CreatedBy
    private String createdBy;

    //    @LastModifiedBy
    private String lastModifiedBy;

    /**
     * How the template was created: AI vs MANUAL.
     */
    private TemplateGenerationType templateGenerationType;

    /**
     * Identifier of the {@code ai_generation_jobs} document tracking the async AI run for this template.
     * Null for manually-created templates.
     */
    private String aiGenerationJobId;

    /**
     * Persisted error message when async AI generation fails (status = FAILED).
     * Null while PROCESSING or after a successful completion.
     */
    private String aiErrorMessage;

    /**
     * IDs of landing pages bound to this template (email or SMS).
     * First entry is the default when auto-selecting in campaign creation.
     */
    @Builder.Default
    private List<String> landingPageIds = new ArrayList<>();
}
