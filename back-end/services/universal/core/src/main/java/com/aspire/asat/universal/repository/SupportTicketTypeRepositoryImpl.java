package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.SupportTicketType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class SupportTicketTypeRepositoryImpl implements SupportTicketTypeRepositoryCustom {

    private final MongoTemplate mongoTemplate;


    @Override
    public Page<SupportTicketType> findAllWithFilters(String search, Boolean active, Pageable pageable) {
        Query query = new Query();

        // Add search filter for name field (case-insensitive regex search)
        // Only apply if search is not null and not empty
        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
        }

        // Add active filter only if active is not null
        if (active != null) {
            query.addCriteria(Criteria.where("active").is(active));
        }

        // Get total count for pagination
        long total = mongoTemplate.count(query, SupportTicketType.class);

        // Apply pagination
        query.with(pageable);
        List<SupportTicketType> results = mongoTemplate.find(query, SupportTicketType.class);

        return new PageImpl<>(results, pageable, total);
    }
}

