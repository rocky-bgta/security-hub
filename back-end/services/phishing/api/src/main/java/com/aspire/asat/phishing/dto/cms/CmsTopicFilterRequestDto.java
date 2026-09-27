package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of CMS {@code TopicFilterRequest} phishing-metadata and tenant fields
 * populated by {@code TopicRecommendationFilterAssembler} for campaign topic recommendation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsTopicFilterRequestDto {

    private List<String> complianceIds;
    private List<String> countryIds;

    private List<String> payloadTypeIds;
    private List<String> difficultyIds;
    private List<String> toneIds;
    private List<String> attackerPersonaIds;
    private List<String> socialEngineeringStrategyIds;
    private List<String> campaignObjectiveIds;
    private List<String> triggerEventIds;
    private List<String> attackTechniqueIds;
    private List<String> emotionalTriggerIds;
    private List<String> urgencyLevelIds;
    private List<String> brandIds;
    private List<String> callToActionIds;
    private List<String> industryIds;
    private List<String> subIndustryIds;

    private List<String> tags;
    private List<String> selectedTopicId;

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    private String status;

    /** Viewer client for CMS privacy scoping on recommend/filter calls. */
    private String clientId;
}
