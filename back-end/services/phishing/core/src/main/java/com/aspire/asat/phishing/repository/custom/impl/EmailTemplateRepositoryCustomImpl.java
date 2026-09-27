package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.repository.custom.EmailTemplateRepositoryCustom;
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
 * Custom repository implementation for EmailTemplate filtering.
 */
@Repository
@RequiredArgsConstructor
public class EmailTemplateRepositoryCustomImpl implements EmailTemplateRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<EmailTemplate> findWithFilters(
            String clientId,
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            boolean isAspireAdmin,
            Pageable pageable) {

        Query query = buildTemplatesQuery(
                clientId, searchParam, difficultyLevelId, payloadTypeId, location, tags, language, status,
                templateType, isAspireAdmin, pageable);

        return mongoTemplate.find(query, EmailTemplate.class);
    }

    @Override
    public long countWithFilters(
            String clientId,
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            boolean isAspireAdmin) {

        Query query = buildTemplatesQuery(
                clientId, searchParam, difficultyLevelId, payloadTypeId, location, tags, language, status,
                templateType, isAspireAdmin, null);

        return mongoTemplate.count(query, EmailTemplate.class);
    }

    private Query buildTemplatesQuery(
            String clientId,
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            boolean isAspireAdmin,
            Pageable pageable) {

        List<Criteria> andCriteria = new ArrayList<>();

        // Tenant scoping (aligned with SenderProfileRepository.findByClientIdOrGlobal):
        // - Platform users: no tenant filter unless clientId param is set (see below).
        // - Clients: tenant templates OR any global template (SUPER_ADMIN / ASPIRE_ADMIN / SYSTEM_USER).
        if (!isAspireAdmin) {
            Criteria accessCriteria = new Criteria().orOperator(
                    Criteria.where("clientId").is(clientId),
                    Criteria.where("isGlobal").is(true)
            );
            andCriteria.add(accessCriteria);
        }

        // Keyword search (templateName/emailSubject/tags) - same fields used by keyword search repository.
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            String keyword = searchParam.trim();
            Pattern keywordPattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);

            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("templateName").regex(keywordPattern),
                    Criteria.where("emailSubject").regex(keywordPattern),
                    Criteria.where("tags").regex(keywordPattern)
            );
            andCriteria.add(keywordCriteria);
        }

        if (difficultyLevelId != null && !difficultyLevelId.isBlank()) {
            andCriteria.add(Criteria.where("difficultyLevel.id").is(difficultyLevelId.trim()));
        }

        if (payloadTypeId != null && !payloadTypeId.isBlank()) {
            andCriteria.add(Criteria.where("payloadType.id").is(payloadTypeId.trim()));
        }

        if (location != null && !location.trim().isEmpty()) {
            andCriteria.add(Criteria.where("serviceLocation").is(location));
        }

        if (language != null && !language.trim().isEmpty()) {
            andCriteria.add(Criteria.where("language").is(language));
        }

        if (status != null) {
            andCriteria.add(Criteria.where("status").is(status));
        }

        if (templateType != null) {
            if (templateType == TemplateType.EMAIL) {
                andCriteria.add(new Criteria().orOperator(
                        Criteria.where("templateType").is(TemplateType.EMAIL),
                        Criteria.where("templateType").exists(false)
                ));
            } else {
                andCriteria.add(Criteria.where("templateType").is(templateType));
            }
        }

        if (tags != null && !tags.isEmpty()) {
            // Match if template tags contain ANY of the selected tags.
            andCriteria.add(Criteria.where("tags").in(tags));
        }

        if (isAspireAdmin && clientId != null && !clientId.trim().isEmpty()) {
            andCriteria.add(Criteria.where("clientId").is(clientId));
        }

        Criteria finalCriteria;
        if (andCriteria.isEmpty()) {
            finalCriteria = new Criteria(); // matches all documents
        } else {
            finalCriteria = new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        }

        Query query = new Query(finalCriteria);
        if (pageable != null) {
            query.with(pageable);
        }

        return query;
    }
}
