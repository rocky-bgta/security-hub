package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.custom.UserRiskProfileEmailTotals;
import com.aspire.asat.phishing.repository.custom.UserRiskProfileRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom repository implementation for UserRiskProfile using MongoDB Criteria.
 * Handles search (email, firstName) and filters (clientId, department).
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRiskProfileRepositoryCustomImpl implements UserRiskProfileRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<UserRiskProfile> findWithFilters(String clientId, String department, String search, Pageable pageable) {
        Query query = buildCriteriaQuery(clientId, department, search, null);
        long total = mongoTemplate.count(query, UserRiskProfile.class);
        List<UserRiskProfile> content = mongoTemplate.find(query.with(pageable), UserRiskProfile.class);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<UserRiskProfile> findWithFilters(
            String clientId,
            String department,
            String search,
            RiskLevel riskLevel,
            Pageable pageable
    ) {
        Query query = buildCriteriaQuery(clientId, department, search, riskLevel);
        long total = mongoTemplate.count(query, UserRiskProfile.class);
        List<UserRiskProfile> content = mongoTemplate.find(query.with(pageable), UserRiskProfile.class);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<UserRiskProfile> findListWithFilters(String clientId, String department, String search, Sort sort) {
        Query query = buildCriteriaQuery(clientId, department, search, null);
        if (sort != null) {
            query.with(sort);
        }
        return mongoTemplate.find(query, UserRiskProfile.class);
    }

    @Override
    public UserRiskProfileEmailTotals aggregateEmailTotals() {
        return aggregateEmailTotalsInternal(null);
    }

    @Override
    public UserRiskProfileEmailTotals aggregateEmailTotalsForClientIds(Collection<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return UserRiskProfileEmailTotals.empty();
        }
        return aggregateEmailTotalsInternal(clientIds);
    }

    private UserRiskProfileEmailTotals aggregateEmailTotalsInternal(Collection<String> clientIds) {
        List<AggregationOperation> operations = new ArrayList<>();
        if (clientIds != null && !clientIds.isEmpty()) {
            operations.add(Aggregation.match(Criteria.where("clientId").in(clientIds)));
        }
        operations.add(Aggregation.group()
                .sum("emailsReceived").as("emailsReceived")
                .sum("emailsOpened").as("emailsOpened")
                .sum("emailsReported").as("emailsReported")
                .sum("linksClicked").as("linksClicked"));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(operations), UserRiskProfile.class, Document.class);

        List<Document> mapped = results.getMappedResults();
        if (mapped.isEmpty()) {
            return UserRiskProfileEmailTotals.empty();
        }

        Document doc = mapped.get(0);
        return new UserRiskProfileEmailTotals(
                longOrZero(doc, "emailsReceived"),
                longOrZero(doc, "emailsOpened"),
                longOrZero(doc, "emailsReported"),
                longOrZero(doc, "linksClicked")
        );
    }

    private static long longOrZero(Document doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    /**
     * Builds a Query with Criteria for clientId (required), optional department/riskLevel,
     * and optional search on email, firstName, and lastName.
     */
    private Query buildCriteriaQuery(String clientId, String department, String search, RiskLevel riskLevel) {
        Query query = new Query();

        // Required: filter by client (clientAdminId maps to clientId)
        if (clientId != null && !clientId.isBlank()) {
            query.addCriteria(Criteria.where("clientId").is(clientId));
        }

        // Optional: filter by department
        if (department != null && !department.isBlank()) {
            query.addCriteria(Criteria.where("department").is(department));
        }

        // Optional: filter by risk level
        if (riskLevel != null) {
            query.addCriteria(Criteria.where("riskLevel").is(riskLevel));
        }

        // Optional: search on email, firstName, and lastName (case-insensitive regex)
        if (search != null && !search.isBlank()) {
            String searchRegex = toContainsRegex(search.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("email").regex(searchRegex, "i"),
                    Criteria.where("firstName").regex(searchRegex, "i"),
                    Criteria.where("lastName").regex(searchRegex, "i")
            ));
        }

        return query;
    }

    private static String toContainsRegex(String search) {
        if (search == null || search.isEmpty()) {
            return ".*";
        }
        String escaped = Pattern.quote(search);
        return ".*" + escaped + ".*";
    }
}
