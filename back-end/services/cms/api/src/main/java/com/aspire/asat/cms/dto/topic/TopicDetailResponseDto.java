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
public class TopicDetailResponseDto {
    private String id;
    private String topicName;
    private String description;
    private TopicStatus status;
    private List<String> chapterIds;
    private List<ProductPackageDetailsMapping> productPackages;
    private String thumbnailUrl;
    private Integer durationMinutes;
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
    private Instant createdAt;
    private Instant updatedAt;

    private String clientId;
    private Boolean isPrivate;
    
    // Detailed information instead of IDs
    private List<CategoryRespDto> categoryDetails;
    private List<CountryDetailsDto> countryDetails;
    private List<ComplianceRespDto> complianceDetails;
    private ContentTypeRespDto contentTypeDetails;
}
