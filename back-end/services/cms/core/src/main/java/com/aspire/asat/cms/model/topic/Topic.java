package com.aspire.asat.cms.model.topic;

import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.ProductPackageMapping;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
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
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "topic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@CompoundIndexes({
        @CompoundIndex(name = "filter_index", def = "{'categoryIds': 1, 'countryIds': 1, 'complianceIds': 1, 'durationMinutes': 1}"),
        @CompoundIndex(name = "status_date_index", def = "{'status': 1, 'createdAt': -1}")
})

public class Topic {

    private String id;
    private String topicName;
    private String description;
    private TopicStatus status;
    private List<String> chapterIds;
    private List<ProductPackageMapping> productPackageMappings;
    private String thumbnailUrl;
    @Indexed
    private Integer durationMinutes;
    private Integer totalContentCount;

    @Indexed
    private String contentTypeId;
    @Indexed
    private List<String> categoryIds; // Multiple categories can be selected
    @Indexed
    private List<String> countryIds; // Multiple countries can be selected
    @Indexed
    private List<String> complianceIds; // PCI, HIPAA, FedRAMP, GDPR

    // Optional phishing-style metadata. Stored as embedded id+name pairs (denormalized labels
    // supplied by the caller). Lists are intentionally NOT indexed by default: they are usually
    // narrowed by the mandatory package/product criterion first. Add indexes later under
    // profiling if a specific dimension becomes a hot filter.
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

    // Free-form labels; multikey index makes tag-based filtering cheap.
    @Indexed
    private List<String> tags;

    private String createdBy;
    @Indexed
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Owning client when the topic is private. Null means a public catalog topic.
     */
    @Indexed
    private String clientId;

    /**
     * When true, topic is visible only to {@link #clientId}. Null/false = public.
     */
    private Boolean isPrivate;

    public static TopicRespDto toTopicRespDto(Topic topic) {
        return TopicRespDto.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .categoryIds(topic.getCategoryIds())
                .countryIds(topic.getCountryIds())
                .complianceIds(topic.getComplianceIds())
                .contentTypeId(topic.getContentTypeId())
                .durationMinutes(topic.getDurationMinutes() != null ? topic.getDurationMinutes() : null)
                .description(topic.getDescription())
                .thumbnailUrl(topic.getThumbnailUrl())
                .productPackages(topic.getProductPackageMappings())
                .chapterIds(topic.getChapterIds())
                .totalContentCount(topic.getTotalContentCount())
                .payloadType(topic.getPayloadType())
                .difficulty(topic.getDifficulty())
                .tone(topic.getTone())
                .attackerPersona(topic.getAttackerPersona())
                .socialEngineeringStrategy(topic.getSocialEngineeringStrategy())
                .campaignObjective(topic.getCampaignObjective())
                .triggerEvent(topic.getTriggerEvent())
                .attackTechnique(topic.getAttackTechnique())
                .emotionalTrigger(topic.getEmotionalTrigger())
                .urgencyLevel(topic.getUrgencyLevel())
                .brand(topic.getBrand())
                .callToAction(topic.getCallToAction())
                .industry(topic.getIndustry())
                .subIndustry(topic.getSubIndustry())
                .tags(topic.getTags())
                .createdBy(topic.getCreatedBy())
                .status(topic.getStatus())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .clientId(topic.getClientId())
                .isPrivate(topic.getIsPrivate())
                .build();
    }

    public static Topic toTopic(String id, TopicReqDto dto, Instant createdAt, Instant updatedAt) {
        boolean privateTopic = dto.getClientId() != null && !dto.getClientId().isBlank();
        String resolvedClientId = privateTopic ? dto.getClientId().trim() : null;
        // Public catalog creates stay DISABLED (CMS enablement rules). Private/micro-content
        // topics may honor an explicit status (e.g. ENABLED from deepfake).
        TopicStatus status = (privateTopic && dto.getStatus() != null)
                ? dto.getStatus()
                : TopicStatusConfig.DEFAULT_TOPIC_STATUS;
        return Topic.builder()
                .id(id)
                .topicName(dto.getTopicName())
                .categoryIds(dto.getCategoryIds())
                .countryIds(dto.getCountryIds())
                .complianceIds(dto.getComplianceIds())
                .contentTypeId(dto.getContentTypeId())
                .durationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : null)
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .productPackageMappings(dto.getProductPackages())
                .chapterIds(dto.getChapterIds())
                .totalContentCount(dto.getTotalContentCount())
                .payloadType(dto.getPayloadType())
                .difficulty(dto.getDifficulty())
                .tone(dto.getTone())
                .attackerPersona(dto.getAttackerPersona())
                .socialEngineeringStrategy(dto.getSocialEngineeringStrategy())
                .campaignObjective(dto.getCampaignObjective())
                .triggerEvent(dto.getTriggerEvent())
                .attackTechnique(dto.getAttackTechnique())
                .emotionalTrigger(dto.getEmotionalTrigger())
                .urgencyLevel(dto.getUrgencyLevel())
                .brand(dto.getBrand())
                .callToAction(dto.getCallToAction())
                .industry(dto.getIndustry())
                .subIndustry(dto.getSubIndustry())
                .tags(dto.getTags())
                .createdBy(dto.getCreatedBy())
                .status(status)
                .clientId(resolvedClientId)
                .isPrivate(privateTopic)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

}
