package com.aspire.asat.phishing.model;

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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB entity for phishing landing pages.
 * Based on BRD Use Case 2.1.4: Admin Manages Phishing Landing Page
 */
@Document(collection = "landing_pages")
@CompoundIndexes({
        @CompoundIndex(name = "category_id_idx", def = "{'category.id': 1}"),
        @CompoundIndex(name = "difficulty_level_id_idx", def = "{'difficultyLevel.id': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPage {
    
    @Id
    private String id;
    
    /**
     * Multi-tenant identifier - links to client
     */
    @NotBlank
    @Indexed
    private String clientId;
    
    /**
     * Landing page name
     */
    @NotBlank
    @Size(min = 3, max = 100)
    @TextIndexed(weight = 3)
    private String name;
    
    /**
     * Brief description of the landing page
     */
    @Size(max = 255)
    private String description;
    
    /**
     * Type of landing page
     */
    @NotNull
    private LandingPageType pageType;
    
    /**
     * Embedded snapshot from {@code landing_page_categories} catalog (optional).
     */
    private LandingPageCategoryDto category;

    /**
     * Embedded snapshot from {@code difficulties} catalog (optional).
     */
    private DifficultyDto difficultyLevel;

    private DepartmentDto department;

    private DepartmentDto targetDepartment;

    private TargetIndustryDto targetIndustry;

    private ConstraintsDataDto constraints;

    private UrgencyLevelDto urgencyLevel;

    private EmotionalTriggerDto emotionalTrigger;

    /**
     * Embedded snapshot from {@code data_capture_types} catalog (optional).
     */
    private DataCaptureTypeDto dataCaptureType;
    
    /**
     * Full HTML content of the landing page.
     * Allowed to be blank while the page is in {@link LandingPageStatus#PROCESSING} state
     * (async AI generation has not yet populated the body).
     */
    private String htmlContent;
    
    /**
     * URL to thumbnail image for preview
     */
    private String thumbnailUrl;
    
    /**
     * Original website URL if imported
     */
    private String websiteUrl;
    
    /**
     * Tags for categorization and search
     */
    @TextIndexed
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
     * Optional FK to {@code domains._id} for custom tracking hostname.
     */
    private String trackingDomainId;
    
    /**
     * Usage count in campaigns
     */
    @Builder.Default
    private int popularity = 0;
    
    /**
     * Whether this page is available to all clients
     */
    @Indexed
    private boolean isGlobal;

    /**
     * Status of the landing page (Draft/Active/Inactive)
     */
    @Indexed
    @Builder.Default
    private LandingPageStatus status = LandingPageStatus.ACTIVE;
    
    /**
     * Premium content flag
     */
    private boolean isPremium;
    
    /**
     * Role of the user who created this page
     */
    private String createdByRole;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * How the landing page was generated: AI vs MANUAL.
     */
    private TemplateGenerationType templateGenerationType;
    
//    @CreatedBy
    private String createdBy;
    
//    @LastModifiedBy
    private String lastModifiedBy;

    /**
     * Identifier of the {@code ai_generation_jobs} document tracking the async AI run for this page.
     * Null for manually-created pages.
     */
    private String aiGenerationJobId;

    /**
     * Persisted error message when async AI generation fails (status = FAILED).
     * Null while PROCESSING or after a successful completion.
     */
    private String aiErrorMessage;
}
