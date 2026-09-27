package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.custom.LandingPageRepositoryCustom;
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
 * Custom repository implementation for LandingPage filtering.
 */
@Repository
@RequiredArgsConstructor
public class LandingPageRepositoryCustomImpl implements LandingPageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<LandingPage> findWithFilters(
            String clientId,
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            boolean isAspireAdmin,
            Pageable pageable) {

        Query query = buildLandingPagesQuery(
                clientId,
                searchParam,
                pageType,
                categoryId,
                difficultyId,
                status,
                tags,
                isAspireAdmin,
                pageable);

        return mongoTemplate.find(query, LandingPage.class);
    }

    @Override
    public long countWithFilters(
            String clientId,
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            boolean isAspireAdmin) {

        Query query = buildLandingPagesQuery(
                clientId,
                searchParam,
                pageType,
                categoryId,
                difficultyId,
                status,
                tags,
                isAspireAdmin,
                null);

        return mongoTemplate.count(query, LandingPage.class);
    }

    private Query buildLandingPagesQuery(
            String clientId,
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            boolean isAspireAdmin,
            Pageable pageable) {

        List<Criteria> andCriteria = new ArrayList<>();

        // Tenant access rules (aligned with SenderProfileRepository.findByClientIdOrGlobal):
        // - Platform roles: no tenant scoping (return all documents)
        // - Client admin: tenant pages (clientId match) OR any global page (isGlobal=true)
        if (!isAspireAdmin) {
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("clientId").is(clientId),
                    Criteria.where("isGlobal").is(true)
            ));
        }

        // Keyword search (name, tags)
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            String keyword = searchParam.trim();
            Pattern keywordPattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);

            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex(keywordPattern),
                    Criteria.where("tags").regex(keywordPattern)
            );
            andCriteria.add(keywordCriteria);
        }

        if (pageType != null) {
            andCriteria.add(Criteria.where("pageType").is(pageType));
        }

        if (categoryId != null && !categoryId.isBlank()) {
            andCriteria.add(Criteria.where("category.id").is(categoryId));
        }

        if (difficultyId != null && !difficultyId.isBlank()) {
            andCriteria.add(Criteria.where("difficultyLevel.id").is(difficultyId));
        }

        if (status != null) {
            andCriteria.add(Criteria.where("status").is(status));
        }

        if (tags != null && !tags.isEmpty()) {
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

