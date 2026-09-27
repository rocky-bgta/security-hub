package com.aspire.asat.universal.repository.custom.impl;

import com.aspire.asat.universal.supportTicket.enums.AssignCategory;
import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.repository.custom.SupportResolutionTimeTypeAggregate;
import com.aspire.asat.universal.repository.custom.SupportTicketRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom repository implementation for SupportTicket using MongoTemplate
 */
@Repository
@RequiredArgsConstructor
public class SupportTicketRepositoryCustomImpl implements SupportTicketRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "support_tickets";

    @Override
    public Page<SupportTicket> findSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            Pageable pageable) {
        return findSupportTicketsWithFilters(
                clientId, userId, parentTicketId, assignedTo, status, priority, supportType, mspId,
                assignToSuperAdmin, createdBy, assignCategory, search, null, null, pageable);
    }

    @Override
    public Page<SupportTicket> findSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate,
            Pageable pageable) {
        Query query = buildQueryCriteria(clientId, null, userId, parentTicketId, assignedTo,
                status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, fromDate, toDate);

        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<SupportTicket> tickets = mongoTemplate.find(query, SupportTicket.class, COLLECTION_NAME);

        // Defer count query; PageableExecutionUtils may skip it when possible.
        return PageableExecutionUtils.getPage(tickets, pageable, () -> {
            Query countQuery = buildQueryCriteria(clientId, null, userId, parentTicketId, assignedTo,
                    status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, fromDate, toDate);
            return mongoTemplate.count(countQuery, SupportTicket.class, COLLECTION_NAME);
        });
    }

    @Override
    public long countSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search) {
        return countSupportTicketsWithFilters(
                clientId, userId, parentTicketId, assignedTo, status, priority, supportType, mspId,
                assignToSuperAdmin, createdBy, assignCategory, search, null, null);
    }

    @Override
    public long countSupportTicketsWithFilters(
            String clientId,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        Query query = buildQueryCriteria(clientId, null, userId, parentTicketId, assignedTo,
                status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, fromDate, toDate);

        return mongoTemplate.count(query, SupportTicket.class, COLLECTION_NAME);
    }

    @Override
    public Page<SupportTicket> findSupportTicketsWithFilters(
            List<String> clientIds,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate,
            Pageable pageable) {
        Query query = buildQueryCriteria(null, clientIds, userId, parentTicketId, assignedTo,
                status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, fromDate, toDate);

        query.with(pageable);

        List<SupportTicket> tickets = mongoTemplate.find(query, SupportTicket.class, COLLECTION_NAME);

        return PageableExecutionUtils.getPage(tickets, pageable, () -> {
            Query countQuery = buildQueryCriteria(null, clientIds, userId, parentTicketId, assignedTo,
                    status, priority, supportType, mspId, assignToSuperAdmin, createdBy, assignCategory, search, fromDate, toDate);
            return mongoTemplate.count(countQuery, SupportTicket.class, COLLECTION_NAME);
        });
    }

    /**
     * Build query criteria based on provided filters
     */
    private Query buildQueryCriteria(
            String clientId,
            List<String> clientIds,
            String userId,
            String parentTicketId,
            String assignedTo,
            TicketStatus status,
            Priority priority,
            String supportType,
            String mspId,
            Boolean assignToSuperAdmin,
            String createdBy,
            AssignCategory assignCategory,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {

        List<Criteria> criteriaList = new ArrayList<>();

        // Exact match filters
        if (StringUtils.hasText(clientId)) {
            criteriaList.add(Criteria.where("clientId").is(clientId));
        } else if (clientIds != null && !clientIds.isEmpty()) {
            criteriaList.add(Criteria.where("clientId").in(clientIds));
        }

        if (StringUtils.hasText(userId)) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (StringUtils.hasText(parentTicketId)) {
            criteriaList.add(Criteria.where("parentTicketId").is(parentTicketId));
        }

        if (StringUtils.hasText(assignedTo)) {
            criteriaList.add(Criteria.where("assignedTo").is(assignedTo));
        }

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (priority != null) {
            criteriaList.add(Criteria.where("priority").is(priority));
        }

        if (supportType != null) {
            criteriaList.add(Criteria.where("supportType").is(supportType));
        }

        if (StringUtils.hasText(mspId)) {
            criteriaList.add(Criteria.where("mspId").is(mspId));
        }

        if (assignToSuperAdmin != null) {
            criteriaList.add(Criteria.where("assignToSuperAdmin").is(assignToSuperAdmin));
        }

        if (StringUtils.hasText(createdBy)) {
            criteriaList.add(Criteria.where("createdBy").is(createdBy));
        }

        if (assignCategory != null) {
            criteriaList.add(Criteria.where("assignCategory").is(assignCategory));
        }

        if (fromDate != null || toDate != null) {
            Criteria createdDateCriteria = Criteria.where("createdDate");
            if (fromDate != null) {
                createdDateCriteria = createdDateCriteria.gte(fromDate);
            }
            if (toDate != null) {
                createdDateCriteria = createdDateCriteria.lte(toDate);
            }
            criteriaList.add(createdDateCriteria);
        }

        // Search filter (case-insensitive search in title and description)
        if (StringUtils.hasText(search)) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(search, "i"),
                    Criteria.where("description").regex(search, "i"),
                    Criteria.where("ticketId").regex(search, "i")
            );
            criteriaList.add(searchCriteria);
        }

        // Combine all criteria
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            if (criteriaList.size() == 1) {
                query.addCriteria(criteriaList.get(0));
            } else {
                // Use $and operator to combine all criteria
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        return query;
    }

    @Override
    public long countByCreatedDateBetween(java.time.Instant startOfDay, java.time.Instant startOfNextDay) {
        Query query = new Query();
        query.addCriteria(Criteria.where("createdDate").gte(startOfDay).lt(startOfNextDay));
        return mongoTemplate.count(query, SupportTicket.class, COLLECTION_NAME);
    }

    @Override
    public Map<String, Long> aggregateReportMetrics(
            String clientId,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        return aggregateReportMetricsInternal(clientId, null, search, fromDate, toDate);
    }

    @Override
    public Map<String, Long> aggregateReportMetrics(
            List<String> clientIds,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        return aggregateReportMetricsInternal(null, clientIds, search, fromDate, toDate);
    }

    private Map<String, Long> aggregateReportMetricsInternal(
            String clientId,
            List<String> clientIds,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        Query baseQuery = buildQueryCriteria(
                clientId, clientIds, null, null, null, null, null, null, null, null, null, null, search, fromDate, toDate);

        List<AggregationOperation> operations = new ArrayList<>();
        org.bson.Document queryObject = baseQuery.getQueryObject();
        if (queryObject != null && !queryObject.isEmpty()) {
            operations.add(context -> new org.bson.Document("$match", queryObject));
        }

        operations.add(context -> new org.bson.Document("$group", new org.bson.Document("_id", null)
                .append("totalTickets", new org.bson.Document("$sum", 1))
                .append("openTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "OPEN")), 1, 0))))
                .append("inProgressTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "IN_PROGRESS")), 1, 0))))
                .append("closedTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "CLOSED")), 1, 0))))
                .append("highPriorityTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$priority", "HIGH")), 1, 0))))
        ));

        org.bson.Document result = mongoTemplate
                .aggregate(Aggregation.newAggregation(operations), COLLECTION_NAME, org.bson.Document.class)
                .getUniqueMappedResult();

        Map<String, Long> metrics = new HashMap<>();
        if (result == null) {
            metrics.put("totalTickets", 0L);
            metrics.put("openTickets", 0L);
            metrics.put("inProgressTickets", 0L);
            metrics.put("closedTickets", 0L);
            metrics.put("highPriorityTickets", 0L);
            return metrics;
        }

        metrics.put("totalTickets", ((Number) result.getOrDefault("totalTickets", 0)).longValue());
        metrics.put("openTickets", ((Number) result.getOrDefault("openTickets", 0)).longValue());
        metrics.put("inProgressTickets", ((Number) result.getOrDefault("inProgressTickets", 0)).longValue());
        metrics.put("closedTickets", ((Number) result.getOrDefault("closedTickets", 0)).longValue());
        metrics.put("highPriorityTickets", ((Number) result.getOrDefault("highPriorityTickets", 0)).longValue());
        return metrics;
    }

    @Override
    public List<SupportTicket> findSupportTicketsForReportExport(
            String clientId,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        return findSupportTicketsForReportExportInternal(clientId, null, search, fromDate, toDate);
    }

    @Override
    public List<SupportTicket> findSupportTicketsForReportExport(
            List<String> clientIds,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        return findSupportTicketsForReportExportInternal(null, clientIds, search, fromDate, toDate);
    }

    private List<SupportTicket> findSupportTicketsForReportExportInternal(
            String clientId,
            List<String> clientIds,
            String search,
            java.time.Instant fromDate,
            java.time.Instant toDate) {
        Query query = buildQueryCriteria(
                clientId, clientIds, null, null, null, null, null, null, null, null, null, null, search, fromDate, toDate);
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdDate"));
        return mongoTemplate.find(query, SupportTicket.class, COLLECTION_NAME);
    }

    @Override
    public List<SupportResolutionTimeTypeAggregate> aggregateResolutionTimeBySupportType(String clientId) {
        Query baseQuery = buildQueryCriteria(
                clientId, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        List<AggregationOperation> operations = new ArrayList<>();
        org.bson.Document queryObject = baseQuery.getQueryObject();
        if (queryObject != null && !queryObject.isEmpty()) {
            operations.add(context -> new org.bson.Document("$match", queryObject));
        }

        operations.add(context -> new org.bson.Document("$group", new org.bson.Document("_id", "$supportType")
                .append("totalTickets", new org.bson.Document("$sum", 1))
                .append("openTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "OPEN")), 1, 0))))
                .append("inProgressTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "IN_PROGRESS")), 1, 0))))
                .append("closedTickets", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "CLOSED")), 1, 0))))
                .append("totalClosedResolutionMs", new org.bson.Document("$sum",
                        new org.bson.Document("$cond", List.of(
                                new org.bson.Document("$eq", List.of("$status", "CLOSED")),
                                new org.bson.Document("$subtract", List.of("$updatedDate", "$createdDate")),
                                0))))
        ));

        operations.add(context -> new org.bson.Document("$sort", new org.bson.Document("totalTickets", -1)));

        List<org.bson.Document> results = mongoTemplate
                .aggregate(Aggregation.newAggregation(operations), COLLECTION_NAME, org.bson.Document.class)
                .getMappedResults();

        List<SupportResolutionTimeTypeAggregate> aggregates = new ArrayList<>();
        for (org.bson.Document result : results) {
            Object id = result.get("_id");
            aggregates.add(SupportResolutionTimeTypeAggregate.builder()
                    .supportTypeId(id != null ? id.toString() : null)
                    .totalTickets(((Number) result.getOrDefault("totalTickets", 0)).longValue())
                    .openTickets(((Number) result.getOrDefault("openTickets", 0)).longValue())
                    .closedTickets(((Number) result.getOrDefault("closedTickets", 0)).longValue())
                    .inProgressTickets(((Number) result.getOrDefault("inProgressTickets", 0)).longValue())
                    .totalClosedResolutionMs(((Number) result.getOrDefault("totalClosedResolutionMs", 0)).longValue())
                    .build());
        }
        return aggregates;
    }
}

