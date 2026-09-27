package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.custom.PhishingUserLicenceRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MongoTemplate-backed paged find/count and department/group aggregations
 * for {@code phishing_user_licence}.
 */
@Repository
@RequiredArgsConstructor
public class PhishingUserLicenceRepositoryCustomImpl implements PhishingUserLicenceRepositoryCustom {

    private static final String COLLECTION = "phishing_user_licence";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<PhishingUserLicence> findLicensedUsers(
            String clientAdminId,
            String productPackageId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }

        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int safeOffset = Math.max(offset, 0);
        long skip = (long) safeOffset * safePageSize;

        Query query = buildFilterQuery(clientAdminId, productPackageId, search, departments, riskGroups);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.skip(skip);
        query.limit(safePageSize);
        return mongoTemplate.find(query, PhishingUserLicence.class);
    }

    @Override
    public long countLicensedUsers(
            String clientAdminId,
            String productPackageId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return 0L;
        }
        Query query = buildFilterQuery(clientAdminId, productPackageId, search, departments, riskGroups);
        return mongoTemplate.count(query, PhishingUserLicence.class);
    }

    @Override
    public List<LicensedUserDepartmentCountDto> countByDepartment(
            String clientAdminId, String productPackageId) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(activeLicensedCriteria(clientAdminId, productPackageId)));
        operations.add(Aggregation.project()
                .andExpression("ifNull(departmentName, '')").as("departmentName"));
        operations.add(Aggregation.group("departmentName").count().as("userCount"));
        operations.add(Aggregation.project()
                .and("_id").as("departmentName")
                .and("userCount").as("userCount")
                .andExclude("_id"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "departmentName"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<LicensedUserDepartmentCountDto> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, LicensedUserDepartmentCountDto.class);
        return results.getMappedResults();
    }

    @Override
    public List<LicensedUserGroupCountDto> countByGroup(
            String clientAdminId, String productPackageId) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(activeLicensedCriteria(clientAdminId, productPackageId)));
        operations.add(Aggregation.group("riskGroup").count().as("userCount"));
        operations.add(Aggregation.project()
                .and("_id").as("riskGroup")
                .and("userCount").as("userCount")
                .andExclude("_id"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "riskGroup"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<LicensedUserGroupCountDto> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, LicensedUserGroupCountDto.class);
        return results.getMappedResults();
    }

    @Override
    public List<String> findLicensedUserIds(String clientAdminId, String productPackageId) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }
        Query query = new Query(baseCriteria(clientAdminId, productPackageId));
        query.fields().include("userId");
        return mongoTemplate.find(query, PhishingUserLicence.class).stream()
                .map(PhishingUserLicence::getUserId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    @Override
    public List<PhishingUserLicence> findAudienceLicences(
            String clientAdminId,
            String productPackageId,
            List<String> departmentIds,
            List<String> groupIds,
            List<String> userIds) {
        if (!StringUtils.hasText(clientAdminId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }

        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(activeLicensedCriteria(clientAdminId, productPackageId));

        List<String> departments = normalizeTokens(departmentIds);
        if (!departments.isEmpty()) {
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("departmentId").in(departments),
                    Criteria.where("departmentName").in(departments)));
        }

        List<String> groups = normalizeTokens(groupIds);
        if (!groups.isEmpty()) {
            List<RiskGroup> riskGroups = new ArrayList<>();
            for (String token : groups) {
                try {
                    riskGroups.add(RiskGroup.valueOf(token.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    // not a risk-group enum value — treat as groupId
                }
            }
            List<Criteria> groupOr = new ArrayList<>();
            groupOr.add(Criteria.where("groupIds").in(groups));
            if (!riskGroups.isEmpty()) {
                groupOr.add(Criteria.where("riskGroup").in(riskGroups));
            }
            andCriteria.add(new Criteria().orOperator(groupOr.toArray(new Criteria[0])));
        }

        List<String> users = normalizeTokens(userIds);
        if (!users.isEmpty()) {
            andCriteria.add(Criteria.where("userId").in(users));
        }

        Query query = new Query(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, PhishingUserLicence.class);
    }

    @Override
    public long updateUserSnapshot(
            String userId,
            String clientAdminId,
            String firstName,
            String lastName,
            String phoneNumber,
            String departmentName,
            String countryName,
            Boolean active) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(clientAdminId)) {
            return 0L;
        }
        Query query = new Query(Criteria.where("userId").is(userId.trim())
                .and("clientAdminId").is(clientAdminId.trim()));
        Update update = new Update()
                .set("firstName", firstName)
                .set("lastName", lastName)
                .set("phoneNumber", phoneNumber)
                .set("departmentName", departmentName)
                .set("countryName", countryName);
        if (active != null) {
            update.set("active", active);
        }
        return mongoTemplate.updateMulti(query, update, PhishingUserLicence.class).getMatchedCount();
    }

    private Query buildFilterQuery(
            String clientAdminId,
            String productPackageId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups) {
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(activeLicensedCriteria(clientAdminId, productPackageId));

        if (StringUtils.hasText(search)) {
            Pattern pattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("firstName").regex(pattern),
                    Criteria.where("lastName").regex(pattern),
                    Criteria.where("email").regex(pattern)
            ));
        }

        List<String> departmentFilter = normalizeTokens(departments);
        if (!departmentFilter.isEmpty()) {
            andCriteria.add(Criteria.where("departmentName").in(departmentFilter));
        }

        if (!CollectionUtils.isEmpty(riskGroups)) {
            andCriteria.add(Criteria.where("riskGroup").in(riskGroups));
        }

        return new Query(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
    }

    private static Criteria baseCriteria(String clientAdminId, String productPackageId) {
        return Criteria.where("clientAdminId").is(clientAdminId.trim())
                .and("productPackageId").is(productPackageId.trim());
    }

    /** Licensed-tab queries: package scope and active seats only. */
    private static Criteria activeLicensedCriteria(String clientAdminId, String productPackageId) {
        return baseCriteria(clientAdminId, productPackageId).and("active").is(true);
    }

    private static List<String> normalizeTokens(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
