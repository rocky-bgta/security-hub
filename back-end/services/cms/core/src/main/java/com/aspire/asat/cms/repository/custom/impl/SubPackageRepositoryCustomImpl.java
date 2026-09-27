package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubPackageRepositoryCustomImpl implements SubPackageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<SubPackage> findSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds,
            Pageable pageable
    ) {
        log.info("Finding SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}, clientAdminIds: {}", 
                search, status, clientId, productId, clientAdminIds);

        // Build query with criteria
        Query query = buildQuery(search, status, clientId, productId, clientAdminIds);
        
        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<SubPackage> subPackages = mongoTemplate.find(query, SubPackage.class);
        
        // Get total count for pagination
        long totalCount = mongoTemplate.count(query, SubPackage.class);

        log.info("Found {} SubPackages out of {} total", subPackages.size(), totalCount);
        
        return new PageImpl<>(subPackages, pageable, totalCount);
    }

    @Override
    public long countSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds
    ) {
        log.info("Counting SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}, clientAdminIds: {}", 
                search, status, clientId, productId, clientAdminIds);

        // Build query with criteria
        Query query = buildQuery(search, status, clientId, productId, clientAdminIds);
        
        // Execute count query
        long count = mongoTemplate.count(query, SubPackage.class);
        
        log.info("Total count: {}", count);
        
        return count;
    }

    /**
     * Build MongoDB query with dynamic criteria based on provided filters
     */
    private Query buildQuery(String search, SubPackageStatus status, String clientId, String productId,
                             List<String> clientAdminIds) {
        Criteria criteria = new Criteria();

        // Always exclude soft-deleted records
        criteria.and("deleted").is(false);

        // Add search criteria (case-insensitive)
        if (StringUtils.hasText(search)) {
            criteria.and("name").regex(search.trim(), "i");
        }

        // Add status filter
        if (status != null) {
            criteria.and("status").is(status);
        }

        // Prefer clientAdminIds list (MSP scope); otherwise filter by single clientId
        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteria.and("clientAdminId").in(clientAdminIds);
        } else if (StringUtils.hasText(clientId)) {
            criteria.and("clientId").is(clientId);
        }

        // Add product ID filter
        if (StringUtils.hasText(productId)) {
            criteria.and("productId").is(productId);
        }

        return new Query(criteria);
    }

    @Override
    public Page<SubPackage> findTrialSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            Pageable pageable
    ) {
        log.info("Finding Trial SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}", 
                search, status, clientId, productId);

        // Build query with criteria including isTrial = true
        Query query = buildTrialQuery(search, status, clientId, productId);
        
        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<SubPackage> subPackages = mongoTemplate.find(query, SubPackage.class);
        
        // Get total count for pagination (without pagination applied)
        Query countQuery = buildTrialQuery(search, status, clientId, productId);
        long totalCount = mongoTemplate.count(countQuery, SubPackage.class);

        log.info("Found {} Trial SubPackages out of {} total", subPackages.size(), totalCount);
        
        return new PageImpl<>(subPackages, pageable, totalCount);
    }

    @Override
    public long countTrialSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId
    ) {
        log.info("Counting Trial SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}", 
                search, status, clientId, productId);

        // Build query with criteria including isTrial = true
        Query query = buildTrialQuery(search, status, clientId, productId);
        
        // Execute count query
        long count = mongoTemplate.count(query, SubPackage.class);
        
        log.info("Total Trial SubPackages count: {}", count);
        
        return count;
    }

    /**
     * Build MongoDB query with dynamic criteria for Trial SubPackages (isTrial = true)
     */
    private Query buildTrialQuery(String search, SubPackageStatus status, String clientId, String productId) {
        Criteria criteria = new Criteria();

        // Always exclude soft-deleted records
        criteria.and("deleted").is(false);
        
        // Filter for trial packages only
        criteria.and("isTrial").is(true);

        // Add search criteria (case-insensitive)
        if (StringUtils.hasText(search)) {
            criteria.and("name").regex(search.trim(), "i");
        }

        // Add status filter
        if (status != null) {
            criteria.and("status").is(status);
        }

        // Add client ID filter
        if (StringUtils.hasText(clientId)) {
            criteria.and("clientId").is(clientId);
        }

        // Add product ID filter
        if (StringUtils.hasText(productId)) {
            criteria.and("productId").is(productId);
        }

        return new Query(criteria);
    }

    @Override
    public long countSubPackagesForReport(String clientAdminId, List<String> clientAdminIds) {
        Criteria criteria = Criteria.where("deleted").is(false);

        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteria.and("clientAdminId").in(clientAdminIds);
        } else if (StringUtils.hasText(clientAdminId)) {
            criteria.and("clientAdminId").is(clientAdminId);
        }

        return mongoTemplate.count(new Query(criteria), SubPackage.class);
    }

    @Override
    public long countDistinctTopicsInSubPackages(String clientAdminId, List<String> clientAdminIds) {
        Criteria criteria = Criteria.where("deleted").is(false)
                .and("topicId").exists(true).ne(null);

        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteria.and("clientAdminId").in(clientAdminIds);
        } else if (StringUtils.hasText(clientAdminId)) {
            criteria.and("clientAdminId").is(clientAdminId);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.unwind("topicId"),
                Aggregation.group("topicId"),
                Aggregation.count().as("count")
        );

        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, SubPackage.class, Document.class);
        Document doc = results.getUniqueMappedResult();
        if (doc == null) {
            return 0L;
        }
        Number count = doc.get("count", Number.class);
        return count != null ? count.longValue() : 0L;
    }
}
