package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.InAppNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom repository implementation for InAppNotification with proper offset-based pagination
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationRepositoryCustomImpl implements InAppNotificationRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<InAppNotification> findByUserIdWithPagination(String userId, String search, int offset, int pageSize) {
        log.info("Finding notifications for user: {} with search: '{}', offset: {}, pageSize: {}", userId, search, offset, pageSize);

        Query query = buildQuery(userId, null, search);
        
        // Get total count before pagination
        long total = mongoTemplate.count(query, InAppNotification.class);
        log.info("Total notifications found: {}", total);
        
        // Apply sorting by created_at descending (use MongoDB field name)
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        
        // Convert offset (page number) to actual skip count for pagination
        // offset=0 → page 0 → skip 0, take pageSize
        // offset=1 → page 1 → skip pageSize, take pageSize
        // offset=2 → page 2 → skip 2*pageSize, take pageSize
        int skip = offset * pageSize;
        
        // Apply offset-based pagination
        query.skip(skip);
        query.limit(pageSize);
        
        log.info("Executing query with skip: {} (offset {} * pageSize {}), limit: {}", skip, offset, pageSize, pageSize);
        List<InAppNotification> notifications = mongoTemplate.find(query, InAppNotification.class);
        
        log.info("Found {} notifications out of {} total (offset: {}, pageSize: {})", notifications.size(), total, offset, pageSize);
        
        // Create Pageable for PageImpl (needed for proper Page object)
        Pageable pageable = PageRequest.of(offset, pageSize);
        
        return new PageImpl<>(notifications, pageable, total);
    }

    @Override
    public Page<InAppNotification> findUnreadByUserIdWithPagination(String userId, String search, int offset, int pageSize) {
        log.info("Finding unread notifications for user: {} with search: '{}', offset: {}, pageSize: {}", userId, search, offset, pageSize);

        Query query = buildQuery(userId, false, search);
        
        // Get total count before pagination
        long total = mongoTemplate.count(query, InAppNotification.class);
        log.info("Total unread notifications found: {}", total);
        
        // Apply sorting by created_at descending (use MongoDB field name)
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        
        // Convert offset (page number) to actual skip count for pagination
        // offset=0 → page 0 → skip 0, take pageSize
        // offset=1 → page 1 → skip pageSize, take pageSize
        // offset=2 → page 2 → skip 2*pageSize, take pageSize
        int skip = offset * pageSize;
        
        // Apply offset-based pagination
        query.skip(skip);
        query.limit(pageSize);
        
        log.info("Executing query with skip: {} (offset {} * pageSize {}), limit: {}", skip, offset, pageSize, pageSize);
        List<InAppNotification> notifications = mongoTemplate.find(query, InAppNotification.class);
        
        log.info("Found {} unread notifications out of {} total (offset: {}, pageSize: {})", notifications.size(), total, offset, pageSize);
        
        // Create Pageable for PageImpl (needed for proper Page object)
        Pageable pageable = PageRequest.of(offset, pageSize);
        
        return new PageImpl<>(notifications, pageable, total);
    }

    @Override
    public Page<InAppNotification> findArchivedByUserIdWithPagination(String userId, String search, int offset, int pageSize) {
        log.info("Finding archived (read) notifications for user: {} with search: '{}', offset: {}, pageSize: {}", userId, search, offset, pageSize);

        Query query = buildQuery(userId, true, search);

        long total = mongoTemplate.count(query, InAppNotification.class);
        log.info("Total archived notifications found: {}", total);

        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        int skip = offset * pageSize;
        query.skip(skip);
        query.limit(pageSize);

        List<InAppNotification> notifications = mongoTemplate.find(query, InAppNotification.class);
        log.info("Found {} archived notifications out of {} total (offset: {}, pageSize: {})", notifications.size(), total, offset, pageSize);

        Pageable pageable = PageRequest.of(offset, pageSize);
        return new PageImpl<>(notifications, pageable, total);
    }

    /**
     * Build MongoDB query with filters
     */
    private Query buildQuery(String userId, Boolean isRead, String search) {
        Query query = new Query();
        
        // Always filter by user_id
        query.addCriteria(Criteria.where("user_id").is(userId));
        
        // Filter by read status if provided
        if (isRead != null) {
            query.addCriteria(Criteria.where("is_read").is(isRead));
        }
        
        // Add search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            Pattern searchPattern = Pattern.compile(search.trim(), Pattern.CASE_INSENSITIVE);
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(searchPattern),
                    Criteria.where("message").regex(searchPattern)
            );
            query.addCriteria(searchCriteria);
        }
        
        return query;
    }
}

