package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for landing page list/details.
 * Based on Task-04 Landing Page Library
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageDto {
    
    private String pageId;
    private String name;
    private String description;
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

    private String landingPageBodyPreview;

    private LandingPageStatus status;
    private String thumbnailUrl;
    private String websiteUrl;
    private List<String> tags;
    private boolean captureSubmittedData;
    private List<String> captureFields;
    private String redirectUrl;
    private String trackingDomainId;
    /** Hostname resolved from {@code domains.domain} via {@link #trackingDomainId}. */
    private String trackingDomain;
    private int popularity;
    private boolean isGlobal;
    private boolean isPremium;
    
    /**
     * Whether current user can edit this page
     */
    private boolean canEdit;
    
    /**
     * Whether current user can delete this page
     */
    private boolean canDelete;
    
    private String createdByRole;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * IDs of email/SMS templates bound to this landing page (computed from template bindings).
     */
    private List<String> emailTemplateIds;
}
