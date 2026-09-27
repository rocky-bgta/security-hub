package com.aspire.asat.registration.repository.custom.impl;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EndUserRepositoryCustomImpl implements EndUserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Document> getFilteredEndUsers(String clientAdminId,
                                              String search,
                                              AdminStatus status,
                                              String department,
                                              int offset,
                                              int pageSize) {

        List<Criteria> criteriaList = new ArrayList<>();

        // required scope
        criteriaList.add(Criteria.where("clientAdminId").is(clientAdminId));

        // optional exact department filter
        if (department != null && !department.isBlank()) {
            criteriaList.add(Criteria.where("department").is(department));
        }

        // optional search across name/email/department
        if (search != null && !search.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("organizationName").regex(search, "i"),
                    Criteria.where("department").regex(search, "i")
            ));
        }

        // optional status filter
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        MatchOperation match = Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "createdAt");
        long skipSize = offset == 0 ? 0 : ((long) offset * pageSize);
        SkipOperation skip = Aggregation.skip(skipSize);
        LimitOperation limit = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(match, sort, skip, limit);

        return mongoTemplate.aggregate(aggregation, "client_admins", Document.class).getMappedResults();
    }


    @Override
    public long countFilteredEndUsers(String clientAdminId,
                                      String search,
                                      AdminStatus status,
                                      String department) {

        List<Criteria> criteriaList = new ArrayList<>();

        // required scope
        criteriaList.add(Criteria.where("clientAdminId").is(clientAdminId));

        // optional exact department filter
        if (department != null && !department.isBlank()) {
            criteriaList.add(Criteria.where("department").is(department));
        }

        // optional search across name/email/department
        if (search != null && !search.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("organizationName").regex(search, "i"),
                    Criteria.where("department").regex(search, "i")
            ));
        }

        // optional status filter
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        MatchOperation match = Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        Aggregation aggregation = Aggregation.newAggregation(match, Aggregation.count().as("total"));

        List<Document> result = mongoTemplate.aggregate(aggregation, "client_admins", Document.class).getMappedResults();
        return result.isEmpty() ? 0 : ((Number) result.get(0).get("total")).longValue();
    }


    @Override
    public List<Document> findUnassignedEndUsers(String clientAdminId,
                                                 String productId,
                                                 String search,
                                                 AdminStatus status,
                                                 int offset,
                                                 int pageSize) {
        // 1) fetch assigned endUserIds for this product
        Criteria assignedCriteria = Criteria.where("productId").is(productId);
        MatchOperation matchAssigned = Aggregation.match(assignedCriteria);
        ProjectionOperation projectEndUserId = Aggregation.project().and("endUserId").as("endUserId");

        Aggregation assignedAgg = Aggregation.newAggregation(
                matchAssigned,
                projectEndUserId
        );

        List<Document> assignedDocs = mongoTemplate
                .aggregate(assignedAgg, "end_user_products", Document.class)
                .getMappedResults();

        List<String> assignedEndUserIds = assignedDocs.stream()
                .map(d -> d.getString("endUserId"))
                .filter(id -> id != null && !id.isBlank())
                .toList();

        // 2) build criteria for client_admins excluding those assigned ids
        List<Criteria> ands = new ArrayList<>();
        ands.add(Criteria.where("clientAdminId").is(clientAdminId));

        if (!assignedEndUserIds.isEmpty()) {
            ands.add(Criteria.where("_id").nin(assignedEndUserIds));
        }

        if (status != null) {
            ands.add(Criteria.where("status").is(status));
        }

        if (search != null && !search.isBlank()) {
            ands.add(new Criteria().orOperator(
                    Criteria.where("organizationName").regex(search, "i"),
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("department").regex(search, "i")
            ));
        }

        MatchOperation matchUnassigned = Aggregation.match(new Criteria().andOperator(ands.toArray(new Criteria[0])));

        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "createdAt");
        long skipSize = offset == 0 ? 0 : (offset * pageSize);
        SkipOperation skip = Aggregation.skip(skipSize);
        LimitOperation limit = Aggregation.limit(pageSize);

        Aggregation agg = Aggregation.newAggregation(
                matchUnassigned,
                sort,
                skip,
                limit
        );

        return mongoTemplate.aggregate(agg, "client_admins", Document.class).getMappedResults();
    }

    @Override
    public long countUnassignedEndUsers(String clientAdminId,
                                        String productId,
                                        String search,
                                        AdminStatus status) {
        // 1) fetch assigned endUserIds for this product
        Criteria assignedCriteria = Criteria.where("productId").is(productId);
        MatchOperation matchAssigned = Aggregation.match(assignedCriteria);
        ProjectionOperation projectEndUserId = Aggregation.project().and("endUserId").as("endUserId");

        Aggregation assignedAgg = Aggregation.newAggregation(
                matchAssigned,
                projectEndUserId
        );

        List<Document> assignedDocs = mongoTemplate
                .aggregate(assignedAgg, "end_user_products", Document.class)
                .getMappedResults();

        List<String> assignedEndUserIds = assignedDocs.stream()
                .map(d -> d.getString("endUserId"))
                .filter(id -> id != null && !id.isBlank())
                .toList();

        // 2) build criteria for count
        List<Criteria> ands = new ArrayList<>();
        ands.add(Criteria.where("clientAdminId").is(clientAdminId));

        if (!assignedEndUserIds.isEmpty()) {
            ands.add(Criteria.where("_id").nin(assignedEndUserIds));
        }

        if (status != null) {
            ands.add(Criteria.where("status").is(status));
        }

        if (search != null && !search.isBlank()) {
            ands.add(new Criteria().orOperator(
                    Criteria.where("organizationName").regex(search, "i"),
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("department").regex(search, "i")
            ));
        }

        Criteria finalCriteria = new Criteria().andOperator(ands.toArray(new Criteria[0]));
        return mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(finalCriteria), "client_admins");
    }

    @Override
    public List<AspireUser> findAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            int offset,
            int pageSize) {
        log.info("Finding AspireUsers with filters - search: {}, userType: {}, country: {}, mspId: {}, clientAdminId: {}, status: {}, offset: {}, pageSize: {}", 
                search, userType, country, mspId, clientAdminId, status, offset, pageSize);

        // Build query with filters
        Query query = buildAspireUserFilterQuery(search, userType, country, mspId, clientAdminId, status);
        
        // Apply sorting by createdAt descending (newest first)
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // Validate and apply pagination
        if (offset < 0) {
            log.warn("Invalid offset: {}. Setting to 0.", offset);
            offset = 0;
        }
        if (pageSize <= 0) {
            log.warn("Invalid pageSize: {}. Setting to 10.", pageSize);
            pageSize = 10;
        }
        
        // Convert offset to skip count
        int skip = offset == 0 ? 0 : (offset * pageSize);
        query.skip(skip);
        query.limit(pageSize);
        
        log.info("Executing query with skip: {} (offset {} * pageSize {}), limit: {}", skip, offset, pageSize, pageSize);
        
        // Execute query
        List<AspireUser> users = mongoTemplate.find(query, AspireUser.class);
        log.info("Found {} AspireUsers", users.size());
        
        return users;
    }

    @Override
    public long countAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status) {
        log.info("Counting AspireUsers with filters - search: {}, userType: {}, country: {}, mspId: {}, clientAdminId: {}, status: {}", 
                search, userType, country, mspId, clientAdminId, status);

        // Build query with filters
        Query query = buildAspireUserFilterQuery(search, userType, country, mspId, clientAdminId, status);
        
        // Get count
        long count = mongoTemplate.count(query, AspireUser.class);
        log.info("Total count: {}", count);
        return count;
    }

    /**
     * Build MongoDB query with all filter criteria for AspireUser
     */
    private Query buildAspireUserFilterQuery(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status) {
        
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Search by email (username field) - case insensitive regex
        if (search != null && !search.isBlank()) {
            String searchPattern = ".*" + search + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("email").regex(searchPattern, "i"),
                    Criteria.where("username").regex(searchPattern, "i")
            ));
        }
        
        // Filter by userType (MSP, CLIENT_ADMIN, USER, etc.)
        if (userType != null && !userType.isBlank()) {
            criteriaList.add(Criteria.where("userType").is(userType.toUpperCase()));
        }
        
        // Filter by country
        if (country != null && !country.isBlank()) {
            criteriaList.add(Criteria.where("country").is(country));
        }
        
        // Filter by mspId
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspId").is(mspId));
        }
        
        // Filter by clientAdminId
        if (clientAdminId != null && !clientAdminId.isBlank()) {
            criteriaList.add(Criteria.where("clientAdminId").is(clientAdminId));
        }
        
        // Filter by status
        if (status != null && !status.isBlank()) {
            criteriaList.add(Criteria.where("status").is(status.toUpperCase()));
        }
        
        // Combine all criteria with AND operator
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        return query;
    }

}