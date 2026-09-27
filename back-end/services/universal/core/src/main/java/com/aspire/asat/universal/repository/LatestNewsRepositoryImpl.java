package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.LatestNews;
import com.aspire.asat.universal.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class LatestNewsRepositoryImpl implements LatestNewsRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public LatestNewsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<LatestNews> findAllWithFilters(String status, String categoryId, String search, Pageable pageable) {
        Query query = new Query();
        addCommonFilters(query, status, categoryId, search);
        
        long total = mongoTemplate.count(query, LatestNews.class);
        
        query.with(pageable);
        query.with(Sort.by(Sort.Direction.DESC, "publishedDate"));
        List<LatestNews> results = mongoTemplate.find(query, LatestNews.class);
        
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<LatestNews> findActiveWithFilters(String status, String categoryId, String search, 
                                                   LocalDateTime currentDate, Pageable pageable) {
        Query query = new Query();
        
        // Add active news criteria (published and not expired)
        query.addCriteria(Criteria.where("publishedDate").lte(currentDate));
        query.addCriteria(Criteria.where("expireDate").gt(currentDate));
        
        // If status is not provided, default to ACTIVE for active news endpoint
        if (status == null || status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(Status.ACTIVE));
        } else {
            try {
                Status statusEnum = Status.valueOf(status.toUpperCase());
                query.addCriteria(Criteria.where("status").is(statusEnum));
            } catch (IllegalArgumentException e) {
                // Invalid status, default to ACTIVE
                query.addCriteria(Criteria.where("status").is(Status.ACTIVE));
            }
        }
        
        // Add other filters
        if (categoryId != null && !categoryId.isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }
        
        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
        }
        
        long total = mongoTemplate.count(query, LatestNews.class);
        
        query.with(pageable);
        List<LatestNews> results = mongoTemplate.find(query, LatestNews.class);
        
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public long countAllWithFilters(String status, String categoryId, String search) {
        Query query = new Query();
        addCommonFilters(query, status, categoryId, search);
        return mongoTemplate.count(query, LatestNews.class);
    }

    @Override
    public long countActiveWithFilters(String status, String categoryId, String search, LocalDateTime currentDate) {
        Query query = new Query();
        
        // Add active news criteria
        query.addCriteria(Criteria.where("publishedDate").lte(currentDate));
        query.addCriteria(Criteria.where("expireDate").gt(currentDate));
        
        // If status is not provided, default to ACTIVE
        if (status == null || status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(Status.ACTIVE));
        } else {
            try {
                Status statusEnum = Status.valueOf(status.toUpperCase());
                query.addCriteria(Criteria.where("status").is(statusEnum));
            } catch (IllegalArgumentException e) {
                // Invalid status, default to ACTIVE
                query.addCriteria(Criteria.where("status").is(Status.ACTIVE));
            }
        }
        
        // Add other filters
        if (categoryId != null && !categoryId.isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }
        
        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
        }
        
        return mongoTemplate.count(query, LatestNews.class);
    }

    private void addCommonFilters(Query query, String status, String categoryId, String search) {
        if (status != null && !status.isEmpty()) {
            try {
                Status statusEnum = Status.valueOf(status.toUpperCase());
                query.addCriteria(Criteria.where("status").is(statusEnum));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }
        
        if (categoryId != null && !categoryId.isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(categoryId));
        }
        
        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
        }
    }
}

