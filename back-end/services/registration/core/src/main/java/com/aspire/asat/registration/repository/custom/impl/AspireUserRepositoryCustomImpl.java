package com.aspire.asat.registration.repository.custom.impl;

import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.reports.UserGrowthTrendPointDTO;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.repository.custom.AspireUserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Custom implementation for {@link AspireUserRepositoryCustom}. Uses count query
 * so that when duplicate usernames exist we return true without throwing "non unique result".
 */
@Repository
@RequiredArgsConstructor
public class AspireUserRepositoryCustomImpl implements AspireUserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String trimmed = username.trim();
        Query query = new Query(
                Criteria.where("username").regex("^" + Pattern.quote(trimmed) + "$", "i")
        );
        return mongoTemplate.count(query, AspireUser.class) > 0;
    }

    @Override
    public Map<String, Long> countUsersGroupedByDepartment(String clientAdminId, String userType) {
        Query query = new Query(Criteria.where("clientAdminId").is(clientAdminId).and("userType").is(userType));
        List<AspireUser> users = mongoTemplate.find(query, AspireUser.class);
        Map<String, Long> merged = new LinkedHashMap<>();
        for (AspireUser u : users) {
            String dept = resolveDepartmentLabel(u.getDepartment());
            merged.merge(dept, 1L, Long::sum);
        }
        return merged;
    }

    @Override
    public Map<String, Long> countUsersGroupedByRiskGroup(String clientAdminId, String userType) {
        Query query = new Query(Criteria.where("clientAdminId").is(clientAdminId).and("userType").is(userType));
        List<AspireUser> users = mongoTemplate.find(query, AspireUser.class);
        Map<String, Long> merged = new LinkedHashMap<>();
        for (AspireUser u : users) {
            String key = resolveRiskGroupKey(u.getRiskGroup());
            merged.merge(key, 1L, Long::sum);
        }
        return merged;
    }

    @Override
    public long countUsersCreatedSince(Instant since, String clientAdminId, String mspId) {
        if (since == null) {
            return 0L;
        }
        Criteria criteria = Criteria.where("createdAt").gte(since);
        applyScope(criteria, clientAdminId, null, mspId);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public long countUsersCreatedSince(Instant since, List<String> clientAdminIds) {
        if (since == null) {
            return 0L;
        }
        Criteria criteria = Criteria.where("createdAt").gte(since);
        applyScope(criteria, null, clientAdminIds, null);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public long countByStatusScoped(String status, String clientAdminId, String mspId) {
        if (status == null || status.isBlank()) {
            return 0L;
        }
        Criteria criteria = Criteria.where("status").is(status);
        applyScope(criteria, clientAdminId, null, mspId);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public long countByStatusScoped(String status, List<String> clientAdminIds) {
        if (status == null || status.isBlank()) {
            return 0L;
        }
        Criteria criteria = Criteria.where("status").is(status);
        applyScope(criteria, null, clientAdminIds, null);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public long countAllScoped(String clientAdminId, String mspId) {
        Criteria criteria = new Criteria();
        applyScope(criteria, clientAdminId, null, mspId);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public long countAllScoped(List<String> clientAdminIds) {
        Criteria criteria = new Criteria();
        applyScope(criteria, null, clientAdminIds, null);
        return mongoTemplate.count(new Query(criteria), AspireUser.class);
    }

    @Override
    public List<UserGrowthTrendPointDTO> getUserGrowthTrend(int months, String clientAdminId, String mspId) {
        return buildUserGrowthTrend(months, clientAdminId, null, mspId);
    }

    @Override
    public List<UserGrowthTrendPointDTO> getUserGrowthTrend(int months, List<String> clientAdminIds) {
        return buildUserGrowthTrend(months, null, clientAdminIds, null);
    }

    private List<UserGrowthTrendPointDTO> buildUserGrowthTrend(int months,
                                                             String clientAdminId,
                                                             List<String> clientAdminIds,
                                                             String mspId) {
        int safeMonths = months <= 0 ? 6 : months;
        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        List<UserGrowthTrendPointDTO> points = new ArrayList<>(safeMonths);
        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            // Cumulative: count all users whose createdAt is strictly before the start
            // of the FOLLOWING month (i.e. on or before end of this month).
            Instant cutoff = LocalDate.of(ym.getYear(), ym.getMonth(), 1)
                    .plusMonths(1)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
            Criteria criteria = Criteria.where("createdAt").lt(cutoff);
            applyScope(criteria, clientAdminId, clientAdminIds, mspId);
            long count = mongoTemplate.count(new Query(criteria), AspireUser.class);
            points.add(UserGrowthTrendPointDTO.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .label(monthLabel(ym.getMonth()))
                    .totalUsers(count)
                    .build());
        }
        return points;
    }

    private static void applyScope(Criteria criteria, String clientAdminId, List<String> clientAdminIds, String mspId) {
        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteria.and("clientAdminId").in(clientAdminIds);
        } else if (clientAdminId != null && !clientAdminId.isBlank()) {
            criteria.and("clientAdminId").is(clientAdminId);
        }
        if (mspId != null && !mspId.isBlank()) {
            criteria.and("mspId").is(mspId);
        }
    }

    @Override
    public List<AspireUser> findUsersForReport(String search,
                                               String userType,
                                               String country,
                                               String mspId,
                                               String clientAdminId,
                                               String status,
                                               Instant fromDate,
                                               Instant toDateExclusive,
                                               int offset,
                                               int pageSize) {
        Query query = buildReportFilterQuery(search, userType, country, mspId, clientAdminId, null, status, fromDate, toDateExclusive);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;
        int skip = safeOffset == 0 ? 0 : (safeOffset * safePageSize);
        query.skip(skip);
        query.limit(safePageSize);

        return mongoTemplate.find(query, AspireUser.class);
    }

    @Override
    public long countUsersForReport(String search,
                                    String userType,
                                    String country,
                                    String mspId,
                                    String clientAdminId,
                                    String status,
                                    Instant fromDate,
                                    Instant toDateExclusive) {
        Query query = buildReportFilterQuery(search, userType, country, mspId, clientAdminId, null, status, fromDate, toDateExclusive);
        return mongoTemplate.count(query, AspireUser.class);
    }

    @Override
    public List<AspireUser> findUsersForReport(String search,
                                               String userType,
                                               String country,
                                               List<String> clientAdminIds,
                                               String status,
                                               Instant fromDate,
                                               Instant toDateExclusive,
                                               int offset,
                                               int pageSize) {
        Query query = buildReportFilterQuery(search, userType, country, null, null, clientAdminIds, status, fromDate, toDateExclusive);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;
        int skip = safeOffset == 0 ? 0 : (safeOffset * safePageSize);
        query.skip(skip);
        query.limit(safePageSize);

        return mongoTemplate.find(query, AspireUser.class);
    }

    @Override
    public long countUsersForReport(String search,
                                    String userType,
                                    String country,
                                    List<String> clientAdminIds,
                                    String status,
                                    Instant fromDate,
                                    Instant toDateExclusive) {
        Query query = buildReportFilterQuery(search, userType, country, null, null, clientAdminIds, status, fromDate, toDateExclusive);
        return mongoTemplate.count(query, AspireUser.class);
    }

    @Override
    public List<AspireUser> findEndUsersPaged(
            String clientAdminId,
            String search,
            String status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            List<UUID> excludeUserIds,
            int offset,
            int pageSize) {
        Query query = buildEndUserFilterQuery(
                List.of(clientAdminId), search, status, departments, riskGroups, excludeUserIds);
        query.with(Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("userId")));

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        long skip = safeOffset == 0 ? 0L : (long) safeOffset * safePageSize;
        query.skip(skip);
        query.limit(safePageSize);
        return mongoTemplate.find(query, AspireUser.class);
    }

    @Override
    public long countEndUsersPaged(
            String clientAdminId,
            String search,
            String status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            List<UUID> excludeUserIds) {
        Query query = buildEndUserFilterQuery(
                List.of(clientAdminId), search, status, departments, riskGroups, excludeUserIds);
        return mongoTemplate.count(query, AspireUser.class);
    }

    @Override
    public List<String> findEndUserIds(List<String> clientAdminIds, String search, List<String> departments) {
        List<String> scopedIds = normalizeClientAdminIds(clientAdminIds);
        if (scopedIds.isEmpty()) {
            return List.of();
        }
        Query query = buildEndUserFilterQuery(scopedIds, search, null, departments, null, null);
        query.fields().include("userId").include("_id");
        return mongoTemplate.find(query, AspireUser.class).stream()
                .map(AspireUserRepositoryCustomImpl::resolveUserId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private static Query buildEndUserFilterQuery(
            List<String> clientAdminIds,
            String search,
            String status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            List<UUID> excludeUserIds) {
        List<Criteria> criteriaList = new ArrayList<>();
        List<String> scopedIds = normalizeClientAdminIds(clientAdminIds);
        if (scopedIds.size() == 1) {
            criteriaList.add(Criteria.where("clientAdminId").is(scopedIds.get(0)));
        } else if (!scopedIds.isEmpty()) {
            criteriaList.add(Criteria.where("clientAdminId").in(scopedIds));
        } else {
            criteriaList.add(Criteria.where("clientAdminId").is("__none__"));
        }
        criteriaList.add(Criteria.where("userType").is(UserType.USER.name()));

        if (StringUtils.hasText(status)) {
            criteriaList.add(Criteria.where("status").is(status.trim()));
        }

        if (!CollectionUtils.isEmpty(departments)) {
            List<Criteria> deptCriteria = departments.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .flatMap(dept -> java.util.stream.Stream.of(
                            Criteria.where("department").regex("^" + Pattern.quote(dept) + "$", "i"),
                            Criteria.where("department").is(dept)))
                    .toList();
            if (!deptCriteria.isEmpty()) {
                criteriaList.add(new Criteria().orOperator(deptCriteria.toArray(new Criteria[0])));
            }
        }

        if (!CollectionUtils.isEmpty(riskGroups)) {
            criteriaList.add(Criteria.where("riskGroup").in(riskGroups));
        }

        if (StringUtils.hasText(search)) {
            String pattern = ".*" + Pattern.quote(search.trim()) + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("email").regex(pattern, "i"),
                    Criteria.where("firstName").regex(pattern, "i"),
                    Criteria.where("lastName").regex(pattern, "i")
            ));
        }

        if (!CollectionUtils.isEmpty(excludeUserIds)) {
            criteriaList.add(Criteria.where("userId").nin(excludeUserIds));
        }

        return new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
    }

    private static List<String> normalizeClientAdminIds(List<String> clientAdminIds) {
        if (clientAdminIds == null || clientAdminIds.isEmpty()) {
            return List.of();
        }
        return clientAdminIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static String resolveUserId(AspireUser user) {
        if (user.getUserId() != null) {
            return user.getUserId().toString();
        }
        return user.getId() != null ? user.getId().toString() : null;
    }

    private static Query buildReportFilterQuery(String search,
                                                String userType,
                                                String country,
                                                String mspId,
                                                String clientAdminId,
                                                List<String> clientAdminIds,
                                                String status,
                                                Instant fromDate,
                                                Instant toDateExclusive) {
        Query query = new Query();
        List<Criteria> criteriaList = new java.util.ArrayList<>();

        if (search != null && !search.isBlank()) {
            String pattern = ".*" + Pattern.quote(search.trim()) + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("email").regex(pattern, "i"),
                    Criteria.where("username").regex(pattern, "i"),
                    Criteria.where("firstName").regex(pattern, "i"),
                    Criteria.where("lastName").regex(pattern, "i")
            ));
        }
        if (userType != null && !userType.isBlank()) {
            criteriaList.add(Criteria.where("userType").is(userType.toUpperCase()));
        }
        if (country != null && !country.isBlank()) {
            criteriaList.add(Criteria.where("country").is(country));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspId").is(mspId));
        }
        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteriaList.add(Criteria.where("clientAdminId").in(clientAdminIds));
        } else if (clientAdminId != null && !clientAdminId.isBlank()) {
            criteriaList.add(Criteria.where("clientAdminId").is(clientAdminId));
        }
        if (status != null && !status.isBlank()) {
            criteriaList.add(Criteria.where("status").is(status.toUpperCase()));
        }
        if (fromDate != null && toDateExclusive != null) {
            criteriaList.add(Criteria.where("createdAt").gte(fromDate).lt(toDateExclusive));
        } else if (fromDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(fromDate));
        } else if (toDateExclusive != null) {
            criteriaList.add(Criteria.where("createdAt").lt(toDateExclusive));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private static String monthLabel(Month month) {
        return month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    private static String resolveRiskGroupKey(RiskGroup riskGroup) {
        return riskGroup == null ? "UNASSIGNED" : riskGroup.name();
    }

    private static String resolveDepartmentLabel(String department) {
        if (department == null) {
            return "Unassigned";
        }
        String s = department.trim();
        return s.isEmpty() ? "Unassigned" : s;
    }
}
