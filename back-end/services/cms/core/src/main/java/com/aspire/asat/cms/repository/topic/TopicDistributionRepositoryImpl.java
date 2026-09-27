package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.TopicDistributionItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Efficient implementation of topic distribution using MongoDB aggregation pipelines.
 * This replaces the inefficient findAll() + Java filtering approach.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TopicDistributionRepositoryImpl implements TopicDistributionRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    
    // Month names for response
    private static final String[] MONTH_NAMES = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    @Override
    public List<TopicDistributionItemDto> getTopicDistribution(Instant yearStart, Instant yearEnd) {
        log.info("Calculating topic distribution using aggregation for year range: {} to {}", yearStart, yearEnd);

        // Initialize result map for all 12 months
        Map<Integer, TopicDistributionItemDto> monthDataMap = initializeMonthMap();

        // Calculate totalContent using aggregation
        calculateTotalContent(yearStart, yearEnd, monthDataMap);

        // Calculate usedContent using aggregation
        calculateUsedContent(yearStart, yearEnd, monthDataMap);

        // Convert map to list ordered by month (1-12)
        return IntStream.rangeClosed(1, 12)
                .mapToObj(monthDataMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Initialize month map with all 12 months set to zero
     */
    private Map<Integer, TopicDistributionItemDto> initializeMonthMap() {
        Map<Integer, TopicDistributionItemDto> map = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            map.put(i + 1, TopicDistributionItemDto.builder()
                    .month(MONTH_NAMES[i])
                    .totalContent(0L)
                    .usedContent(0L)
                    .build());
        }
        return map;
    }

    /**
     * Calculate totalContent: Count ENABLED topics created in each month using aggregation
     */
    private void calculateTotalContent(Instant yearStart, Instant yearEnd, 
                                      Map<Integer, TopicDistributionItemDto> monthDataMap) {
        log.debug("Calculating totalContent using aggregation");

        // Match: Filter ENABLED topics created within year range
        MatchOperation match = Aggregation.match(
                Criteria.where("status").is(TopicStatus.ENABLED)
                        .and("createdAt").gte(yearStart).lte(yearEnd)
        );

        // Project: Extract month from createdAt (MongoDB $month returns 1-12)
        ProjectionOperation project = Aggregation.project()
                .andExpression("month(createdAt)").as("month")
                .and("_id").as("topicId");

        // Group: Count topics by month
        GroupOperation group = Aggregation.group("month")
                .count().as("count");

        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(match, project, group);
        
        List<Document> results = mongoTemplate.aggregate(aggregation, "topic", Document.class)
                .getMappedResults();

        // Update monthDataMap with results
        for (Document doc : results) {
            Integer month = doc.getInteger("_id"); // month value from group
            // MongoDB $count returns Integer, convert to Long
            Number countNumber = (Number) doc.get("count");
            Long count = countNumber != null ? countNumber.longValue() : null;
            
            if (month != null && month >= 1 && month <= 12 && count != null) {
                TopicDistributionItemDto item = monthDataMap.get(month);
                if (item != null) {
                    item.setTotalContent(count);
                    log.debug("Month {}: totalContent = {}", month, count);
                }
            }
        }
    }

    /**
     * Calculate usedContent: Count unique topics used in subpackages created in each month
     * This uses a more complex aggregation to:
     * 1. Match subpackages created within year range
     * 2. Unwind topicId array
     * 3. Group by month and collect unique topic IDs
     * 4. Count unique topics per month
     */
    private void calculateUsedContent(Instant yearStart, Instant yearEnd,
                                     Map<Integer, TopicDistributionItemDto> monthDataMap) {
        log.debug("Calculating usedContent using aggregation");

        // Match: Filter subpackages created within year range with non-empty topicId
        MatchOperation match = Aggregation.match(
                Criteria.where("createdAt").gte(yearStart).lte(yearEnd)
                        .and("topicId").exists(true).ne(Collections.emptyList())
        );

        // Project: Extract month and topicId array
        ProjectionOperation project = Aggregation.project()
                .andExpression("month(createdAt)").as("month")
                .and("topicId").as("topicIds");

        // Unwind: Expand topicId array into individual documents
        UnwindOperation unwind = Aggregation.unwind("topicIds");

        // Group: Group by month and collect unique topic IDs
        GroupOperation group = Aggregation.group("month")
                .addToSet("topicIds").as("uniqueTopicIds");

        // Project: Count unique topics per month
        ProjectionOperation projectCount = Aggregation.project()
                .and("_id").as("month")
                .andExpression("size(uniqueTopicIds)").as("count");

        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(match, project, unwind, group, projectCount);
        
        List<Document> results = mongoTemplate.aggregate(aggregation, "sub_packages", Document.class)
                .getMappedResults();

        // Update monthDataMap with results
        for (Document doc : results) {
            Integer month = doc.getInteger("month");
            // MongoDB $size returns Integer, convert to Long
            Number countNumber = (Number) doc.get("count");
            Long count = countNumber != null ? countNumber.longValue() : null;
            
            if (month != null && month >= 1 && month <= 12 && count != null) {
                TopicDistributionItemDto item = monthDataMap.get(month);
                if (item != null) {
                    item.setUsedContent(count);
                    log.debug("Month {}: usedContent = {}", month, count);
                }
            }
        }
    }
}

