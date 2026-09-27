package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.repository.custom.DomainRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom repository implementation for Domain filtering.
 */
@Repository
@RequiredArgsConstructor
public class DomainRepositoryCustomImpl implements DomainRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Domain> findWithFilters(String clientId, String search, List<DomainStatus> statuses,
                                        Pageable pageable) {
        Query query = buildQuery(clientId, search, statuses, pageable);
        return mongoTemplate.find(query, Domain.class);
    }

    @Override
    public long countWithFilters(String clientId, String search, List<DomainStatus> statuses) {
        Query query = buildQuery(clientId, search, statuses, null);
        return mongoTemplate.count(query, Domain.class);
    }

    private Query buildQuery(String clientId, String search, List<DomainStatus> statuses, Pageable pageable) {
        List<Criteria> andCriteria = new ArrayList<>();

        andCriteria.add(new Criteria().orOperator(
                Criteria.where("clientId").is(clientId),
                Criteria.where("isGlobal").is(true)
        ));

        if (StringUtils.hasText(search)) {
            Pattern pattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            andCriteria.add(Criteria.where("domain").regex(pattern));
        }

        if (statuses != null && !statuses.isEmpty()) {
            andCriteria.add(Criteria.where("status").in(statuses));
        }

        Criteria criteria = new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(criteria);
        if (pageable != null) {
            query.with(pageable);
        }
        return query;
    }
}
