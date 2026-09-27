package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.repository.custom.CourseRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    /**
     * Retrieves a list of user courses based on the provided filters and constraints. This method performs
     * a database aggregation to fetch course-related details, progress, and content completion status for
     * a specific user. The results are paginated based on the given offset and page size.
     *
     * @param userId      The unique identifier of the user for whom the courses are fetched. Must not be null.
     * @param status      The status of the user courses to retrieve (e.g., "completed", "in-progress").
     *                    Can be null to include courses of any status.
     * @param search      A optional search string to filter courses by name using partial matching.
     *                    Can be null or blank if no search filter is applied.
     * @param isSaved     A Boolean flag to filter courses based on whether they are saved by the user.
     *                    Can be null to include both saved and non-saved courses.
     * @param offset      The number of courses to skip for pagination. Must be a non-negative integer.
     * @param pageSize    The maximum number of courses to include in the result. Must be a positive integer.
     * @return A list of documents representing the fetched user courses, including associated
     *                    details such as progress, course information, and content completion. Returns an empty
     *                    list if no courses match the criteria.
     */
    @Override
    public List<Document> getUserCourses(String userId, String status, String search, Boolean isSaved, int offset, int pageSize) {
        Criteria criteria = Criteria.where("userId").is(userId);
        if (status != null && !status.isBlank()) criteria = criteria.and("status").is(status);
        if (isSaved != null) criteria = criteria.and("isSaved").is(isSaved);

        MatchOperation match = Aggregation.match(criteria);

        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        // Unwind course to allow filtering and direct access
        UnwindOperation unwindCourse = Aggregation.unwind("course");

        MatchOperation matchCourseStatus = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        MatchOperation matchSearch = null;
        if (search != null && !search.isBlank()) {
            matchSearch = Aggregation.match(Criteria.where("course.courseName").regex(search, "i"));
        }

        ProjectionOperation project = Aggregation.project()
                .and("courseId").as("courseId")
                .and("course.courseName").as("courseName")
                .and("course.thumbnailUrl").as("thumbnailUrl")
                .and("course.createdAt").as("createdAt")
                .and("course.updatedAt").as("updatedAt")
                .and("course.totalContentCount").as("totalContentCount")
                .and("course.chapterIds").as("chapterIds")
                .and("status").as("status")
                .and("certificateLink").as("certificateLink")
                .and("isSaved").as("isSaved");

        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match);
        pipeline.add(joinCourse);
        pipeline.add(unwindCourse);
        pipeline.add(matchCourseStatus);
        if (matchSearch != null) pipeline.add(matchSearch);
        pipeline.add(project);
        pipeline.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")));
        pipeline.add(Aggregation.skip(offset));
        pipeline.add(Aggregation.limit(pageSize));

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getMappedResults();
    }





    /**
     * Retrieves a paginated list of user courses associated with a specific package.
     * This method fetches courses linked to the given package ID, applies optional filters
     * such as status, search query, and saved status, and returns the results with pagination.
     *
     * @param packageId The unique identifier of the package whose user courses are to be retrieved.
     * @param status An optional parameter representing the status of the courses to filter.
     *               If null or blank, this filter is not applied.
     * @param search An optional search string to filter courses by name. Matches are case-insensitive.
     *               If null or blank, this filter is not applied.
     * @param isSaved An optional Boolean indicating whether to filter courses marked as saved.
     *                If null, this filter is not applied.
     * @param offset The number of items to skip for pagination purposes.
     * @param pageSize The maximum number of courses to retrieve in the current page.
     * @return A list of {@link Document}, each representing a user course with associated details,
     *         including course name, thumbnail URL, progress, and other metadata.
     */
    @Override
    public List<Document> getUserCoursesByPackage(String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize) {
        // Fetch package to get courseIds
        Document pkg = mongoTemplate.findById(packageId, Document.class, "package");
        if (pkg == null || pkg.get("courseIds") == null) return Collections.emptyList();

        @SuppressWarnings("unchecked")
        List<String> courseIds = (List<String>) pkg.get("courseIds");
        if (courseIds.isEmpty()) return Collections.emptyList();

        // Match by userId, packageId, and courseIds
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("packageId").is(packageId)
                .and("courseId").in(courseIds);

        if (status != null && !status.isBlank()) {
            criteria = criteria.and("status").is(status);
        }
        if (isSaved != null) {
            criteria = criteria.and("isSaved").is(isSaved);
        }

        MatchOperation match = Aggregation.match(criteria);

        // Join with course collection
        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        UnwindOperation unwindCourse = Aggregation.unwind("course");

        // Only include enabled courses
        MatchOperation matchCourseStatus = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        // Optional search on course name
        MatchOperation matchSearch = null;
        if (search != null && !search.isBlank()) {
            matchSearch = Aggregation.match(Criteria.where("course.courseName").regex(search, "i"));
        }

        // Project only required fields
        ProjectionOperation project = Aggregation.project()
                .and("courseId").as("courseId")
                .and("packageId").as("packageId")
                .and("course.courseName").as("courseName")
                .and("course.thumbnailUrl").as("thumbnailUrl")
                .and("course.createdAt").as("createdAt")
                .and("course.updatedAt").as("updatedAt")
                .and("course.totalContentCount").as("totalContentCount")
                .and("course.chapterIds").as("chapterIds")
                .and("status").as("status")
                .and("certificateLink").as("certificateLink")
                .and("isSaved").as("isSaved");

        // Group to avoid duplicates (same course across multiple docs)
        GroupOperation groupByCourseId = Aggregation.group("courseId")
                .first("courseId").as("courseId")
                .first("packageId").as("packageId")
                .first("courseName").as("courseName")
                .first("thumbnailUrl").as("thumbnailUrl")
                .first("createdAt").as("createdAt")
                .first("updatedAt").as("updatedAt")
                .first("totalContentCount").as("totalContentCount")
                .first("chapterIds").as("chapterIds")
                .first("status").as("status")
                .first("certificateLink").as("certificateLink")
                .first("isSaved").as("isSaved");

        // Build pipeline
        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match);
        pipeline.add(joinCourse);
        pipeline.add(unwindCourse);
        pipeline.add(matchCourseStatus);
        if (matchSearch != null) pipeline.add(matchSearch);
        pipeline.add(project);
        pipeline.add(groupByCourseId);
        pipeline.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")));
        pipeline.add(Aggregation.skip(offset));
        pipeline.add(Aggregation.limit(pageSize));

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getMappedResults();
    }







    /**
     * Counts the number of courses associated with a specific user, applying optional filters.
     * This method retrieves the user courses based on the user ID and applies additional filters
     * such as status, search text, and saved status, then returns the total count.
     *
     * @param userId The unique identifier of the user whose courses are to be counted.
     * @param status An optional parameter representing the status of the courses to filter.
     *               If null or blank, this filter is not applied.
     * @param search An optional search string to filter courses by their names. Matches are case-insensitive.
     *               If null or blank, this filter is not applied.
     * @param isSaved An optional Boolean indicating whether to filter courses that are marked as saved.
     *                If null, this filter is not applied.
     * @return The total count of courses associated with the user after applying the specified filters.
     */
    @Override
    public long countUserCourses(String userId, String status, String search, Boolean isSaved) {
        // Step 1: Match by userId, optional status and isSaved
        Criteria criteria = Criteria.where("userId").is(userId);

        if (status != null && !status.isBlank()) {
            criteria = criteria.and("status").is(status);
        }

        if (isSaved != null) {
            criteria = criteria.and("isSaved").is(isSaved);
        }

        MatchOperation match = Aggregation.match(criteria);

        // Step 2: Lookup course to join metadata
        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        // Step 3: Unwind the course array
        UnwindOperation unwind = Aggregation.unwind("course");

        // Step 4: Filter for ENABLED courses
        MatchOperation matchCourseStatus = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        // Step 5: Optional search by courseName
        MatchOperation matchSearch = null;
        if (search != null && !search.isBlank()) {
            matchSearch = Aggregation.match(
                    Criteria.where("course.courseName").regex(search, "i")
            );
        }

        // Step 6: Group to count
        GroupOperation groupCount = Aggregation.group().count().as("total");

        // Step 7: Build aggregation pipeline
        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match);
        pipeline.add(joinCourse);
        pipeline.add(unwind);
        pipeline.add(matchCourseStatus);
        if (matchSearch != null) pipeline.add(matchSearch);
        pipeline.add(groupCount);

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        // Step 8: Execute and extract count
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "user_courses", Document.class);
        Document doc = results.getUniqueMappedResult();

        return doc != null ? ((Number) doc.get("total")).longValue() : 0L;
    }



    /**
     * Counts the number of user courses associated with a specific package, applying optional filters.
     * This method retrieves the courses linked to a given package ID and applies additional filters
     * such as status, search text, and saved status, before returning the total count.
     *
     * @param packageId The unique identifier of the package whose user courses are to be counted.
     * @param status An optional parameter representing the status of the courses to filter.
     *               If null or blank, this filter is not applied.
     * @param search An optional search string to filter courses by their name. Matches are case-insensitive.
     *               If null or blank, this filter is not applied.
     * @param isSaved An optional Boolean indicating whether to filter courses that are marked as saved.
     *                If null, this filter is not applied.
     * @return The total count of user courses associated with the given package,
     *         after applying the specified filters.
     */
    @Override
    public long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved) {
        // Step 1: Fetch courseIds from package document
        Document pkg = mongoTemplate.findById(packageId, Document.class, "package");
        if (pkg == null || pkg.get("courseIds") == null) return 0;

        @SuppressWarnings("unchecked")
        List<String> courseIds = (List<String>) pkg.get("courseIds");
        if (courseIds.isEmpty()) return 0;

        // Step 2: Base filter for user_courses by courseIds
        Criteria criteria = Criteria.where("courseId").in(courseIds);
        if (status != null && !status.isBlank()) {
            criteria = criteria.and("status").is(status);
        }
        if (isSaved != null) {
            criteria = criteria.and("isSaved").is(isSaved);
        }

        MatchOperation match = Aggregation.match(criteria);

        // Step 3: Join with course collection
        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        UnwindOperation unwindCourse = Aggregation.unwind("course");

        MatchOperation matchCourseStatus = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        // Step 4: Optional search
        MatchOperation matchSearch = null;
        if (search != null && !search.isBlank()) {
            matchSearch = Aggregation.match(Criteria.where("course.courseName").regex(search, "i"));
        }

        // Step 5: Group by courseId to count unique courses
        GroupOperation groupByCourseId = Aggregation.group("courseId");

        // Step 6: Count the number of unique groups
        GroupOperation finalCount = Aggregation.group().count().as("total");

        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match);
        pipeline.add(joinCourse);
        pipeline.add(unwindCourse);
        pipeline.add(matchCourseStatus);
        if (matchSearch != null) pipeline.add(matchSearch);
        pipeline.add(groupByCourseId);
        pipeline.add(finalCount);

        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        Document result = mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getUniqueMappedResult();

        return result != null ? ((Number) result.get("total")).longValue() : 0L;
    }



    /**
     * Retrieves a paginated list of completed certificates for a specific user.
     * This method filters the certificates based on the user ID and optionally by course ID
     * and retrieves a selected set of fields for each certificate.
     *
     * @param userId The unique identifier of the user whose certificates are to be retrieved.
     * @param courseId An optional parameter representing the course ID to filter the certificates.
     *                 If null or blank, this filter is not applied.
     * @param offset The number of items to skip for pagination purposes.
     * @param pageSize The maximum number of certificates to retrieve in the current page.
     * @return A list of {@link Document}, each representing a certificate, with selected fields
     *         such as course name, thumbnail URL, certificate link, and creation date.
     */
    @Override
    public List<Document> getUserCertificates(String userId, String courseId, int offset, int pageSize) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is("COMPLETED")
                .and("certificateLink").ne("");

        if (courseId != null && !courseId.isBlank()) {
            criteria = criteria.and("courseId").is(courseId);
        }

        MatchOperation match = Aggregation.match(criteria);

        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        // 🔹 Required to access joined course fields
        UnwindOperation unwindCourse = Aggregation.unwind("course");

        // 🔹 (Optional) Only include ENABLED courses
        MatchOperation matchCourseStatus = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        ProjectionOperation project = Aggregation.project()
                .and("courseId").as("courseId")
                .and("certificateLink").as("certificateLink")
                .and("ImageCertificateLink").as("ImageCertificateLink")
                .and("course.courseName").as("courseName")
                .and("course.thumbnailUrl").as("thumbnailUrl")
                .and("course.createdAt").as("createdAt");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinCourse,
                unwindCourse,
                matchCourseStatus, // optional
                project,
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")),
                Aggregation.skip(offset),
                Aggregation.limit(pageSize)
        );

        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getMappedResults();
    }


    /**
     * Counts the number of completed user certificates based on specific criteria.
     * This method filters completed certificates of a particular user, optionally filtered
     * by a specific course, by querying the "user_courses" collection.
     *
     * @param userId The unique identifier of the user whose certificates are to be counted.
     * @param courseId An optional parameter representing the course ID to filter the certificates.
     *                 If null or blank, this filter is not applied.
     * @return The total count of completed certificates for the user, optionally filtered by the course ID.
     */
    @Override
    public long countUserCertificates(String userId, String courseId) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is("COMPLETED")
                .and("certificateLink").ne("");

        if (courseId != null && !courseId.isBlank()) {
            criteria = criteria.and("courseId").is(courseId);
        }

        MatchOperation match = Aggregation.match(criteria);

        LookupOperation joinCourse = LookupOperation.newLookup()
                .from("course")
                .localField("courseId")
                .foreignField("_id")
                .as("course");

        UnwindOperation unwindCourse = Aggregation.unwind("course");

        // 🔹 Only count if the course is ENABLED
        MatchOperation matchEnabledCourses = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));

        GroupOperation group = Aggregation.group().count().as("total");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinCourse,
                unwindCourse,
                matchEnabledCourses,
                group
        );

        Document result = mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getUniqueMappedResult();

        return result != null ? ((Number) result.get("total")).longValue() : 0L;
    }



    /**
     * Aggregates and retrieves a summary of the user's dashboard data, including course statuses
     * and certificate counts.
     *
     * @param userId The unique identifier of the user whose dashboard summary is to be retrieved.
     * @return A {@link Document} containing the aggregated summary of the user's courses and certificates.
     *         The summary includes total courses, completed courses, in-progress courses, pending courses,
     *         and total certificates.
     */
    @Override
    public Document getUserDashboardSummary(String userId) {
        // -------- Step 1: Aggregate status count with ENABLED course filter --------
        MatchOperation matchByUser = Aggregation.match(Criteria.where("userId").is(userId));
        LookupOperation joinCourse = Aggregation.lookup("course", "courseId", "_id", "course");
        UnwindOperation unwindCourse = Aggregation.unwind("course");
        MatchOperation matchEnabledCourses = Aggregation.match(Criteria.where("course.courseStatus").is("ENABLED"));
        GroupOperation groupByStatus = Aggregation.group("status").count().as("count");

        Aggregation statusAgg = Aggregation.newAggregation(
                matchByUser,
                joinCourse,
                unwindCourse,
                matchEnabledCourses,
                groupByStatus
        );

        List<Document> statusCounts = mongoTemplate.aggregate(statusAgg, "user_courses", Document.class).getMappedResults();

        // -------- Step 2: Count certificates with ENABLED course check --------
        MatchOperation matchCompletedWithCertificate = Aggregation.match(Criteria.where("userId").is(userId)
                .and("status").is("COMPLETED")
                .and("certificateLink").ne(""));

        Aggregation certAgg = Aggregation.newAggregation(
                matchCompletedWithCertificate,
                joinCourse,
                unwindCourse,
                matchEnabledCourses,
                Aggregation.group().count().as("certificateCount")
        );

        Document certResult = mongoTemplate.aggregate(certAgg, "user_courses", Document.class).getUniqueMappedResult();

        // -------- Step 3: Prepare final result --------
        Document result = new Document();
        int total = 0, completed = 0, inProgress = 0, pending = 0;

        for (Document doc : statusCounts) {
            String status = doc.getString("_id");
            int count = doc.getInteger("count", 0);
            total += count;

            switch (status) {
                case "COMPLETED" -> completed = count;
                case "IN_PROGRESS" -> inProgress = count;
                case "NOT_STARTED" -> pending = count;
            }
        }

        int certificateCount = certResult != null ? certResult.getInteger("certificateCount", 0) : 0;

        result.append("totalCourses", total);
        result.append("completedCourses", completed);
        result.append("inProgressCourses", inProgress);
        result.append("pendingCourses", pending);
        result.append("totalCertificates", certificateCount);

        return result;
    }



    /**
     * Retrieves the full hierarchy of a course for a specific user. This includes course details,
     * chapters, contents, content statuses, and user progress.
     *
     * @param userId the ID of the user for whom the course hierarchy is being fetched
     * @param courseId the ID of the course whose hierarchy is being fetched
     * @return a Document containing the full course hierarchy, including course details,
     *         chapters, contents, related statuses, and user course progress
     */
