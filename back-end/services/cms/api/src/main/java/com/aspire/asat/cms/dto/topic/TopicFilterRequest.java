package com.aspire.asat.cms.dto.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for filtering topics.")
public class TopicFilterRequest {
    // Text search
    @Schema(description = "Text search for filtering topics", example = "cybersecurity")
    private String searchText;

    // Multi-select filters - empty or null means show all
    @Schema(description = "List of category IDs for filtering topics", example = "[\"Password Security-001\", \"Social Engineering-001\"]")
    private List<String> categoryIds;
    @Schema(description = "List of compliance IDs for filtering topics", example = "[\"PCI-001\", \"HIPAA-001\"]")
    private List<String> complianceIds;
    @Schema(description = "List of country IDs for filtering topics", example = "[\"US-001\", \"BANGLADESH-001\"]")
    private List<String> countryIds;
    @Schema(description = "List of content type IDs for filtering topics", example = "[\"video-001\", \"animation-001\"]")
    private List<String> contentTypeId;
    @Schema(description = "List of duration ranges to filter by. Empty or null means show all.", example = "[\"ONE_TO_THREE\", \"FIVE_TO_TEN\"]")
    private List<DurationRange> durationRanges;

    // Optional phishing-style metadata filters - topics matching ANY id in each list (OR within dimension).
    // All dimensions combine via AND.
    @Schema(description = "Filter by payload type ids", example = "[\"payload-001\", \"payload-002\"]")
    private List<String> payloadTypeIds;
    @Schema(description = "Filter by difficulty ids", example = "[\"difficulty-001\", \"difficulty-002\"]")
    private List<String> difficultyIds;
    @Schema(description = "Filter by tone ids", example = "[\"tone-001\"]")
    private List<String> toneIds;
    @Schema(description = "Filter by attacker persona ids", example = "[\"attackerPersona-001\"]")
    private List<String> attackerPersonaIds;
    @Schema(description = "Filter by social engineering strategy ids", example = "[\"socialEngineeringStrategy-001\"]")
    private List<String> socialEngineeringStrategyIds;
    @Schema(description = "Filter by campaign objective ids", example = "[\"campaignObjective-001\"]")
    private List<String> campaignObjectiveIds;
    @Schema(description = "Filter by trigger event ids", example = "[\"triggerEvent-001\"]")
    private List<String> triggerEventIds;
    @Schema(description = "Filter by attack technique ids", example = "[\"attackTechnique-001\"]")
    private List<String> attackTechniqueIds;
    @Schema(description = "Filter by emotional trigger ids", example = "[\"emotionalTrigger-001\"]")
    private List<String> emotionalTriggerIds;
    @Schema(description = "Filter by urgency level ids", example = "[\"urgencyLevel-001\"]")
    private List<String> urgencyLevelIds;
    @Schema(description = "Filter by brand ids", example = "[\"brand-001\"]")
    private List<String> brandIds;
    @Schema(description = "Filter by call-to-action ids", example = "[\"callToAction-001\"]")
    private List<String> callToActionIds;
    @Schema(description = "Filter by industry ids", example = "[\"industry-001\"]")
    private List<String> industryIds;
    @Schema(description = "Filter by sub-industry ids", example = "[\"subIndustry-001\"]")
    private List<String> subIndustryIds;

    @Schema(description = "Filter by tags. Topics matching ANY of the supplied tags are returned.", example = "[\"finance\", \"banking\"]")
    private List<String> tags;

    @Schema(description = "Selected topic IDs to pin at the top of the result list. When empty or null, normal pagination is used.",
            example = "[\"topic-001\", \"topic-002\"]")
    private List<String> selectedTopicId;

    // Pagination
    @Schema(description = "The page number for pagination", example = "1")
    private Integer page;
    @Schema(description = "The page size for pagination", example = "20")
    private Integer size;
    @Schema(description = "Field to sort the results by (e.g., 'topicName', 'createdAt').", example = "topicName")
    private String sortBy;
    @Schema(description = "The sort direction (ASC or DESC)", example = "ASC")
    private String sortDirection;

    @Schema(description = "The status filter for the topic. Defaults to ENABLED", example = "ENABLED")
    private TopicStatus status;

    @Schema(description = "Viewer client id for privacy scoping. When set, returns public topics "
            + "plus private topics owned by this client. When absent, the service may resolve it "
            + "from the current user context; with no client context, all topics are visible "
            + "(admin/internal).", example = "client-admin-001")
    private String clientId;
}
