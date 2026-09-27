package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.custom.DeepfakeRenderJobRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class DeepfakeRenderJobRepositoryCustomImpl implements DeepfakeRenderJobRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<DeepfakeRenderJob> findWithFilters(
            String clientId,
            Instant uploadedFrom,
            Instant uploadedTo,
            String title,
            String description,
            Pageable pageable) {
        Query query = buildQuery(clientId, uploadedFrom, uploadedTo, title, description);
        query.with(pageable);
        return mongoTemplate.find(query, DeepfakeRenderJob.class);
    }

    @Override
    public long countWithFilters(
            String clientId,
            Instant uploadedFrom,
            Instant uploadedTo,
            String title,
            String description) {
        Query query = buildQuery(clientId, uploadedFrom, uploadedTo, title, description);
        return mongoTemplate.count(query, DeepfakeRenderJob.class);
    }

    private Query buildQuery(
            String clientId,
            Instant uploadedFrom,
            Instant uploadedTo,
            String title,
            String description) {

        List<Criteria> andCriteria = new ArrayList<>();

        andCriteria.add(Criteria.where("isDeleted").ne(true));

        if (clientId != null && !clientId.trim().isEmpty()) {
            andCriteria.add(Criteria.where("clientId").is(clientId.trim()));
        }

        if (uploadedFrom != null || uploadedTo != null) {
            Criteria createdAt = Criteria.where("createdAt");
            if (uploadedFrom != null) {
                createdAt = createdAt.gte(uploadedFrom);
            }
            if (uploadedTo != null) {
                createdAt = createdAt.lt(uploadedTo);
            }
            andCriteria.add(createdAt);
        }

        if (title != null && !title.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(Pattern.quote(title.trim()), Pattern.CASE_INSENSITIVE);
            andCriteria.add(Criteria.where("title").regex(pattern));
        }

        if (description != null && !description.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(Pattern.quote(description.trim()), Pattern.CASE_INSENSITIVE);
            andCriteria.add(Criteria.where("description").regex(pattern));
        }

        Criteria finalCriteria = andCriteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));

        return new Query(finalCriteria);
    }
}
