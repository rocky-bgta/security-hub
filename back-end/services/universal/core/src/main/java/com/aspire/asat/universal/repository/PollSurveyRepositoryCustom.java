package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PollSurveyRepositoryCustom {

    /**
     * Find all poll/surveys with search and filters
     * @param search search term for title (case-insensitive)
     * @param type filter by poll/survey type
     * @param status filter by status
     * @param pageable pagination parameters
     * @return paginated results ordered by createdAt DESC
     */
    Page<PollSurvey> findAllWithFilters(String search, String type, String status, Pageable pageable);

    /**
     * Count all poll/surveys with search and filters
     */
    long countWithFilters(String search, String type, String status);
}

