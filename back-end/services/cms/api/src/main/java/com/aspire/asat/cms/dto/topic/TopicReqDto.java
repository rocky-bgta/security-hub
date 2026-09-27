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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicReqDto {
    @NotBlank(message = "Topic name is required")
    private String topicName;
    @NotEmpty(message = "At least one category is required")
    private List<String> categoryIds;
    @NotEmpty(message = "At least one country is required")
    private List<String> countryIds;
    @NotEmpty(message = "At least one compliance acronym is required")
    private List<String> complianceIds;
    private String contentTypeId;
    private Integer durationMinutes;
    private String description;
    private String thumbnailUrl;
    private List<ProductPackageMapping> productPackages;
    private List<String> chapterIds;
    private Integer totalContentCount;

    // Optional phishing-style metadata. All fields are optional and stored as denormalized
    // id + display-name pairs. CMS does not validate the catalog ids; the caller is the
    // source of truth for the labels.
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

    /**
     * When set, the topic is private to this client ({@code isPrivate=true}).
     * When blank/absent, the topic remains a public catalog topic.
     */
    private String clientId;
}