//    @Override
//    public Document getFullCourseHierarchy(String userId, String courseId, String packageId) {
//        MatchOperation match = Aggregation.match(
//                Criteria.where("userId").is(userId)
//                        .and("courseId").is(courseId)
//                        .and("packageId").is(packageId)
//        );
//
//        LookupOperation joinCourse = Aggregation.lookup("course", "courseId", "_id", "course");
//        UnwindOperation unwindCourse = Aggregation.unwind("course");
//
//        AddFieldsOperation addChapterIds = AddFieldsOperation.builder()
//                .addFieldWithValue("chapterIds", "$course.chapterIds")
//                .build();
//
//        LookupOperation joinChapters = Aggregation.lookup("chapter", "chapterIds", "_id", "chapters");
//
//        AddFieldsOperation addContentIds = AddFieldsOperation.builder()
//                .addFieldWithValue("contentIds",
//                        new Document("$reduce",
//                                new Document("input", "$chapters.contentIds")
//                                        .append("initialValue", Collections.emptyList())
//                                        .append("in", new Document("$concatArrays", List.of("$$value", "$$this")))))
//                .build();
//
//        LookupOperation joinContents = Aggregation.lookup("content", "contentIds", "_id", "contents");
//
//        // Directly join user_content_status by userId + courseId + packageId
//        LookupOperation joinContentStatus = Aggregation.lookup(
//                "user_content_status",
//                "contentIds",   // localField
//                "contentId",    // foreignField
//                "contentStatus"
//        );
//
//        // Directly join user_course_progress by courseId + packageId (we'll filter in project)
//        LookupOperation joinProgress = Aggregation.lookup(
//                "user_course_progress",
//                "courseId",
//                "courseId",
//                "progress"
//        );
//
//        ProjectionOperation project = Aggregation.project()
//                .and("courseId").as("courseId")
//                .and("packageId").as("packageId")
//                .and("status").as("status")
//                .and("isSaved").as("isSaved")
//                .and("certificateLink").as("certificateLink")
//                .and("ImageCertificateLink").as("ImageCertificateLink")
//                .and("course.courseName").as("courseName")
//                .and("course.courseDescription").as("courseDescription")
//                .and("course.thumbnailUrl").as("thumbnailUrl")
//                .and("chapters").as("chapters")
//                .and("contents").as("contents")
//                .and("contentStatus").as("contentStatus")
//                .and(ArrayOperators.ArrayElemAt.arrayOf("progress").elementAt(0)).as("progress");
//
//        Aggregation aggregation = Aggregation.newAggregation(
//                match,
//                joinCourse,
//                unwindCourse,
//                addChapterIds,
//                joinChapters,
//                addContentIds,
//                joinContents,
//                joinContentStatus,
//                joinProgress,
//                project
//        );
//
//        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getUniqueMappedResult();
//    }
    @Override
    public Document getFullCourseHierarchy(String userId, String courseId, String packageId) {
        MatchOperation match = Aggregation.match(
                Criteria.where("userId").is(userId)
                        .and("courseId").is(courseId)
                        .and("packageId").is(packageId)
        );

        LookupOperation joinCourse = Aggregation.lookup("course", "courseId", "_id", "course");
        UnwindOperation unwindCourse = Aggregation.unwind("course");

        AddFieldsOperation addChapterIds = AddFieldsOperation.builder()
                .addFieldWithValue("chapterIds", "$course.chapterIds")
                .build();

        LookupOperation joinChapters = Aggregation.lookup("chapter", "chapterIds", "_id", "chapters");

        // Flatten all chapter contentIds -> contentIds
        AddFieldsOperation addContentIds = AddFieldsOperation.builder()
                .addFieldWithValue("contentIds",
                        new Document("$reduce",
                                new Document("input", "$chapters.contentIds")
                                        .append("initialValue", Collections.emptyList())
                                        .append("in", new Document("$concatArrays", List.of("$$value", "$$this")))))
                .build();

        // Pull all contents by those ids
        LookupOperation joinContents = Aggregation.lookup("content", "contentIds", "_id", "contents");

        // Pull all status rows by contentId (we'll filter next)
        LookupOperation joinContentStatus = Aggregation.lookup(
                "user_content_status", "contentIds", "contentId", "contentStatus"
        );

        // Keep only this user's/course's/package's rows AND ensure the contentId is in contentIds
        AddFieldsOperation keepOnlyThisUsersStatus = AddFieldsOperation.builder()
                .addFieldWithValue("contentStatus",
                        new Document("$filter", new Document()
                                .append("input", "$contentStatus")
                                .append("as", "cs")
                                .append("cond", new Document("$and", List.of(
                                        new Document("$eq", List.of("$$cs.userId", userId)),
                                        new Document("$eq", List.of("$$cs.courseId", courseId)),
                                        new Document("$eq", List.of("$$cs.packageId", packageId)),
                                        new Document("$in", List.of("$$cs.contentId", "$contentIds"))
                                )))
                        )
                ).build();

        // Assigned content ids = ids that have a status row for this user/course/package
        AddFieldsOperation addAssignedContentIds = AddFieldsOperation.builder()
                .addFieldWithValue("assignedContentIds",
                        new Document("$map", new Document()
                                .append("input", "$contentStatus")
                                .append("as", "cs")
                                .append("in", "$$cs.contentId")
                        )
                ).build();

        // Filter contents to assigned ids only
        AddFieldsOperation filterContentsToAssigned = AddFieldsOperation.builder()
                .addFieldWithValue("contents",
                        new Document("$filter", new Document()
                                .append("input", "$contents")
                                .append("as", "c")
                                .append("cond", new Document("$in", List.of("$$c._id", "$assignedContentIds")))
                        )
                ).build();

        // Progress: lookup all then pick first for this user+package
        LookupOperation joinProgress = Aggregation.lookup(
                "user_course_progress", "courseId", "courseId", "progressRaw"
        );

        AddFieldsOperation setProgress = AddFieldsOperation.builder()
                .addFieldWithValue("progress",
                        new Document("$arrayElemAt", List.of(
                                new Document("$filter", new Document()
                                        .append("input", "$progressRaw")
                                        .append("as", "p")
                                        .append("cond", new Document("$and", List.of(
                                                new Document("$eq", List.of("$$p.userId", userId)),
                                                new Document("$eq", List.of("$$p.packageId", packageId))
                                        )))
                                ),
                                0
                        ))
                ).build();

        ProjectionOperation project = Aggregation.project()
                .and("courseId").as("courseId")
                .and("packageId").as("packageId")
                .and("status").as("status")
                .and("isSaved").as("isSaved")
                .and("certificateLink").as("certificateLink")
                .and("ImageCertificateLink").as("ImageCertificateLink")
                .and("course.courseName").as("courseName")
                .and("course.courseDescription").as("courseDescription")
                .and("course.thumbnailUrl").as("thumbnailUrl")
                .and("chapters").as("chapters")
                .and("contents").as("contents")          // now filtered to assigned only
                .and("contentStatus").as("contentStatus") // filtered to this user/course/package
                .and("progress").as("progress");          // first progress row for this user+package

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinCourse, unwindCourse,
                addChapterIds, joinChapters,
                addContentIds, joinContents,
                joinContentStatus, keepOnlyThisUsersStatus,
                addAssignedContentIds, filterContentsToAssigned,
                joinProgress, setProgress,
                project
        );

        return mongoTemplate.aggregate(aggregation, "user_courses", Document.class).getUniqueMappedResult();
    }





}
