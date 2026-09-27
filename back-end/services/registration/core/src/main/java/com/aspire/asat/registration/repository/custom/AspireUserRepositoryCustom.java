package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.reports.UserGrowthTrendPointDTO;
import com.aspire.asat.registration.model.AspireUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Custom fragment for AspireUser repository. Use for methods that must not throw
 * "non unique result" when duplicate usernames exist (e.g. exists uses count, not findOne).
 */
public interface AspireUserRepositoryCustom {

    /**
     * Returns true if any user exists with the given username (case-insensitive).
     * Implemented via count so it never throws when multiple documents match.
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Counts {@link com.aspire.asat.registration.model.AspireUser} rows grouped by {@code department}
     * (null or blank department is labeled {@code Unassigned}), filtered by {@code clientAdminId}
     * and {@code userType}.
     */
    Map<String, Long> countUsersGroupedByDepartment(String clientAdminId, String userType);

    /**
     * Counts {@link com.aspire.asat.registration.model.AspireUser} rows grouped by {@code riskGroup}
     * (null risk group is labeled {@code UNASSIGNED}), filtered by {@code clientAdminId} and {@code userType}.
     */
    Map<String, Long> countUsersGroupedByRiskGroup(String clientAdminId, String userType);

    /**
     * Counts users created on or after {@code since}, optionally scoped by
     * {@code clientAdminId} and/or {@code mspId}. Either scope is applied only
     * when non-null/non-blank.
     */
    long countUsersCreatedSince(Instant since, String clientAdminId, String mspId);

    /**
     * Counts users matching the given {@code status}, optionally scoped by
     * {@code clientAdminId} and/or {@code mspId}.
     */
    long countByStatusScoped(String status, String clientAdminId, String mspId);

    /**
     * Total user count, optionally scoped by {@code clientAdminId} and/or {@code mspId}.
     */
    long countAllScoped(String clientAdminId, String mspId);

    long countAllScoped(List<String> clientAdminIds);

    long countByStatusScoped(String status, List<String> clientAdminIds);

    long countUsersCreatedSince(Instant since, List<String> clientAdminIds);

    List<UserGrowthTrendPointDTO> getUserGrowthTrend(int months, List<String> clientAdminIds);

    /**
     * Returns one trend point per month for the trailing {@code months} months,
     * where each point holds the cumulative number of users whose {@code createdAt}
     * is on or before the end of that month. Results are ordered chronologically.
     */
    List<UserGrowthTrendPointDTO> getUserGrowthTrend(int months, String clientAdminId, String mspId);

    /**
     * Paginated list of {@link AspireUser} for the User Summary Report. Same
     * filter shape as {@code EndUserRepositoryCustom.findAllAspireUsersWithFilters}
     * plus an inclusive-start / exclusive-end {@code createdAt} range. Any of
     * the filter args (including {@code fromDate}/{@code toDateExclusive}) may
     * be {@code null}/blank to disable that filter. Results are sorted by
     * {@code createdAt} descending. {@code offset} is interpreted as a page
     * number (skip = offset * pageSize).
     */
    List<AspireUser> findUsersForReport(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            Instant fromDate,
            Instant toDateExclusive,
            int offset,
            int pageSize);

    /**
     * Total count matching {@link #findUsersForReport} (without pagination).
     */
    long countUsersForReport(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            Instant fromDate,
            Instant toDateExclusive);

    List<AspireUser> findUsersForReport(
            String search,
            String userType,
            String country,
            List<String> clientAdminIds,
            String status,
            Instant fromDate,
            Instant toDateExclusive,
            int offset,
            int pageSize);

    long countUsersForReport(
            String search,
            String userType,
            String country,
            List<String> clientAdminIds,
            String status,
            Instant fromDate,
            Instant toDateExclusive);

    /**
     * Paginated end-user list for a client admin ({@code userType=USER}), with optional
     * filters and exclusion of licensed userIds before skip/limit.
     * {@code offset} is page index (skip = offset * pageSize). Sort: createdAt DESC, userId ASC.
     */
    List<AspireUser> findEndUsersPaged(
            String clientAdminId,
            String search,
            String status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            List<UUID> excludeUserIds,
            int offset,
            int pageSize);

    long countEndUsersPaged(
            String clientAdminId,
            String search,
            String status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            List<UUID> excludeUserIds);

    /**
     * Unpaginated end-user IDs ({@code userType=USER}) for the given client admins.
     * Search matches firstName, lastName, and email. Department match is case-insensitive exact.
     * Returns {@code userId} when present, otherwise document {@code id}.
     */
    List<String> findEndUserIds(
            List<String> clientAdminIds,
            String search,
            List<String> departments);
}
