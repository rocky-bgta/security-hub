package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.model.LandingPage;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository for LandingPage with advanced filter/search.
 */
public interface LandingPageRepositoryCustom {

    /**
     * Find landing pages accessible by a client (client-specific + global),
     * applying optional search and filters.
     */
    List<LandingPage> findWithFilters(
            String clientId,
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            boolean isAspireAdmin,
            Pageable pageable);

    /**
     * Count landing pages for the same filters (used for pagination totals).
     */
    long countWithFilters(
            String clientId,
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            boolean isAspireAdmin);
}

