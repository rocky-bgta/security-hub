package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.repository.custom.ClientWindowActivityTotals;
import com.aspire.asat.phishing.repository.custom.EmailActivityRepositoryCustom;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom repository implementation for EmailActivity bulk recipient lookups.
 */
@Repository
@RequiredArgsConstructor
public class EmailActivityRepositoryCustomImpl implements EmailActivityRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<EmailActivity> findByCampaignIdsAndRecipientIds(
            List<String> campaignIds, List<String> recipientIds) {
        if (campaignIds == null || campaignIds.isEmpty() || recipientIds == null || recipientIds.isEmpty()) {
            return List.of();
        }

        Query query = new Query(new Criteria().andOperator(
                Criteria.where("campaignId").in(campaignIds),
                Criteria.where("recipientId").in(recipientIds)
        ));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(query, EmailActivity.class);
    }

    @Override
    public Page<EmailActivity> findEmailActivityLog(
            String clientId,
            ActivityType activityType,
            Instant startTime,
            Instant endTime,
            String search,
            Collection<String> campaignIds,
            Pageable pageable) {
        if (campaignIds != null && campaignIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        Criteria criteria = buildEmailActivityLogCriteria(
                clientId, activityType, startTime, endTime, search, campaignIds);
        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, EmailActivity.class);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.with(pageable);
        List<EmailActivity> content = mongoTemplate.find(query, EmailActivity.class);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countEmailActivityLog(
            String clientId,
            ActivityType activityType,
            Instant startTime,
            Instant endTime,
            String search,
            Collection<String> campaignIds) {
        if (campaignIds != null && campaignIds.isEmpty()) {
            return 0L;
        }
        Criteria criteria = buildEmailActivityLogCriteria(
                clientId, activityType, startTime, endTime, search, campaignIds);
        return mongoTemplate.count(new Query(criteria), EmailActivity.class);
    }

    @Override
    public List<RecipientWindowActivityMetrics> aggregateRecipientMetricsForWindow(
            String clientId,
            Instant startInclusive,
            Instant endExclusive,
            Collection<String> campaignIds,
            DashboardChannelScope.ActivityMapping activityMapping) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }
        if (campaignIds == null || campaignIds.isEmpty()) {
            return List.of();
        }
        DashboardChannelScope.ActivityMapping mapping = activityMapping != null
                ? activityMapping
                : DashboardChannelScope.activityMapping(null);

        Criteria match = Criteria.where("clientId").is(clientId)
                .and("campaignId").in(campaignIds)
                .and("timestamp").gte(startInclusive).lt(endExclusive)
                .and("recipientEmail").exists(true).ne(null).ne("");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.group("recipientEmail")
                        .sum(countWhen(mapping.sent())).as("delivered")
                        .sum(countWhen(mapping.opened())).as("opened")
                        .sum(countWhen(mapping.clicked())).as("clicked")
                        .sum(countWhen(mapping.hack())).as("submits")
                        .sum(countWhen(mapping.reported())).as("reported"),
                Aggregation.project("delivered", "opened", "clicked", "submits", "reported")
                        .and("_id").as("recipientEmail")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, EmailActivity.class, Document.class);

        List<RecipientWindowActivityMetrics> metrics = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            String email = doc.getString("recipientEmail");
            if (email == null || email.isBlank()) {
                continue;
            }
            metrics.add(new RecipientWindowActivityMetrics(
                    email,
                    intOrZero(doc, "delivered"),
                    intOrZero(doc, "opened"),
                    intOrZero(doc, "clicked"),
                    intOrZero(doc, "submits"),
                    intOrZero(doc, "reported")));
        }
        return metrics;
    }

    @Override
    public ClientWindowActivityTotals aggregateClientActivityTotalsForWindow(
            String clientId,
            Instant startInclusive,
            Instant endExclusive,
            Collection<String> campaignIds,
            DashboardChannelScope.ActivityMapping activityMapping) {
        if (clientId == null || clientId.isBlank()) {
            return ClientWindowActivityTotals.empty();
        }
        if (campaignIds == null || campaignIds.isEmpty()) {
            return ClientWindowActivityTotals.empty();
        }
        DashboardChannelScope.ActivityMapping mapping = activityMapping != null
                ? activityMapping
                : DashboardChannelScope.activityMapping(null);

        Criteria match = Criteria.where("clientId").is(clientId)
                .and("campaignId").in(campaignIds)
                .and("timestamp").gte(startInclusive).lt(endExclusive);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.group()
                        .sum(countWhen(mapping.sent())).as("emailsSent")
                        .sum(countWhen(mapping.hack())).as("dataSubmitted")
                        .sum(countWhen(mapping.reported())).as("emailsReported")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, EmailActivity.class, Document.class);

        List<Document> mapped = results.getMappedResults();
        if (mapped.isEmpty()) {
            return ClientWindowActivityTotals.empty();
        }

        Document doc = mapped.get(0);
        return new ClientWindowActivityTotals(
                intOrZero(doc, "emailsSent"),
                intOrZero(doc, "dataSubmitted"),
                intOrZero(doc, "emailsReported"));
    }

    private static AggregationExpression countWhen(ActivityType type) {
        if (type == null) {
            return context -> new Document("$literal", 0);
        }
        return ConditionalOperators.when(
                Criteria.where("activityType").is(type.name())).then(1).otherwise(0);
    }

    private static int intOrZero(Document doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private static Criteria buildEmailActivityLogCriteria(
            String clientId,
            ActivityType activityType,
            Instant startTime,
            Instant endTime,
            String search,
            Collection<String> campaignIds) {
        List<Criteria> andParts = new ArrayList<>();
        andParts.add(Criteria.where("clientId").is(clientId));
        if (campaignIds != null && !campaignIds.isEmpty()) {
            andParts.add(Criteria.where("campaignId").in(campaignIds));
        }

        if (activityType != null) {
            andParts.add(Criteria.where("activityType").is(activityType));
            // Legacy: list by activity type did not apply a date range.
        } else {
            Instant effectiveEnd = endTime != null ? endTime : Instant.now();
            Instant effectiveStart;
            if (startTime != null && endTime != null) {
                effectiveStart = startTime;
                effectiveEnd = endTime;
            } else {
                effectiveStart = effectiveEnd.minus(7, ChronoUnit.DAYS);
            }
            andParts.add(Criteria.where("timestamp").gte(effectiveStart).lte(effectiveEnd));
        }

        if (StringUtils.hasText(search)) {
            Pattern pattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            andParts.add(new Criteria().orOperator(
                    Criteria.where("recipientName").regex(pattern),
                    Criteria.where("recipientEmail").regex(pattern),
                    Criteria.where("campaignName").regex(pattern)));
        }
        return new Criteria().andOperator(andParts.toArray(Criteria[]::new));
    }
}
