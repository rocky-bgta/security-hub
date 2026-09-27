package com.aspire.asat.cms.dto.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.attribute.AttackTechnique;
import com.aspire.asat.cms.dto.topic.attribute.AttackerPersona;
import com.aspire.asat.cms.dto.topic.attribute.Brand;
import com.aspire.asat.cms.dto.topic.attribute.CallToAction;
import com.aspire.asat.cms.dto.topic.attribute.CampaignObjective;
import com.aspire.asat.cms.dto.topic.attribute.Difficulty;
import com.aspire.asat.cms.dto.topic.attribute.EmotionalTrigger;
import com.aspire.asat.cms.dto.topic.attribute.Industry;
import com.aspire.asat.cms.dto.topic.attribute.PayloadType;
import com.aspire.asat.cms.dto.topic.attribute.SocialEngineeringStrategy;
import com.aspire.asat.cms.dto.topic.attribute.SubIndustry;
import com.aspire.asat.cms.dto.topic.attribute.Tone;
import com.aspire.asat.cms.dto.topic.attribute.TriggerEvent;
import com.aspire.asat.cms.dto.topic.attribute.UrgencyLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRespDto {
    private String id;
    private String topicName;
    private List<String> categoryIds;
    private List<String> countryIds;
    private List<String> complianceIds;
    private String contentTypeId;
    private Integer durationMinutes;
    private String description;
    private String thumbnailUrl;
    private String thumbnailPreviewUrl;
    private List<ProductPackageMapping> productPackages;
    private List<String> chapterIds;
    private Integer totalContentCount;

    // Optional phishing-style metadata, surfaced as id + display-name pairs.
    private List<PayloadType> payloadType;
    private List<Difficulty> difficulty;
    private List<Tone> tone;
    private List<AttackerPersona> attackerPersona;
    private List<SocialEngineeringStrategy> socialEngineeringStrategy;
    private List<CampaignObjective> campaignObjective;
    private List<TriggerEvent> triggerEvent;
    private List<AttackTechnique> attackTechnique;
    private List<EmotionalTrigger> emotionalTrigger;
    private List<UrgencyLevel> urgencyLevel;
    private List<Brand> brand;
    private List<CallToAction> callToAction;
    private List<Industry> industry;
    private List<SubIndustry> subIndustry;
    private List<String> tags;

    private String createdBy;
    private TopicStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    /** Owning client when the topic is private; null for public catalog topics. */
    private String clientId;
    /** True when the topic is visible only to {@link #clientId}. */
    private Boolean isPrivate;

    /**
     * Soft-match score for recommendation APIs only. Omitted from filter API responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer matchScore;
    
    // Detailed information fields (populated when needed)
    private List<CategoryRespDto> categoryDetails;
    private ContentTypeRespDto contentTypeDetails;
}
