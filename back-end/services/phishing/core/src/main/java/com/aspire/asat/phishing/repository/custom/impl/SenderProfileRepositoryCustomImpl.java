package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.repository.custom.SenderProfileRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MongoTemplate-based filtering aligned with findByClientIdOrGlobal visibility.
 */
@Repository
@RequiredArgsConstructor
public class SenderProfileRepositoryCustomImpl implements SenderProfileRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<SenderProfile> findWithFilters(
            String tenantClientId,
            String searchKeyword,
            ProfileType profileType,
            Boolean isVerified,
            String category,
            String targetIndustryId,
            String regionId,
            String language,
            String deceptionLevelId,
            List<String> psychologicalTriggers,
            DomainType domainType,
            String personalizationLevelId,
            ProviderType providerType,
            List<String> tags,
            Pageable pageable) {

        Query query = buildQuery(tenantClientId, searchKeyword, profileType, isVerified, category, targetIndustryId,
                regionId, language, deceptionLevelId, psychologicalTriggers, domainType, personalizationLevelId, providerType, tags);
        query.with(pageable);
        return mongoTemplate.find(query, SenderProfile.class);
    }

    @Override
    public long countWithFilters(
            String tenantClientId,
            String searchKeyword,
            ProfileType profileType,
            Boolean isVerified,
            String category,
            String targetIndustryId,
            String regionId,
            String language,
            String deceptionLevelId,
            List<String> psychologicalTriggers,
            DomainType domainType,
            String personalizationLevelId,
            ProviderType providerType,
            List<String> tags) {

        Query query = buildQuery(tenantClientId, searchKeyword, profileType, isVerified, category, targetIndustryId,
                regionId, language, deceptionLevelId, psychologicalTriggers, domainType, personalizationLevelId, providerType, tags);
        return mongoTemplate.count(query, SenderProfile.class);
    }

    private Query buildQuery(
            String tenantClientId,
            String searchKeyword,
            ProfileType profileType,
            Boolean isVerified,
            String category,
            String targetIndustryId,
            String regionId,
            String language,
            String deceptionLevelId,
            List<String> psychologicalTriggers,
            DomainType domainType,
            String personalizationLevelId,
            ProviderType providerType,
            List<String> tags) {

        List<Criteria> andCriteria = new ArrayList<>();

        if (tenantClientId != null && !tenantClientId.trim().isEmpty()) {
            Criteria tenantScope = new Criteria().orOperator(
                    Criteria.where("clientId").is(tenantClientId.trim()),
                    Criteria.where("isGlobal").is(true)
            );
            andCriteria.add(tenantScope);
        }

        if (profileType != null) {
            andCriteria.add(Criteria.where("profileType").is(profileType));
        }

        if (isVerified != null) {
            andCriteria.add(Criteria.where("isVerified").is(isVerified));
        }

        if (category != null && !category.trim().isEmpty()) {
            andCriteria.add(Criteria.where("category").is(category.trim()));
        }
        if (targetIndustryId != null && !targetIndustryId.trim().isEmpty()) {
            andCriteria.add(Criteria.where("targetIndustryId").is(targetIndustryId.trim()));
        }
        if (regionId != null && !regionId.trim().isEmpty()) {
            andCriteria.add(Criteria.where("regionId").is(regionId.trim()));
        }
        if (language != null && !language.trim().isEmpty()) {
            andCriteria.add(Criteria.where("language").is(language.trim()));
        }
        if (deceptionLevelId != null && !deceptionLevelId.isBlank()) {
            andCriteria.add(Criteria.where("deceptionLevel.id").is(deceptionLevelId.trim()));
        }
        if (domainType != null) {
            andCriteria.add(Criteria.where("domainType").is(domainType));
        }
        if (personalizationLevelId != null && !personalizationLevelId.isBlank()) {
            andCriteria.add(Criteria.where("personalizationLevel.id").is(personalizationLevelId.trim()));
        }
        if (providerType != null) {
            andCriteria.add(Criteria.where("providerType").is(providerType));
        }
        if (psychologicalTriggers != null && !psychologicalTriggers.isEmpty()) {
            andCriteria.add(Criteria.where("psychologicalTriggers").in(psychologicalTriggers));
        }
        if (tags != null && !tags.isEmpty()) {
            andCriteria.add(Criteria.where("tags").in(tags));
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            String quoted = Pattern.quote(searchKeyword.trim());
            Pattern keywordPattern = Pattern.compile(quoted, Pattern.CASE_INSENSITIVE);
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("profileName").regex(keywordPattern),
                    Criteria.where("fromAddress").regex(keywordPattern),
                    Criteria.where("host").regex(keywordPattern)
            );
            andCriteria.add(keywordCriteria);
        }

        Criteria finalCriteria;
        if (andCriteria.isEmpty()) {
            finalCriteria = new Criteria();
        } else {
            finalCriteria = new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        }

        return new Query(finalCriteria);
    }
}
