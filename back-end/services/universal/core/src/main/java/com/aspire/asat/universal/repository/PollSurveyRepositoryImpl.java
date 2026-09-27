package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurvey;
import com.aspire.asat.universal.enums.PollSurveyStatus;
import com.aspire.asat.universal.enums.PollSurveyType;
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

import java.util.List;
import java.util.regex.Pattern;

@Repository
public class PollSurveyRepositoryImpl implements PollSurveyRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(PollSurveyRepositoryImpl.class);
    private final MongoTemplate mongoTemplate;

    public PollSurveyRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<PollSurvey> findAllWithFilters(String search, String type, String status, Pageable pageable) {
        log.debug("Finding all polls with search: {}, type: {}, status: {}", search, type, status);

        Query query = new Query();
        addFilters(query, search, type, status);

        long total = mongoTemplate.count(query, PollSurvey.class);

        // Apply sorting by createdAt DESC and pagination
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);

        List<PollSurvey> results = mongoTemplate.find(query, PollSurvey.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public long countWithFilters(String search, String type, String status) {
        Query query = new Query();
        addFilters(query, search, type, status);
        return mongoTemplate.count(query, PollSurvey.class);
    }

    private void addFilters(Query query, String search, String type, String status) {
        // Search by title (case-insensitive)
        if (search != null && !search.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(search.trim(), Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("title").regex(pattern));
            log.debug("Applied search filter: {}", search);
        }

        // Filter by type
        if (type != null && !type.trim().isEmpty()) {
            try {
                PollSurveyType pollType = PollSurveyType.valueOf(type.trim().toUpperCase());
                query.addCriteria(Criteria.where("type").is(pollType));
                log.debug("Applied type filter: {}", pollType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid type provided: {}, ignoring filter", type);
            }
        }

        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            try {
                PollSurveyStatus pollStatus = PollSurveyStatus.valueOf(status.trim().toUpperCase());
                query.addCriteria(Criteria.where("status").is(pollStatus));
                log.debug("Applied status filter: {}", pollStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}, ignoring filter", status);
            }
        }
    }
}

