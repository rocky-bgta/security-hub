package com.aspire.asat.registration.repository.impl;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.repository.custom.ClientAdminRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ClientAdminRepositoryImpl implements ClientAdminRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ClientAdmin> findClientAdminsWithFilters(
            String search,
            String mspId,
            AdminStatus status,
            Instant createdAt,
            String country,
            String state,
            Pageable pageable) {

        log.info("Finding client admins with filters - search: {}, mspId: {}, status: {}, createdAt: {}, country: {}, state: {}, page: {}, size: {}",
                search, mspId, status, createdAt, country, state, pageable.getPageNumber(), pageable.getPageSize());

        Query query = buildFilterQuery(search, mspId, status, createdAt, country, state);
        
        // Apply pagination
        query.with(pageable);
        
        // Apply sorting by createdAt descending (newest first)
        query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        List<ClientAdmin> clientAdmins = mongoTemplate.find(query, ClientAdmin.class);
        long total = mongoTemplate.count(Query.of(query).limit(0).skip(0), ClientAdmin.class);

        log.info("Found {} client admins out of {} total", clientAdmins.size(), total);

        return new PageImpl<>(clientAdmins, pageable, total);
    }

    @Override
    public long countClientAdminsWithFilters(
            String search,
            String mspId,
            AdminStatus status,
            Instant createdAt,
            String country,
            String state) {

        log.info("Counting client admins with filters - search: {}, mspId: {}, status: {}, createdAt: {}, country: {}, state: {}",
                search, mspId, status, createdAt, country, state);

        Query query = buildFilterQuery(search, mspId, status, createdAt, country, state);
        long count = mongoTemplate.count(query, ClientAdmin.class);

        log.info("Total count: {}", count);
        return count;
    }

    private Query buildFilterQuery(String search, String mspId, AdminStatus status, Instant createdAt,
                                   String country, String state) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        // Search filter - search in email, organizationName (client name), and domain
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = ".*" + search.trim() + ".*";
            criteria.orOperator(
                    Criteria.where("email").regex(searchPattern, "i"),
                    Criteria.where("organizationName").regex(searchPattern, "i"),
                    Criteria.where("domain").regex(searchPattern, "i")
            );
        }

        // MSP ID filter
        if (mspId != null && !mspId.trim().isEmpty()) {
            criteria.and("mspId").is(mspId.trim());
        }

        // Status filter
        if (status != null) {
            criteria.and("status").is(status);
        }

        // Country filter
        if (country != null && !country.trim().isEmpty()) {
            criteria.and("country").is(country.trim());
        }

        // State filter
        if (state != null && !state.trim().isEmpty()) {
            criteria.and("state").is(state.trim());
        }

        // Created date filter
        if (createdAt != null) {
            // For Instant, we'll filter by exact date match
            // You can modify this to use date range if needed
            criteria.and("createdAt").gte(createdAt).lt(createdAt.plusSeconds(86400)); // 24 hours range
        }

        query.addCriteria(criteria);
        return query;
    }
}
