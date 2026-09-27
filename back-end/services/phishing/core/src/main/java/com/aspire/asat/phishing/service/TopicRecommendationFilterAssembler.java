package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.cms.ClientAdminInfoDto;
import com.aspire.asat.phishing.dto.cms.CmsTopicFilterRequestDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.utils.CatalogDtoUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds {@link CmsTopicFilterRequestDto} for campaign topic recommendation using CMS filter semantics:
 * one catalog id per dimension (primary then fallback), omitted when unresolved.
 */
@Component
public class TopicRecommendationFilterAssembler {

    public CmsTopicFilterRequestDto build(Campaign campaign,
                                          EmailTemplate emailTemplate,
                                          LandingPage landingPage,
                                          SenderProfile senderProfile,
                                          ClientAdminInfoDto adminInfo,
                                          int page,
                                          int size) {
        CmsTopicFilterRequestDto.CmsTopicFilterRequestDtoBuilder builder = CmsTopicFilterRequestDto.builder();

        if (emailTemplate != null) {
            builder.payloadTypeIds(singleCatalogId(emailTemplate.getPayloadType()));
            builder.toneIds(singleCatalogId(emailTemplate.getTone()));
            builder.socialEngineeringStrategyIds(singleCatalogId(emailTemplate.getSocialEngineeringStrategy()));
            builder.triggerEventIds(singleCatalogId(emailTemplate.getTriggerEvent()));
            builder.attackTechniqueIds(singleCatalogId(emailTemplate.getAttackTechnique()));
            builder.attackerPersonaIds(singleCatalogId(emailTemplate.getAttackerPersona()));
            builder.campaignObjectiveIds(singleCatalogId(emailTemplate.getCampaignObjective()));
            builder.brandIds(singleCatalogId(emailTemplate.getBrand()));
            builder.callToActionIds(singleCatalogId(emailTemplate.getCallToAction()));
        }

        builder.difficultyIds(singleCatalogId(
                emailTemplate != null ? emailTemplate.getDifficultyLevel() : null,
                landingPage != null ? landingPage.getDifficultyLevel() : null));

        builder.emotionalTriggerIds(singleCatalogId(
                emailTemplate != null ? emailTemplate.getEmotionalTrigger() : null,
                landingPage != null ? landingPage.getEmotionalTrigger() : null));

        builder.urgencyLevelIds(singleCatalogId(
                emailTemplate != null ? emailTemplate.getUrgencyLevel() : null,
                landingPage != null ? landingPage.getUrgencyLevel() : null));

        builder.industryIds(resolveIndustryIds(senderProfile, emailTemplate, landingPage, adminInfo));

        if (adminInfo != null) {
            builder.complianceIds(singlePlainId(adminInfo.getComplianceId()));
            builder.subIndustryIds(singlePlainId(adminInfo.getSubIndustryId()));
            List<String> countryIds = singlePlainId(adminInfo.getCountry());
            if (countryIds == null && senderProfile != null) {
                countryIds = singlePlainId(senderProfile.getRegionId());
            }
            builder.countryIds(countryIds);
        } else if (senderProfile != null) {
            builder.countryIds(singlePlainId(senderProfile.getRegionId()));
        }

        List<String> tags = mergeTags(campaign, emailTemplate, landingPage, senderProfile);
        if (tags != null) {
            builder.tags(tags);
        }

        builder.status("ENABLED");
        builder.page(page);
        builder.size(size);
        builder.sortBy("topicName");
        builder.sortDirection("ASC");

        return builder.build();
    }

    private static List<String> resolveIndustryIds(SenderProfile senderProfile,
                                                   EmailTemplate emailTemplate,
                                                   LandingPage landingPage,
                                                   ClientAdminInfoDto adminInfo) {
        if (senderProfile != null && isNotBlank(senderProfile.getTargetIndustryId())) {
            return List.of(senderProfile.getTargetIndustryId().trim());
        }
        List<String> fromTemplate = singleCatalogId(
                emailTemplate != null ? emailTemplate.getTargetIndustry() : null);
        if (fromTemplate != null) {
            return fromTemplate;
        }
        List<String> fromLanding = singleCatalogId(
                landingPage != null ? landingPage.getTargetIndustry() : null);
        if (fromLanding != null) {
            return fromLanding;
        }
        if (adminInfo != null) {
            return singlePlainId(adminInfo.getIndustry());
        }
        return null;
    }

    private static List<String> mergeTags(Campaign campaign,
                                          EmailTemplate emailTemplate,
                                          LandingPage landingPage,
                                          SenderProfile senderProfile) {
        Set<String> merged = new LinkedHashSet<>();
        if (emailTemplate != null && emailTemplate.getTags() != null) {
            merged.addAll(emailTemplate.getTags());
        }
        if (landingPage != null && landingPage.getTags() != null) {
            merged.addAll(landingPage.getTags());
        }
        if (senderProfile != null && senderProfile.getTags() != null) {
            merged.addAll(senderProfile.getTags());
        }
        if (campaign != null && campaign.getCampaignTags() != null) {
            merged.addAll(campaign.getCampaignTags());
        }
        return merged.isEmpty() ? null : new ArrayList<>(merged);
    }

    private static List<String> singleCatalogId(Object... sources) {
        for (Object source : sources) {
            String id = CatalogDtoUtils.getId(source);
            if (isNotBlank(id)) {
                return List.of(id.trim());
            }
        }
        return null;
    }

    private static List<String> singlePlainId(String id) {
        return isNotBlank(id) ? List.of(id.trim()) : null;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
