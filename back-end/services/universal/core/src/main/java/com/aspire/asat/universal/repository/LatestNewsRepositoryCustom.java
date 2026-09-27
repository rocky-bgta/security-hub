package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.LatestNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface LatestNewsRepositoryCustom {
    Page<LatestNews> findAllWithFilters(String status, String categoryId, String search, Pageable pageable);

    Page<LatestNews> findActiveWithFilters(String status, String categoryId, String search,
                                           LocalDateTime currentDate, Pageable pageable);

    long countAllWithFilters(String status, String categoryId, String search);

    long countActiveWithFilters(String status, String categoryId, String search, LocalDateTime currentDate);
}

