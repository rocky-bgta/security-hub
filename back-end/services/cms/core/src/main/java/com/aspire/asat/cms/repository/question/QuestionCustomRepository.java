package com.aspire.asat.cms.repository.question;

import com.aspire.asat.cms.dto.question.QuestionAggregationResult;
import com.aspire.asat.cms.dto.question.QuestionCountByTopicProductDto;
import com.aspire.asat.cms.dto.question.QuestionStatus;
import com.aspire.asat.cms.dto.question.TopicSummaryDto;
import com.aspire.asat.cms.model.question.Question;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class QuestionCustomRepository {

    private final MongoTemplate mongoTemplate;
    private final TopicRepository topicRepository;

    /**
     * Find questions with filters using MongoTemplate and Criteria
     */
    public Page<Question> findQuestionsWithFilters(String topicId, String search,
                                                   QuestionStatus status, Pageable pageable) {
        log.info("Finding questions with filters - topicId: {}, search: {}, status: {}",
                topicId, search, status);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Mandatory topic ID filter
        if (StringUtils.hasText(topicId)) {
            criteriaList.add(Criteria.where("topicId").is(topicId));
            log.info("Applied topic ID filter: {}", topicId);
        }

        // Text search filter
        if (StringUtils.hasText(search)) {
            String searchPattern = ".*" + search + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("questionText").regex(searchPattern, "i")
            ));
            log.info("Applied text search filter: {}", search);
        }

        if(status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        } else {
            criteriaList.add(Criteria.where("status").in(QuestionStatus.ACTIVE, QuestionStatus.INACTIVE));
        }

        // Apply all criteria
        query.addCriteria(new Criteria().andOperator(
                criteriaList.toArray(new Criteria[0])
        ));

        // Apply pagination
        query.with(pageable);

        // Get total count before applying pagination
        long total = mongoTemplate.count(query, Question.class);
        log.info("Total questions found: {}", total);

        // Execute query
        List<Question> questions = mongoTemplate.find(query, Question.class);
        log.info("Questions returned after pagination: {}", questions.size());

        return new PageImpl<>(questions, pageable, total);
    }

    /**
     * Count questions with filters using MongoTemplate and Criteria
     */
    public long countQuestionsWithFilters(String topicId, String search, QuestionStatus status) {
        log.info("Counting questions with filters - topicId: {}, search: {}, status: {}",
                topicId, search, status);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Mandatory topic ID filter
        if (StringUtils.hasText(topicId)) {
            criteriaList.add(Criteria.where("topicId").is(topicId));
        }

        // Text search filter
        if (StringUtils.hasText(search)) {
            String searchPattern = ".*" + search + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("questionText").regex(searchPattern, "i")
            ));
        }

        if(status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        } else {
            criteriaList.add(Criteria.where("status").in(QuestionStatus.ACTIVE, QuestionStatus.INACTIVE));
        }

        // Apply all criteria
        query.addCriteria(new Criteria().andOperator(
                criteriaList.toArray(new Criteria[0])
        ));

        return mongoTemplate.count(query, Question.class);
    }

    /**
     * Get question count grouped by topic using aggregation
     */
    public List<QuestionCountByTopicProductDto> getQuestionCountByTopicAndProduct() {
        log.info("Getting question count grouped by topic");

        // Using aggregation pipeline to group by topicId
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(QuestionStatus.ACTIVE)),
                Aggregation.match(Criteria.where("topicId").exists(true)),
                Aggregation.match(Criteria.where("topicId").ne(null)),
                Aggregation.match(Criteria.where("topicId").ne("")),
                Aggregation.group("topicId")
                        .count().as("questionCount"),
                Aggregation.project("questionCount")
                        .and("_id").as("topicId")
                        .and("questionCount").as("questionCount"),
                Aggregation.sort(Sort.Direction.ASC, "topicId")
        );

        AggregationResults<QuestionAggregationResult> results = mongoTemplate.aggregate(
                aggregation, Question.class, QuestionAggregationResult.class);

        List<QuestionAggregationResult> aggregationResults = results.getMappedResults();
        log.info("Found {} topics with questions", aggregationResults.size());

        // Convert aggregation results to full DTOs with topic details
        List<QuestionCountByTopicProductDto> counts = new ArrayList<>();

        if (aggregationResults.isEmpty()) {
            log.info("No topics with questions found");
            return counts;
        }

        for (QuestionAggregationResult result : aggregationResults) {
            log.debug("Processing aggregation result: topicId={}, questionCount={}",
                    result.getTopicId(), result.getQuestionCount());

            // Skip if topicId is null
            if (result.getTopicId() == null) {
                log.warn("Skipping aggregation result with null topicId");
                continue;
            }

            // Fetch topic details
            Topic topic = topicRepository.findById(result.getTopicId()).orElse(null);
            TopicSummaryDto topicDto = null;
            if (topic != null) {
                topicDto = TopicSummaryDto.builder()
                        .topicId(topic.getId())
                        .topicName(topic.getTopicName())
                        .durationMinutes(topic.getDurationMinutes())
                        .description(topic.getDescription())
                        .build();
            }

            // Count active and inactive questions for this topic
            Long activeCount = mongoTemplate.count(
                    Query.query(Criteria.where("topicId").is(result.getTopicId())
                            .and("status").is(QuestionStatus.ACTIVE)),
                    Question.class);

            Long inactiveCount = mongoTemplate.count(
                    Query.query(Criteria.where("topicId").is(result.getTopicId())
                            .and("status").is(QuestionStatus.INACTIVE)),
                    Question.class);

            QuestionCountByTopicProductDto dto = QuestionCountByTopicProductDto.builder()
                    .topic(topicDto)
                    .questionCount(result.getQuestionCount())
                    .activeQuestionCount(activeCount)
                    .inactiveQuestionCount(inactiveCount)
                    .topicCreatedAt(topic != null ? topic.getCreatedAt() : null)
                    .topicUpdatedAt(topic != null ? topic.getUpdatedAt() : null)
                    .build();

            counts.add(dto);
        }

        return counts;
    }

    public List<Question> findAllById(List<String> questionIds) {
        return mongoTemplate.find(Query.query(Criteria.where("id").in(questionIds)), Question.class);
    }


    public List<Question> findAllByTopicIdAndStatus(String topicId, QuestionStatus status) {
        return mongoTemplate.find(Query.query(Criteria.where("topicId").is(topicId)
                .and("status").is(status)), Question.class);
    }
}
