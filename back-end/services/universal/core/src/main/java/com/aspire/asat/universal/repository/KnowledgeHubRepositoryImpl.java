package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.KnowlegeHub;
import com.aspire.asat.universal.enums.ResourceType;
import com.aspire.asat.universal.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class KnowledgeHubRepositoryImpl implements KnowledgeHubRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeHubRepositoryImpl.class);
    private final MongoTemplate mongoTemplate;

    public KnowledgeHubRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<KnowlegeHub> findAllWithFilters(String search, String categoryId, String resourceType, String status, Pageable pageable) {
        log.debug("Finding all knowledge hub items with filters - search: {}, categoryId: {}, resourceType: {}, status: {}",
                search, categoryId, resourceType, status);

        Query query = new Query();
        addCommonFilters(query, search, categoryId, resourceType, status);

        long total = mongoTemplate.count(query, KnowlegeHub.class);

        // Apply sorting by createdAt DESC and pagination
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);

        List<KnowlegeHub> results = mongoTemplate.find(query, KnowlegeHub.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<KnowlegeHub> findActiveWithFilters(String search, String categoryId, String resourceType,
                                                   LocalDateTime currentDate, Pageable pageable) {
        log.debug("Finding active knowledge hub items with filters - search: {}, categoryId: {}, resourceType: {}, currentDate: {}",
                search, categoryId, resourceType, currentDate);

        Query query = new Query();

        // Active status filter
        query.addCriteria(Criteria.where("status").is(Status.ACTIVE));

        // Published before current date
        query.addCriteria(Criteria.where("publishedDate").lt(currentDate));

        // Not expired (expiry date after current date)
        query.addCriteria(Criteria.where("expireDate").gt(currentDate));

        // Add common filters (status is null since we already filter by ACTIVE)
        addCommonFilters(query, search, categoryId, resourceType, null);

        long total = mongoTemplate.count(query, KnowlegeHub.class);

        // Apply sorting by createdAt DESC
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);

        List<KnowlegeHub> results = mongoTemplate.find(query, KnowlegeHub.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public long countAllWithFilters(String search, String categoryId, String resourceType, String status) {
        Query query = new Query();
        addCommonFilters(query, search, categoryId, resourceType, status);
        return mongoTemplate.count(query, KnowlegeHub.class);
    }

    @Override
    public long countActiveWithFilters(String search, String categoryId, String resourceType, LocalDateTime currentDate) {
        Query query = new Query();

        // Active status filter
        query.addCriteria(Criteria.where("status").is(Status.ACTIVE));

        // Published before current date
        query.addCriteria(Criteria.where("publishedDate").lt(currentDate));

        // Not expired (expiry date after current date)
        query.addCriteria(Criteria.where("expireDate").gt(currentDate));

        // Add common filters (status is null since we already filter by ACTIVE)
        addCommonFilters(query, search, categoryId, resourceType, null);

        return mongoTemplate.count(query, KnowlegeHub.class);
    }

    /**
     * Add common filters to the query
     */
    private void addCommonFilters(Query query, String search, String categoryId, String resourceType, String status) {
        // Search by name (case-insensitive)
        if (search != null && !search.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(search.trim(), Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
            log.debug("Applied search filter: {}", search);
        }

        // Filter by categoryId
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId.trim()));
            log.debug("Applied categoryId filter: {}", categoryId);
        }

        // Filter by resourceType
        if (resourceType != null && !resourceType.trim().isEmpty()) {
            try {
                ResourceType rt = ResourceType.valueOf(resourceType.trim().toUpperCase());
                query.addCriteria(Criteria.where("resourceType").is(rt));
                log.debug("Applied resourceType filter: {}", rt);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid resourceType provided: {}, ignoring filter", resourceType);
            }
        }

        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            try {
                Status statusEnum = Status.valueOf(status.trim().toUpperCase());
                query.addCriteria(Criteria.where("status").is(statusEnum));
                log.debug("Applied status filter: {}", statusEnum);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}, ignoring filter", status);
            }
        }
    }
}

