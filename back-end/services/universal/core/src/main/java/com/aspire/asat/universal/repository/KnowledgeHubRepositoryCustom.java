package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.KnowlegeHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface KnowledgeHubRepositoryCustom {

    /**
     * Find all knowledge hub items with filters
     * @param search search by name (case-insensitive)
     * @param categoryId filter by category ID
     * @param resourceType filter by resource type
     * @param status filter by status (DRAFT, ACTIVE, INACTIVE)
     * @param pageable pagination parameters
     * @return paginated results ordered by createdAt DESC
     */
    Page<KnowlegeHub> findAllWithFilters(String search, String categoryId, String resourceType, String status, Pageable pageable);

    /**
     * Find active knowledge hub items with filters (published and not expired)
     * @param search search by name (case-insensitive)
     * @param categoryId filter by category ID
     * @param resourceType filter by resource type
     * @param currentDate current date for publish/expiry check
     * @param pageable pagination parameters
     * @return paginated results ordered by sequence ASC, then createdAt DESC
     */
    Page<KnowlegeHub> findActiveWithFilters(String search, String categoryId, String resourceType,
                                            LocalDateTime currentDate, Pageable pageable);

    /**
     * Count all knowledge hub items with filters
     */
    long countAllWithFilters(String search, String categoryId, String resourceType, String status);

    /**
     * Count active knowledge hub items with filters
     */
    long countActiveWithFilters(String search, String categoryId, String resourceType, LocalDateTime currentDate);
}

