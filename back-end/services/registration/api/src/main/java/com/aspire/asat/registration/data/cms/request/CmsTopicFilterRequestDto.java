package com.aspire.asat.registration.data.cms.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Registration mirror of CMS {@code TopicFilterRequest} for topic fetching.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic filter criteria forwarded to CMS")
public class CmsTopicFilterRequestDto {

    private String searchText;
    private List<String> categoryIds;
    private List<String> complianceIds;
    private List<String> countryIds;
    private List<String> contentTypeId;
    private List<String> durationRanges;

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

    private String status;
}
