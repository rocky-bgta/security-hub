package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.entity.KnowlegeHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeHubRepository extends MongoRepository<KnowlegeHub, UUID>, KnowledgeHubRepositoryCustom {
    Page<KnowlegeHub> findByStatus(Status status, Pageable pageable);

    Page<KnowlegeHub> findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
        Status status,
        LocalDateTime currentDate,
        LocalDateTime currentDateForExpiry,
        Pageable pageable
    );

    Optional<KnowlegeHub> findBySlug(String slug);

    // Count active knowledge within publish/expiry bounds
    long countByStatusAndPublishedDateBeforeAndExpireDateAfter(Status status, LocalDateTime currentDate, LocalDateTime currentDateForExpiry);
}
