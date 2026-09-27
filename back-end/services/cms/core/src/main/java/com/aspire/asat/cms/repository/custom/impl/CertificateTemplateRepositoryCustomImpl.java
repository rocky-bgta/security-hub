package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.model.CertificateTemplate;
import com.aspire.asat.cms.repository.custom.CertificateTemplateRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CertificateTemplateRepositoryCustomImpl implements CertificateTemplateRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<CertificateTemplate> findAllWithPaginationAndSearch(
            String search,
            Status status,
            int offset,
            int pageSize,
            String sortBy,
            String order
    ) {
        Query query = buildSearchQuery(search, status);

        // Apply sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        query.with(Sort.by(direction, sortBy));

        // Apply pagination
        query.skip(offset);
        query.limit(pageSize);

        return mongoTemplate.find(query, CertificateTemplate.class);
    }

    @Override
    public long countWithSearch(String search, Status status) {
        Query query = buildSearchQuery(search, status);
        return mongoTemplate.count(query, CertificateTemplate.class);
    }

    @Override
    public List<CertificateTemplate> findAllExcludingIdsWithPaginationAndSearch(
            String search,
            Status status,
            List<String> excludeIds,
            int offset,
            int pageSize,
            String sortBy,
            String order
    ) {
        Query query = buildSearchQueryExcludingIds(search, status, excludeIds);

        // Apply sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        query.with(Sort.by(direction, sortBy));

        // Apply pagination
        query.skip(offset);
        query.limit(pageSize);

        return mongoTemplate.find(query, CertificateTemplate.class);
    }

    @Override
    public long countExcludingIdsWithSearch(String search, Status status, List<String> excludeIds) {
        Query query = buildSearchQueryExcludingIds(search, status, excludeIds);
        return mongoTemplate.count(query, CertificateTemplate.class);
    }

    private Query buildSearchQuery(String search, Status status) {
        Query query = new Query();

        // Search by template name (case-insensitive)
        if (search != null && !search.isBlank()) {
            query.addCriteria(Criteria.where("templateName").regex(search, "i"));
        }

        // Filter by status
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return query;
    }

    private Query buildSearchQueryExcludingIds(String search, Status status, List<String> excludeIds) {
        Query query = new Query();

        // Exclude specified template IDs
        if (excludeIds != null && !excludeIds.isEmpty()) {
            query.addCriteria(Criteria.where("id").nin(excludeIds));
        }

        // Search by template name (case-insensitive)
        if (search != null && !search.isBlank()) {
            query.addCriteria(Criteria.where("templateName").regex(search, "i"));
        }

        // Filter by status
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return query;
    }
}

