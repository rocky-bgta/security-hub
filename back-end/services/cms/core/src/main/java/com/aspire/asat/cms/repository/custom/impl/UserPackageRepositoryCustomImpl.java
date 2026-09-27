package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserPackageRepositoryCustomImpl implements UserPackageRepositoryCustom {

    private static final String USER_PACKAGES_TABLE = "user_subpackages";
    private final MongoTemplate mongoTemplate;

    /**
     * Retrieves a paginated list of user packages with detailed information,
     * including package details and metadata such as validity, expiry dates,
     * and completed course IDs.
     *
     * @param userId   the ID of the user whose packages are to be retrieved
     * @param offset   the number of results to skip for pagination
     * @param pageSize the number of results to include in the page
     * @return a list of documents representing detailed user packages
     */
    @Override
    public List<Document> getUserPackagesWithDetails(String userId, int offset, int pageSize, String status) {
        Criteria criteria = Criteria.where("userId").is(userId);
        if ("COMPLETED".equalsIgnoreCase(status)) {
            criteria = criteria.and("status").is("COMPLETED");
        } else if (status != null) {
            criteria = criteria.and("status").ne("COMPLETED");
        }

        MatchOperation matchUser = Aggregation.match(criteria);

        LookupOperation lookupPackages = Aggregation.lookup("sub_packages", "subPackageId", "_id", "packageDetails");
        UnwindOperation unwind = Aggregation.unwind("packageDetails");

        ProjectionOperation project = Aggregation.project()
                .and("packageDetails._id").as("subPackageId")
                .and("packageDetails.name").as("subPackageName")
                .and("packageDetails.price").as("price")
                .and("packageDetails.packageDescription").as("packageDescription")
                .and("packageDetails.packageStatus").as("packageStatus")
                .and("packageDetails.courseIds").as("courseIds")
                .and("validity").as("validity")
                .and("expiryDate").as("expiryDate")
                .and("assignedDate").as("assignedDate")
                .and("completedTopicIds").as("completedTopicIds");

        Aggregation aggregation = Aggregation.newAggregation(
                matchUser,
                lookupPackages,
                unwind,
                project,
                Aggregation.skip(offset),
                Aggregation.limit(pageSize)
        );

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }


    /**
     * Counts the number of user packages for a given user ID, including detailed package information.
     *
     * @param userId the ID of the user whose package count is to be determined
     * @return the total count of user packages with detailed information
     */
    @Override
    public long countUserPackagesWithDetails(String userId, String status) {
        Criteria criteria = Criteria.where("userId").is(userId);
        if ("COMPLETED".equalsIgnoreCase(status)) {
            criteria = criteria.and("status").is("COMPLETED");
        } else if (status != null) {
            criteria = criteria.and("status").ne("COMPLETED");
        }

        MatchOperation matchUser = Aggregation.match(criteria);
        LookupOperation lookupPackages = Aggregation.lookup("sub_packages", "subPackageId", "_id", "packageDetails");
        UnwindOperation unwind = Aggregation.unwind("packageDetails");
        GroupOperation group = Aggregation.group().count().as("total");

        Aggregation aggregation = Aggregation.newAggregation(matchUser, lookupPackages, unwind, group);
        Document doc = mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getUniqueMappedResult();

        return doc != null ? doc.getInteger("total", 0) : 0;
    }

    /**
     * Retrieves a list of user packages that include the specified course.
     * The method performs a series of aggregation operations to filter relevant user package data
     * and includes package details such as package name, price, description, status, validity,
     * expiry date, and course IDs.
     *
     * @param userId   the ID of the user whose packages are to be filtered.
     * @param courseId the ID of the course to filter packages by.
     * @return a list of documents representing the user packages that include the specified course.
     */
    @Override
    public List<Document> getUserPackagesWithCourseIncluded(String userId, String courseId) {
        MatchOperation matchUser = Aggregation.match(Criteria.where("userId").is(userId));

        LookupOperation lookupPackages = Aggregation.lookup("sub_packages", "subPackageId", "_id", "packageDetails");

        UnwindOperation unwind = Aggregation.unwind("packageDetails");

        MatchOperation filterPackagesWithCourse = Aggregation.match(
                Criteria.where("packageDetails.courseIds").in(courseId)
        );

        ProjectionOperation project = Aggregation.project()
                .and("packageId").as("packageId")
                .and("packageDetails.packageName").as("packageName")
                .and("packageDetails.price").as("price")
                .and("packageDetails.packageDescription").as("packageDescription")
                .and("packageDetails.packageStatus").as("packageStatus")
                .and("packageDetails.courseIds").as("courseIds")
                .and("validity").as("validity")
                .and("expiryDate").as("expiryDate")
                .and("completedTopicIds").as("completedTopicIds");

        Aggregation aggregation = Aggregation.newAggregation(
                matchUser,
                lookupPackages,
                unwind,
                filterPackagesWithCourse,
                project
        );

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }


    @Override
    public List<Document> getUserPackageStatsMinimal(String userId) {
        MatchOperation matchUser = Aggregation.match(Criteria.where("userId").is(userId));

        // Project only necessary fields
        ProjectionOperation project = Aggregation.project()
                .andInclude("assignedDate", "expiryDate", "status", "certificateLink");

        Aggregation aggregation = Aggregation.newAggregation(matchUser, project);

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }


    @Override
    public List<Document> getUserPackagesWithCourseIds(String userId, int offset, int pageSize) {
        MatchOperation match = Aggregation.match(Criteria.where("userId").is(userId));

        LookupOperation joinPackage = Aggregation.lookup("package", "packageId", "_id", "package");
        UnwindOperation unwind = Aggregation.unwind("package");

        ProjectionOperation project = Aggregation.project()
                .and("package._id").as("packageId")
                .and("package.packageName").as("packageName")
                .and("package.courseIds").as("courseIds");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinPackage,
                unwind,
                project,
                Aggregation.skip(offset),
                Aggregation.limit(pageSize)
        );

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }

    @Override
    public List<Document> getUserCourseStatuses(String userId, List<String> courseIds) {
        MatchOperation match = Aggregation.match(Criteria.where("userId").is(userId).and("courseId").in(courseIds));

        ProjectionOperation project = Aggregation.project()
                .and("courseId").as("courseId")
                .and("status").as("status");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getMappedResults();
    }

    @Override
    public List<Document> getUserCertificates(String userId, String subPackageId, int offset, int pageSize) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is("COMPLETED")
                .and("certificateLink").ne("");

        if (subPackageId != null && !subPackageId.isBlank()) {
            criteria = criteria.and("subPackageId").is(subPackageId);
        }

        MatchOperation match = Aggregation.match(criteria);

        LookupOperation joinPackage = Aggregation.lookup("package", "packageId", "_id", "package");
        UnwindOperation unwind = Aggregation.unwind("package");

        MatchOperation matchEnabled = Aggregation.match(Criteria.where("package.packageStatus").is("ENABLED"));

        ProjectionOperation project = Aggregation.project()
                .and("packageId").as("packageId")
                .and("certificateLink").as("certificateLink")
                .and("imageCertificateLink").as("imageCertificateLink")
                .and("package.packageName").as("packageName")
                .and("package.thumbnailUrl").as("thumbnailUrl")
                .and("lastSynced").as("createdAt");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinPackage,
                unwind,
                matchEnabled,
                project,
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")),
                Aggregation.skip(offset),
                Aggregation.limit(pageSize)
        );

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }

    @Override
    public long countUserCertificates(String userId, String subPackageId) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is("COMPLETED")
                .and("certificateLink").ne("");

        if (subPackageId != null && !subPackageId.isBlank()) {
            criteria = criteria.and("subPackageId").is(subPackageId);
        }

        MatchOperation match = Aggregation.match(criteria);
        LookupOperation joinPackage = Aggregation.lookup("package", "packageId", "_id", "package");
        UnwindOperation unwind = Aggregation.unwind("package");
        MatchOperation matchEnabled = Aggregation.match(Criteria.where("package.packageStatus").is("ENABLED"));

        GroupOperation group = Aggregation.group().count().as("total");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinPackage,
                unwind,
                matchEnabled,
                group
        );

        Document result = mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getUniqueMappedResult();
        return result != null ? ((Number) result.get("total")).longValue() : 0L;
    }

    @Override
    public List<Document> getUserPackagesGroupedByStatus(String userId) {
        MatchOperation match = Aggregation.match(Criteria.where("userId").is(userId));

        LookupOperation joinPackage = Aggregation.lookup("package", "packageId", "_id", "package");
        UnwindOperation unwind = Aggregation.unwind("package");

        MatchOperation matchEnabled = Aggregation.match(Criteria.where("package.packageStatus").is("ENABLED"));

        ProjectionOperation project = Aggregation.project()
                .and("status").as("status")
                .and("packageId").as("id")
                .and("package.packageName").as("packageName")
                .and("package.price").as("price")
                .and("package.thumbnailUrl").as("thumbnailUrl");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinPackage,
                unwind,
                matchEnabled,
                project
        );

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }

    @Override
    public List<Document> getClientProductStatsByPackageIds(List<String> productIds, List<String> endUserIds, String search) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        // 1) Match Products by their IDs
        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(Aggregation.match(Criteria.where("_id").in(productIds)));

        // Optional search on productName
        if (search != null && !search.isBlank()) {
            ops.add(Aggregation.match(Criteria.where("productName").regex(search, "i")));
        }

        // 2) Lookup user_packages by Product ID
        // Assuming UserPackage.packageId will reference Product._id
        ops.add(Aggregation.lookup(USER_PACKAGES_TABLE, "_id", "packageId", "ups"));

        // 3) Compute enrolledUsers and completedUsers (filtered by given endUserIds, if provided)
        Document inListExpr = (endUserIds != null && !endUserIds.isEmpty())
                ? new Document("$in", List.of("$$u.userId", endUserIds))
                : new Document("$const", true);

        Document enrolledFilter = new Document("$filter", new Document("input", "$ups")
                .append("as", "u")
                .append("cond", inListExpr));

        Document completedCond = new Document("$and", List.of(
                inListExpr,
                new Document("$eq", List.of("$$u.status", "COMPLETED"))
        ));

        Document completedFilter = new Document("$filter", new Document("input", "$ups")
                .append("as", "u")
                .append("cond", completedCond));

        Document addFields = new Document()
                .append("productId", "$_id")  // Product ID
                .append("enrolledUsers", new Document("$size", enrolledFilter))
                .append("completedUsers", new Document("$size", completedFilter))
                .append("completionRate",
                        new Document("$cond", List.of(
                                new Document("$gt", List.of(new Document("$size", enrolledFilter), 0)),
                                new Document("$multiply", List.of(
                                        new Document("$divide", List.of(
                                                new Document("$size", completedFilter),
                                                new Document("$size", enrolledFilter)
                                        )),
                                        100
                                )),
                                0
                        ))
                );

        ops.add(context -> new Document("$addFields", addFields));

        // 4) Final projection using Product fields
        ProjectionOperation project = Aggregation.project()
                .and("productId").as("productId")
                .and("productName").as("packageName")  // Map to packageName for backward compatibility
                .and("productDescription").as("packageDescription")
                .and("productStatus").as("packageStatus")
                .and("thumbnailUrl").as("thumbnailUrl")
                .and("enrolledUsers").as("enrolledUsers")
                .and("completedUsers").as("completedUsers")
                .and("completionRate").as("completionRate");

        ops.add(project);

        Aggregation aggregation = Aggregation.newAggregation(ops);

        // Query the "product" collection directly
        return mongoTemplate.aggregate(aggregation, "product", Document.class).getMappedResults();
    }

    @Override
    public List<Document> getAnalyticsByPackageIdsAndUserIds(List<String> packageIds, List<String> endUserIds) {
        if (packageIds == null || packageIds.isEmpty() || endUserIds == null || endUserIds.isEmpty()) {
            return List.of();
        }

        MatchOperation match = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("packageId").in(packageIds),
                        Criteria.where("userId").in(endUserIds)
                )
        );

        GroupOperation group = Aggregation.group("packageId")
                .sum(ConditionalOperators.when(Criteria.where("status").is("COMPLETED")).then(1).otherwise(0)).as("completed")
                .sum(
                        ConditionalOperators.when(
                                Criteria.where("status").in(List.of("IN_PROGRESS", "EXAM"))
                        ).then(1).otherwise(0)
                ).as("inProgress")
                .sum(ConditionalOperators.when(Criteria.where("status").is("NOT_STARTED")).then(1).otherwise(0)).as("notStarted")
                .count().as("totalEnrolled");

        LookupOperation lookup = Aggregation.lookup("package", "_id", "_id", "packageInfo");
        UnwindOperation unwind = Aggregation.unwind("packageInfo");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("packageId")
                .and("completed").as("completed")
                .and("inProgress").as("inProgress")
                .and("notStarted").as("notStarted")
                .and("totalEnrolled").as("totalEnrolled")
                .and("packageInfo.packageName").as("packageName")
                .and(
                        ArithmeticOperators.Multiply.valueOf(
                                ArithmeticOperators.Divide.valueOf("completed")
                                        .divideBy(ConditionalOperators.ifNull("totalEnrolled").then(1))
                        ).multiplyBy(100)
                ).as("completionRate");

        Aggregation aggregation = Aggregation.newAggregation(match, group, lookup, unwind, project);

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }

    @Override
    public Map<String, String> getProductNamesByProductIds(List<String> productIds) {
        MatchOperation match = Aggregation.match(Criteria.where("_id").in(productIds));

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("productId")
                .and("packageName").as("productName");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(aggregation, "package", Document.class)
                .getMappedResults()
                .stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("productId"),
                        doc -> doc.getString("productName")
                ));
    }


    @Override
    public Map<String, String> getBundleNamesByPackageIds(List<String> packageIds) {
        MatchOperation match = Aggregation.match(Criteria.where("_id").in(packageIds));

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("packageId")
                .and("bundleName").as("bundleName");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(aggregation, "bundles", Document.class)
                .getMappedResults()
                .stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("packageId"),
                        doc -> doc.getString("bundleName"),
                        (existing, replacement) -> existing  // In case of duplicate packageId
                ));
    }

    @Override
    public List<Document> getPackagePerformanceStats(List<String> productIds, List<String> endUserIds) {
        if (productIds == null || productIds.isEmpty() || endUserIds == null || endUserIds.isEmpty()) {
            return List.of();
        }

        // Match by packageId (actually productId) and userId
        MatchOperation match = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("packageId").in(productIds),
                        Criteria.where("userId").in(endUserIds)
                )
        );

        // Group by packageId and calculate counts
        GroupOperation group = Aggregation.group("packageId")
                .count().as("assignedUsers")
                .sum(
                        ConditionalOperators.when(Criteria.where("status").is("COMPLETED"))
                                .then(1)
                                .otherwise(0)
                ).as("completedUsers")
                .sum(
                        ArrayOperators.Size.lengthOfArray(
                                ConditionalOperators.ifNull("completedCourseIds").thenValueOf("completedCourseIds")
                        )
                ).as("topicsCompleted");

        // Project fields
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("packageId")
                .and("assignedUsers").as("assignedUsers")
                .and("completedUsers").as("completedUsers")
                .and("topicsCompleted").as("topicsCompleted")
                .and(
                        ArithmeticOperators.Multiply.valueOf(
                                ArithmeticOperators.Divide.valueOf("completedUsers")
                                        .divideBy(ConditionalOperators.ifNull("assignedUsers").then(1))
                        ).multiplyBy(100)
                ).as("completionRate");

        // Final aggregation
        Aggregation aggregation = Aggregation.newAggregation(match, group, project);

        return mongoTemplate.aggregate(aggregation, USER_PACKAGES_TABLE, Document.class).getMappedResults();
    }

    @Override
    public List<Document> searchExamsWithPackageName(String search, int offset, int limit) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // Step 1: Lookup packages using packageId to fetch full package info
        pipeline.add(Aggregation.lookup("package", "packageId", "_id", "package"));
        pipeline.add(Aggregation.unwind("package", true)); // preserve null values if no match

        // Step 2: Match on package.packageName (case-insensitive)
        if (search != null && !search.isBlank()) {
            pipeline.add(Aggregation.match(Criteria.where("package.packageName").regex(search, "i")));
        }

        // Step 3: Project required fields
        pipeline.add(Aggregation.project()
                .andExpression("_id").as("examId")
                .and("packageId").as("packageId")
                .and("package.packageName").as("packageName")
                .and("title").as("title")
                .and("passingScore").as("passingScore")
                .and("questions").as("questions")
                .and("examDetails").as("examDetails")
        );

        // Step 4: Pagination
        pipeline.add(Aggregation.skip((long) offset));
        pipeline.add(Aggregation.limit(limit));

        // Step 5: Run aggregation
        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        return mongoTemplate.aggregate(aggregation, "package_exams", Document.class).getMappedResults();
    }


    @Override
    public long countExamsWithSearch(String search) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // Step 1: Lookup packages
        pipeline.add(Aggregation.lookup("package", "packageId", "_id", "package"));
        pipeline.add(Aggregation.unwind("package", true));

        // Step 2: Match on package.packageName
        if (search != null && !search.isBlank()) {
            pipeline.add(Aggregation.match(Criteria.where("package.packageName").regex(search, "i")));
        }

        // Step 3: Count matched documents
        pipeline.add(Aggregation.count().as("count"));

        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        Document result = mongoTemplate.aggregate(aggregation, "package_exams", Document.class)
                .getUniqueMappedResult();

        return result != null ? result.getInteger("count", 0) : 0;
    }


}
