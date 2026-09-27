package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.custom.UserSubPackageRepositoryCustom;
import com.aspire.asat.cms.util.PackageAssignmentStatusUtil;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class UserSubPackageRepositoryCustomImpl implements UserSubPackageRepositoryCustom {

    private static final String COLLECTION = "user_subpackages";

    private final MongoTemplate mongoTemplate;

    @Override
    public long countDistinctParentPackages(String clientAdminId, List<String> clientAdminIds,
                                            LocalDate fromDate, LocalDate toDate) {
        Query query = buildBaseQuery(clientAdminId, clientAdminIds, null, null, fromDate, toDate);
        List<UserSubPackage> assignments = mongoTemplate.find(query, UserSubPackage.class, COLLECTION);
        if (assignments.isEmpty()) {
            return 0L;
        }

        Set<String> subPackageIds = new HashSet<>();
        for (UserSubPackage assignment : assignments) {
            if (assignment.getSubPackageId() != null) {
                subPackageIds.add(assignment.getSubPackageId());
            }
        }
        if (subPackageIds.isEmpty()) {
            return 0L;
        }

        Query subPackageQuery = new Query(Criteria.where("_id").in(subPackageIds));
        subPackageQuery.fields().include("packageId");
        List<SubPackage> subPackages = mongoTemplate.find(subPackageQuery, SubPackage.class, "sub_packages");

        return subPackages.stream()
                .map(SubPackage::getPackageId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .count();
    }

    @Override
    public long countAssignments(String clientAdminId, List<String> clientAdminIds,
                                 String search, String statusFilter,
                                 LocalDate fromDate, LocalDate toDate) {
        Query query = buildBaseQuery(clientAdminId, clientAdminIds, search, statusFilter, fromDate, toDate);
        return mongoTemplate.count(query, UserSubPackage.class, COLLECTION);
    }

    @Override
    public long countByExpiryBucket(String clientAdminId, List<String> clientAdminIds,
                                    LocalDate fromDate, LocalDate toDate,
                                    String bucket) {
        Query query = buildBaseQuery(clientAdminId, clientAdminIds, null, bucket, fromDate, toDate);
        return mongoTemplate.count(query, UserSubPackage.class, COLLECTION);
    }

    @Override
    public List<UserSubPackage> findAssignmentsForReport(String clientAdminId, List<String> clientAdminIds,
                                                         String search, String statusFilter,
                                                         LocalDate fromDate, LocalDate toDate,
                                                         int offset, int pageSize) {
        Query query = buildBaseQuery(clientAdminId, clientAdminIds, search, statusFilter, fromDate, toDate);
        query.with(Sort.by(Sort.Direction.DESC, "assignedDate"));

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int skip = safeOffset == 0 ? 0 : safeOffset * safePageSize;
        query.skip(skip);
        query.limit(safePageSize);

        return mongoTemplate.find(query, UserSubPackage.class, COLLECTION);
    }

    @Override
    public List<UserSubPackage> findAssignmentsForReportExport(String clientAdminId, List<String> clientAdminIds,
                                                               String search, String statusFilter,
                                                               LocalDate fromDate, LocalDate toDate,
                                                               int offset, int pageSize) {
        Query query = buildBaseQuery(clientAdminId, clientAdminIds, search, statusFilter, fromDate, toDate);
        query.with(Sort.by(Sort.Direction.DESC, "assignedDate"));

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 1000 : pageSize;
        int skip = safeOffset == 0 ? 0 : safeOffset * safePageSize;
        query.skip(skip);
        query.limit(safePageSize);

        return mongoTemplate.find(query, UserSubPackage.class, COLLECTION);
    }

    @Override
    public long countLicenseAssignments(String clientAdminId, List<String> clientAdminIds,
                                        List<String> userIds, String productId, String packageId,
                                        String statusFilter, LocalDate fromDate, LocalDate toDate) {
        if (userIds != null && userIds.isEmpty()) {
            return 0L;
        }
        if (hasText(packageId)) {
            return countLicenseAssignmentsWithPackage(clientAdminId, clientAdminIds, userIds, productId,
                    packageId, statusFilter, fromDate, toDate);
        }
        Query query = buildLicenseAssignmentQuery(
                clientAdminId, clientAdminIds, userIds, productId, statusFilter, fromDate, toDate);
        return mongoTemplate.count(query, UserSubPackage.class, COLLECTION);
    }

    @Override
    public List<UserSubPackage> findLicenseAssignments(String clientAdminId, List<String> clientAdminIds,
                                                       List<String> userIds, String productId, String packageId,
                                                       String statusFilter, LocalDate fromDate, LocalDate toDate,
                                                       int offset, int pageSize) {
        if (userIds != null && userIds.isEmpty()) {
            return List.of();
        }
        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        long skip = safeOffset == 0 ? 0L : (long) safeOffset * safePageSize;

        if (hasText(packageId)) {
            return findLicenseAssignmentsWithPackage(clientAdminId, clientAdminIds, userIds, productId,
                    packageId, statusFilter, fromDate, toDate, skip, safePageSize);
        }

        Query query = buildLicenseAssignmentQuery(
                clientAdminId, clientAdminIds, userIds, productId, statusFilter, fromDate, toDate);
        query.with(Sort.by(Sort.Direction.DESC, "assignedDate"));
        query.skip(skip);
        query.limit(safePageSize);
        return mongoTemplate.find(query, UserSubPackage.class, COLLECTION);
    }

    private Query buildBaseQuery(String clientAdminId,
                                 List<String> clientAdminIds,
                                 String search,
                                 String statusFilter,
                                 LocalDate fromDate,
                                 LocalDate toDate) {
        List<Criteria> criteriaList = new ArrayList<>();
        applyScope(criteriaList, clientAdminId, clientAdminIds);

        if (fromDate != null) {
            criteriaList.add(Criteria.where("assignedDate").gte(fromDate));
        }
        if (toDate != null) {
            criteriaList.add(Criteria.where("assignedDate").lte(toDate));
        }

        if (search != null && !search.isBlank()) {
            String pattern = ".*" + Pattern.quote(search.trim()) + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("subPackageName").regex(pattern, "i"),
                    Criteria.where("userEmail").regex(pattern, "i")
            ));
        }

        LocalDate today = LocalDate.now();
        Criteria statusCriteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria(statusFilter, today);
        if (statusCriteria.getCriteriaObject() != null && !statusCriteria.getCriteriaObject().isEmpty()) {
            criteriaList.add(statusCriteria);
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private static void applyScope(List<Criteria> criteriaList, String clientAdminId, List<String> clientAdminIds) {
        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            criteriaList.add(Criteria.where("clientAdminId").in(clientAdminIds));
        } else if (clientAdminId != null && !clientAdminId.isBlank()) {
            criteriaList.add(Criteria.where("clientAdminId").is(clientAdminId));
        }
    }

    private Query buildLicenseAssignmentQuery(String clientAdminId,
                                              List<String> clientAdminIds,
                                              List<String> userIds,
                                              String productId,
                                              String statusFilter,
                                              LocalDate fromDate,
                                              LocalDate toDate) {
        List<Criteria> criteriaList = buildLicenseAssignmentCriteria(
                clientAdminId, clientAdminIds, userIds, productId, statusFilter, fromDate, toDate);
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private List<Criteria> buildLicenseAssignmentCriteria(String clientAdminId,
                                                          List<String> clientAdminIds,
                                                          List<String> userIds,
                                                          String productId,
                                                          String statusFilter,
                                                          LocalDate fromDate,
                                                          LocalDate toDate) {
        List<Criteria> criteriaList = new ArrayList<>();
        applyScope(criteriaList, clientAdminId, clientAdminIds);

        if (fromDate != null) {
            criteriaList.add(Criteria.where("assignedDate").gte(fromDate));
        }
        if (toDate != null) {
            criteriaList.add(Criteria.where("assignedDate").lte(toDate));
        }
        if (hasText(productId)) {
            criteriaList.add(Criteria.where("productId").is(productId.trim()));
        }
        if (userIds != null && !userIds.isEmpty()) {
            criteriaList.add(Criteria.where("userId").in(userIds));
        }

        LocalDate today = LocalDate.now();
        Criteria statusCriteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria(statusFilter, today);
        if (statusCriteria.getCriteriaObject() != null && !statusCriteria.getCriteriaObject().isEmpty()) {
            criteriaList.add(statusCriteria);
        }
        return criteriaList;
    }

    private long countLicenseAssignmentsWithPackage(String clientAdminId, List<String> clientAdminIds,
                                                    List<String> userIds, String productId, String packageId,
                                                    String statusFilter, LocalDate fromDate, LocalDate toDate) {
        List<AggregationOperation> ops = new ArrayList<>(licenseAssignmentPackagePipeline(
                clientAdminId, clientAdminIds, userIds, productId, packageId, statusFilter, fromDate, toDate));
        ops.add(Aggregation.count().as("total"));
        Aggregation aggregation = Aggregation.newAggregation(ops);
        Document result = mongoTemplate.aggregate(aggregation, COLLECTION, Document.class).getUniqueMappedResult();
        if (result == null || result.get("total") == null) {
            return 0L;
        }
        return ((Number) result.get("total")).longValue();
    }

    private List<UserSubPackage> findLicenseAssignmentsWithPackage(String clientAdminId, List<String> clientAdminIds,
                                                                   List<String> userIds, String productId,
                                                                   String packageId, String statusFilter,
                                                                   LocalDate fromDate, LocalDate toDate,
                                                                   long skip, int limit) {
        List<AggregationOperation> ops = new ArrayList<>(licenseAssignmentPackagePipeline(
                clientAdminId, clientAdminIds, userIds, productId, packageId, statusFilter, fromDate, toDate));
        ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "assignedDate")));
        ops.add(Aggregation.skip(skip));
        ops.add(Aggregation.limit(limit));
        Aggregation aggregation = Aggregation.newAggregation(ops);
        return mongoTemplate.aggregate(aggregation, COLLECTION, UserSubPackage.class).getMappedResults();
    }

    private List<AggregationOperation> licenseAssignmentPackagePipeline(String clientAdminId,
                                                                        List<String> clientAdminIds,
                                                                        List<String> userIds,
                                                                        String productId,
                                                                        String packageId,
                                                                        String statusFilter,
                                                                        LocalDate fromDate,
                                                                        LocalDate toDate) {
        List<Criteria> matchCriteria = buildLicenseAssignmentCriteria(
                clientAdminId, clientAdminIds, userIds, productId, statusFilter, fromDate, toDate);
        List<AggregationOperation> ops = new ArrayList<>();
        if (!matchCriteria.isEmpty()) {
            ops.add(Aggregation.match(new Criteria().andOperator(matchCriteria.toArray(new Criteria[0]))));
        }
        ops.add(Aggregation.lookup("sub_packages", "subPackageId", "_id", "subPackageJoin"));
        ops.add(Aggregation.unwind("subPackageJoin"));
        ops.add(Aggregation.match(Criteria.where("subPackageJoin.packageId").is(packageId.trim())));
        return ops;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
