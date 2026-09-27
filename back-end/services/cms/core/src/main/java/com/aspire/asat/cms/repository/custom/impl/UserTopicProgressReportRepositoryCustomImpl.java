package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.reports.TopicAssignmentProgressRowDTO;
import com.aspire.asat.cms.dto.reports.TopicAssignmentSummaryDTO;
import com.aspire.asat.cms.model.UserTopicProgress;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.custom.UserTopicProgressReportRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class UserTopicProgressReportRepositoryCustomImpl implements UserTopicProgressReportRepositoryCustom {

    private static final String COMPLETED = "COMPLETED";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<String> findTopicIdsByNameSearch(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        Query query = new Query(Criteria.where("topicName").regex(Pattern.quote(search.trim()), "i"));
        query.fields().include("_id");
        return mongoTemplate.find(query, Topic.class).stream()
                .map(Topic::getId)
                .toList();
    }

    @Override
    public TopicAssignmentSummaryDTO aggregateSummaryMetrics(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> topicIdsFilter) {

        if (topicIdsFilter != null && topicIdsFilter.isEmpty()) {
            return emptySummary();
        }

        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(scopeLookup(clientAdminId, clientAdminIds));
        ops.add(Aggregation.match(Criteria.where("scopeMatch.0").exists(true)));
        ops.addAll(assignmentMatchOps(start, end, null, topicIdsFilter));
        ops.add(Aggregation.group()
                .count().as("assignedToUsers")
                .addToSet("topicId").as("topicIds")
                .sum(ConditionalOperators.when(Criteria.where("status").is(COMPLETED)).then(1).otherwise(0))
                .as("completedAssignments")
                .avg("progress").as("avgProgress"));
        ops.add(Aggregation.project("assignedToUsers", "completedAssignments", "avgProgress")
                .and("topicIds").size().as("assignedTopics"));

        AggregationResults<Document> results =
                mongoTemplate.aggregate(Aggregation.newAggregation(ops), UserTopicProgress.class, Document.class);
        Document doc = results.getUniqueMappedResult();
        if (doc == null) {
            return emptySummary();
        }

        long assignedTopics = numberLong(doc.get("assignedTopics"));
        long assignedToUsers = numberLong(doc.get("assignedToUsers"));
        long completedAssignments = numberLong(doc.get("completedAssignments"));
        Double avgProgress = doc.get("avgProgress") != null
                ? ((Number) doc.get("avgProgress")).doubleValue()
                : 0.0;

        return TopicAssignmentSummaryDTO.builder()
                .assignedTopics(assignedTopics)
                .assignedToUsers(assignedToUsers)
                .completedAssignments(completedAssignments)
                .avgProgress(avgProgress)
                .build();
    }

    @Override
    public List<TopicAssignmentProgressRowDTO> aggregateTopicProgressPage(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter,
            int skip,
            int limit) {

        if (topicIdsFilter != null && topicIdsFilter.isEmpty()) {
            return List.of();
        }

        List<AggregationOperation> ops = buildTopicProgressOps(
                clientAdminId, clientAdminIds, start, end, statuses, topicIdsFilter);
        ops.add(Aggregation.sort(Sort.by(Sort.Order.desc("assignedCount"), Sort.Order.asc("topicName"))));
        ops.add(Aggregation.skip((long) Math.max(skip, 0)));
        ops.add(Aggregation.limit(Math.max(limit, 1)));

        return mapProgressRows(mongoTemplate.aggregate(
                Aggregation.newAggregation(ops), UserTopicProgress.class, Document.class));
    }

    @Override
    public long countTopicProgressGroups(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter) {

        if (topicIdsFilter != null && topicIdsFilter.isEmpty()) {
            return 0L;
        }

        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(scopeLookup(clientAdminId, clientAdminIds));
        ops.add(Aggregation.match(Criteria.where("scopeMatch.0").exists(true)));
        ops.addAll(assignmentMatchOps(start, end, statuses, topicIdsFilter));
        ops.add(Aggregation.group("topicId"));
        ops.add(Aggregation.count().as("count"));

        AggregationResults<Document> results =
                mongoTemplate.aggregate(Aggregation.newAggregation(ops), UserTopicProgress.class, Document.class);
        Document doc = results.getUniqueMappedResult();
        return doc == null ? 0L : numberLong(doc.get("count"));
    }

    @Override
    public List<TopicAssignmentProgressRowDTO> aggregateTopicProgressForExport(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter) {

        if (topicIdsFilter != null && topicIdsFilter.isEmpty()) {
            return List.of();
        }

        List<AggregationOperation> ops = buildTopicProgressOps(
                clientAdminId, clientAdminIds, start, end, statuses, topicIdsFilter);
        ops.add(Aggregation.sort(Sort.by(Sort.Order.desc("assignedCount"), Sort.Order.asc("topicName"))));

        return mapProgressRows(mongoTemplate.aggregate(
                Aggregation.newAggregation(ops), UserTopicProgress.class, Document.class));
    }

    private List<AggregationOperation> buildTopicProgressOps(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter) {

        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(scopeLookup(clientAdminId, clientAdminIds));
        ops.add(Aggregation.match(Criteria.where("scopeMatch.0").exists(true)));
        ops.addAll(assignmentMatchOps(start, end, statuses, topicIdsFilter));
        ops.add(Aggregation.group("topicId")
                .count().as("assignedCount")
                .sum(ConditionalOperators.when(Criteria.where("status").is(COMPLETED)).then(1).otherwise(0))
                .as("completedCount")
                .avg("progress").as("avgProgress"));
        ops.add(Aggregation.lookup("topic", "_id", "_id", "topicDoc"));
        ops.add(Aggregation.project()
                .and("_id").as("topicId")
                .and("assignedCount").as("assignedCount")
                .and("completedCount").as("completedCount")
                .and("avgProgress").as("avgProgress")
                .and("topicDoc.topicName").arrayElementAt(0).as("topicName"));
        return ops;
    }

    private AggregationOperation scopeLookup(String clientAdminId, List<String> clientAdminIds) {
        List<Document> lookupPipeline = new ArrayList<>();
        List<Document> matchExprAnd = new ArrayList<>();
        matchExprAnd.add(new Document("$eq", List.of("$userId", "$$uid")));
        matchExprAnd.add(new Document("$eq", List.of("$subPackageId", "$$spid")));

        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            matchExprAnd.add(new Document("$in", List.of("$clientAdminId", clientAdminIds)));
        } else if (StringUtils.hasText(clientAdminId)) {
            matchExprAnd.add(new Document("$eq", List.of("$clientAdminId", clientAdminId)));
        }

        lookupPipeline.add(new Document("$match", new Document("$expr", new Document("$and", matchExprAnd))));
        lookupPipeline.add(new Document("$limit", 1));

        Document lookup = new Document("$lookup", new Document()
                .append("from", "user_subpackages")
                .append("let", new Document("uid", "$userId").append("spid", "$subPackageId"))
                .append("pipeline", lookupPipeline)
                .append("as", "scopeMatch"));

        return context -> lookup;
    }

    private List<AggregationOperation> assignmentMatchOps(
            Instant start,
            Instant end,
            List<String> statuses,
            List<String> topicIdsFilter) {

        List<Criteria> criteriaList = new ArrayList<>();
        if (start != null) {
            criteriaList.add(Criteria.where("createdAt").gte(start));
        }
        if (end != null) {
            criteriaList.add(Criteria.where("createdAt").lte(end));
        }
        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                criteriaList.add(Criteria.where("status").is(statuses.get(0)));
            } else {
                criteriaList.add(Criteria.where("status").in(statuses));
            }
        }
        if (topicIdsFilter != null) {
            criteriaList.add(Criteria.where("topicId").in(topicIdsFilter));
        }

        if (criteriaList.isEmpty()) {
            return List.of();
        }
        return List.of(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
    }

    private List<TopicAssignmentProgressRowDTO> mapProgressRows(AggregationResults<Document> results) {
        List<TopicAssignmentProgressRowDTO> rows = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            long assigned = numberLong(doc.get("assignedCount"));
            long completed = numberLong(doc.get("completedCount"));
            double avgProgress = doc.get("avgProgress") != null
                    ? ((Number) doc.get("avgProgress")).doubleValue()
                    : 0.0;
            double completionRate = assigned > 0 ? (completed * 100.0) / assigned : 0.0;
            Object topicNameObj = doc.get("topicName");
            rows.add(TopicAssignmentProgressRowDTO.builder()
                    .topicId(doc.get("topicId") != null ? doc.get("topicId").toString() : null)
                    .topicName(topicNameObj != null ? topicNameObj.toString() : null)
                    .assignedCount(assigned)
                    .completedCount(completed)
                    .avgProgress(avgProgress)
                    .completionRate(completionRate)
                    .build());
        }
        return rows;
    }

    private static TopicAssignmentSummaryDTO emptySummary() {
        return TopicAssignmentSummaryDTO.builder()
                .assignedTopics(0L)
                .assignedToUsers(0L)
                .completedAssignments(0L)
                .avgProgress(0.0)
                .build();
    }

    private static long numberLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
