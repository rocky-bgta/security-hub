package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.UserRiskProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;

/**
 * Custom repository for UserRiskProfile with criteria-based search and filter.
 */
public interface UserRiskProfileRepositoryCustom {

    /**
     * Find user risk profiles with optional filters and search.
     *
     * @param clientId  filter by client (required; maps from clientAdminId)
     * @param department filter by department (optional)
     * @param search    search in email and firstName (optional, case-insensitive)
     * @param pageable  pagination and sort
     * @return page of user risk profiles
     */
    Page<UserRiskProfile> findWithFilters(String clientId, String department, String search, Pageable pageable);

    /**
     * Find user risk profiles with optional filters, risk level, and search.
     *
     * @param clientId filter by client (required; maps from clientAdminId)
     * @param department filter by department (optional)
     * @param search search in email, firstName, and lastName (optional, case-insensitive)
     * @param riskLevel filter by risk level (optional)
     * @param pageable pagination and sort
     * @return page of user risk profiles
     */
    Page<UserRiskProfile> findWithFilters(
            String clientId,
            String department,
            String search,
            RiskLevel riskLevel,
            Pageable pageable
    );

    /**
     * Find all user risk profiles with optional filters and search.
     *
     * @param clientId filter by client (required; maps from clientAdminId)
     * @param department filter by department (optional)
     * @param search search in email and firstName (optional, case-insensitive)
     * @param sort sort specification
     * @return list of user risk profiles
     */
    List<UserRiskProfile> findListWithFilters(String clientId, String department, String search, Sort sort);

    /**
     * Sum email counters across all user risk profiles (platform-wide phishing performance).
     */
    UserRiskProfileEmailTotals aggregateEmailTotals();

    /**
     * Sum email counters for user risk profiles whose {@code clientId} is in {@code clientIds}.
     * Empty/null {@code clientIds} returns empty totals.
     */
    UserRiskProfileEmailTotals aggregateEmailTotalsForClientIds(Collection<String> clientIds);
}
