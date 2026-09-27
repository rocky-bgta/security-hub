package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.LatestNews;
import com.aspire.asat.universal.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LatestNewsRepository extends MongoRepository<LatestNews, String>, LatestNewsRepositoryCustom {
    Page<LatestNews> findByStatus(Status status, Pageable pageable);

    Page<LatestNews> findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
        Status status,
        LocalDateTime currentDate,
        LocalDateTime currentDateForExpiry,
        Pageable pageable
    );

    Optional<LatestNews> findBySlug(String slug);

    // Count active news within publish/expiry bounds (no ordering in count)
    long countByStatusAndPublishedDateBeforeAndExpireDateAfter(Status status, LocalDateTime currentDate, LocalDateTime currentDateForExpiry);
}

