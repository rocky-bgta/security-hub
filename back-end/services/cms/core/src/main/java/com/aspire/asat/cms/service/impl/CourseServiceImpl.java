package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.chapter.ChapterResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.ChapterDetailsDTO;
import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.ContentStatusDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseDetailsResponseDTO;
import com.aspire.asat.cms.dto.course.CourseRequestDto;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.course.CourseResponseWithItemDto;
import com.aspire.asat.cms.dto.course.CourseUpdateDto;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.ProductStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.product.ResponseDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Course;
import com.aspire.asat.cms.model.UserContentStatus;
import com.aspire.asat.cms.model.UserCourse;
import com.aspire.asat.cms.model.UserCourseProgress;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.CourseRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.UserContentStatusRepository;
import com.aspire.asat.cms.repository.UserCourseProgressRepository;
import com.aspire.asat.cms.repository.UserCourseRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.CourseRepositoryCustom;
import com.aspire.asat.cms.service.CourseService;
import com.aspire.asat.cms.util.CommonUtil;
import com.aspire.asat.common.exception.JsonException;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    // Constants for duplicated string literals
    private static final String COURSE_NOT_FOUND = "Course not found with id: ";
    private static final String COURSE_NAME = "courseName";
    private static final String CERTIFICATE_LINK = "certificateLink";

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final ProductRepository productRepository;
    private final ChapterServiceImpl chapterService;

    private final UserContentStatusRepository userContentStatusRepository;
    private final UserSubPackageRepository userSubPackageRepository;
    private final CourseRepositoryCustom courseRepositoryCustom;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;


    @Override
    public CourseResponseDto saveCourse(CourseRequestDto courseDto) {

        // Check unique course
        if (courseRepository.existsByCourseName(courseDto.getCourseName())) {
            throw new DuplicateNameException("Course already exists with name: " + courseDto.getCourseName());
        }

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        int totalContentCount = 0;

        Course course = Course.toCourse(id.toString(), courseDto, now, now, totalContentCount);
        return Course.toCourseDto(courseRepository.save(course));
    }

    @Override
    public List<CourseResponseDto> getAllCourses(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, COURSE_NAME));
        Page<Course> pageCourse;
        if (search != null && !search.isEmpty()) {
            pageCourse = courseRepository.findByCourseName(search, pageable);
        } else {
            pageCourse = courseRepository.findAll(pageable);
        }

        return pageCourse.getContent()
                .stream()
                .map(Course::toCourseDto)
                .toList();
    }

    @Override
    public List<CourseResponseDto> getAllCoursesByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.fromString(order), sortBy));
        Page<Course> courses = courseRepository.findByCourseStatus(CourseStatus.ENABLED, pageable);
        return courses.stream()
                .map(Course::toCourseDto)
                .toList();
    }

    @Override
    public CourseResponseDto updateCourseById(String courseId, CourseUpdateDto courseUpdateDto) {
        return courseRepository.findById(courseId)
                .map(existingCourse -> {
                    // Check if name is different
                    if (!existingCourse.getCourseName().equals(courseUpdateDto.getCourseName())) {
                        boolean isCourseNameExists = courseRepository.existsByCourseName(courseUpdateDto.getCourseName());
                        if (isCourseNameExists) {
                            throw new DuplicateNameException("Course already exists with name: " + courseUpdateDto.getCourseName());
                        }
                    }

                    Course updatedCourse = Course.toUpdateCourse(courseUpdateDto, courseId, Instant.now());
                    updatedCourse.setCreatedAt(existingCourse.getCreatedAt());
                    return Course.toCourseDto(courseRepository.save(updatedCourse));
                })
                .orElseThrow(() -> new ResourceNotFoundException(COURSE_NOT_FOUND + courseId));
    }


    @Override
    public CourseResponseWithItemDto getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(COURSE_NOT_FOUND + courseId));

        List<ChapterResponseDto> chapterDetails = chapterRepository.findAllById(course.getChapterIds())
                .stream()
                .map(chapter -> ChapterResponseDto.builder()
                        .id(chapter.getId())
//                        .courseId(chapter.getCourseId())
                        .chapterName(chapter.getChapterName())
                        .chapterDescription(chapter.getChapterDescription())
                        .position(chapter.getPosition())
                        .chapterStatus(chapter.getChapterStatus())
                        .contentIds(chapter.getContentIds())
                        .createdAt(chapter.getCreatedAt())
                        .updatedAt(chapter.getUpdatedAt())
                        .build())
                .toList();

        List<ResponseDto> productDetails = productRepository.findAllById(course.getProductIds())
                .stream()
                .filter(product -> product.getProductStatus() == ProductStatus.ENABLED)
                .map(product -> ResponseDto.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .productDescription(product.getProductDescription())
                        .productStatus(product.getProductStatus())
//                        .courseIds(product.getCourseIds())
                        .createdAt(product.getCreatedAt())
                        .updatedAt(product.getUpdatedAt())
                        .lastModifiedBy(product.getLastModifiedBy())
                        .build())
                .toList();

        return Course.toCourseDtoWithItemDetails(course, chapterDetails, productDetails);
    }

    @Override
    public List<Course> searchWithRelevance(String text) {
        return List.of();
    }

    @Override
    public void exportCoursesToCsv(Integer offset, Integer pageSize, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=course.csv");
        List<CourseResponseDto> courses = getAllCourses(null, offset, pageSize, "courseName", "asc");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"ID", "Course Name", "Course Description", "Course Status", "Chapter IDS", "Product IDS", "Created At", "Updated At"});
            for (CourseResponseDto course : courses) {
                writer.writeNext(new String[]{
                        course.getId(),
                        course.getCourseName(),
                        course.getCourseDescription(),
                        course.getCourseStatus().toString(),
                        course.getChapterIds().toString(),
                        course.getProductIds().toString(),
                        course.getCreatedAt().toString(),
                        course.getUpdatedAt().toString()
                });
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to export courses to csv: " + e.getMessage());
        }

    }

    @Override
    public long getTotalCourseCount() {
        return courseRepository.count();
    }

    public String deleteCourseById2(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));


        List<String> chapterIds = course.getChapterIds();
        for (String chapterId : chapterIds) {
            chapterService.deleteChapterById(chapterId);
        }
        String courseName = course.getCourseName();


        courseRepository.deleteById(id);
        return courseName;
    }

    @Override
    public CourseResponseDto deleteCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .map(course -> {
                    List<String> chapterIds = course.getChapterIds();
                    for (String chapterId : chapterIds) {
                        chapterService.deleteChapterById(chapterId);
                    }
                    courseRepository.delete(course);
                    return Course.toCourseDto(course);
                })
                .orElseThrow(() -> new ResourceNotFoundException(COURSE_NOT_FOUND + courseId));
    }

    @Override
    public List<String> deleteCoursesByIds(List<String> ids) {
        List<String> deletedNames = new ArrayList<>();
        for (String id : ids) {
            deletedNames.add(deleteCourseById2(id));
        }
        return deletedNames;
    }


    @Override
    public List<String> updateCoursesStatusByIds(List<String> ids, Status status) {
        List<String> updatedCourseNames = new ArrayList<>();
        for (String id : ids) {
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
            course.setCourseStatus(CourseStatus.valueOf(status.name()));
            courseRepository.save(course);
            updatedCourseNames.add(course.getCourseName());
        }
        return updatedCourseNames;
    }

    @Override
    public void exportBulkCourses(List<String> ids, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bulk_courses.csv");

        List<CourseResponseDto> courses = ids.stream()
                .map(id -> {
                    Course course = courseRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
                    return Course.toCourseDto(course);
                })
                .toList();

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            String[] header = {"ID", "Course Name", "Course Description", "Status", "Chapter IDs", "Product IDs", "Created At", "Updated At"};
            writer.writeNext(header);

            for (CourseResponseDto course : courses) {
                writer.writeNext(new String[]{
                        course.getId(),
                        course.getCourseName(),
                        course.getCourseDescription(),
                        course.getCourseStatus().toString(),
                        course.getChapterIds().toString(),
                        course.getProductIds().toString(),
                        course.getCreatedAt().toString(),
                        course.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new JsonException("Failed to export bulk Courses to CSV" + e);
        }
    }


    @Override
    public void assignCourseToUser(String userId, String courseId) {
        log.info("Assigning courseId={} to userId={}", courseId, userId);

        // Prevent duplicate assignment
        if (userCourseRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            log.warn("User {} is already assigned to course {}", userId, courseId);
            throw new IllegalStateException("User is already assigned to this course.");
        }

        // Save UserCourse
        UserCourse userCourse = new UserCourse();
        userCourse.setId(UUID.randomUUID().toString());
        userCourse.setUserId(userId);
        userCourse.setCourseId(courseId);
        userCourse.setStatus(CourseStatus.NOT_STARTED.toString());
        userCourse.setIsSaved(false);
        userCourse.setCertificateLink(null);
        userCourseRepository.save(userCourse);
        log.debug("UserCourse entry created for userId={}, courseId={}", userId, courseId);

        // Fetch course content hierarchy
        Document doc = courseRepositoryCustom.getFullCourseHierarchy(userId, courseId, null);
        if (doc == null) {
            log.error("Course structure not found for courseId={}", courseId);
            throw new ResourceNotFoundException("Course structure not found for courseId: " + courseId);
        }

        List<Document> contents = (List<Document>) doc.get("contents");
        if (contents == null || contents.isEmpty()) {
            log.error("No content found in courseId={} for userId={}", courseId, userId);
            throw new ResourceNotFoundException("No content found in course to assign to user");
        }

        List<String> contentIds = contents.stream()
                .map(c -> c.getString("_id"))
                .filter(Objects::nonNull)
                .toList();

        // Save UserCourseProgress
        UserCourseProgress progress = new UserCourseProgress(
                userId,
                courseId,
                0.0,
                List.of(),
                Instant.now()
        );
        progress.setId(UUID.randomUUID().toString());
        userCourseProgressRepository.save(progress);
        log.debug("UserCourseProgress initialized for userId={}, courseId={}", userId, courseId);

        // Save UserContentStatus
        List<UserContentStatus> statusList = contentIds.stream().map(contentId -> {
            UserContentStatus ucs = new UserContentStatus();
            ucs.setId(UUID.randomUUID().toString());
            ucs.setUserId(userId);
            ucs.setCourseId(courseId);
            ucs.setContentId(contentId);
            ucs.setIsDone(false);
            return ucs;
        }).toList();

        userContentStatusRepository.saveAll(statusList);
        log.info("Assigned {} contents to userId={} for courseId={}", statusList.size(), userId, courseId);
    }

    @Override
    public void bookmarkCourse(String courseId, String userId, String packageId, boolean isSaved) {
        log.debug("Updating bookmark status for userId={}, courseId={}, isSaved={}", userId, courseId, isSaved);

        UserCourse userCourse = userCourseRepository
                .findByUserIdAndCourseIdAndPackageId(userId, courseId, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("User course not found for userId=" + userId + ", courseId=" + courseId));

        // Update the bookmark status (isSaved)
        userCourse.setIsSaved(isSaved);  // Correct setter method

        userCourseRepository.save(userCourse);

        log.debug("Bookmark status updated for userId={}, courseId={}", userId, courseId);
    }

    @Override
    public CourseDetailsResponseDTO getCourseDetails(String courseId, String userId, String packageId) {
        log.info("Fetching course details for userId={} and courseId={}", userId, courseId);

        Document doc = courseRepositoryCustom.getFullCourseHierarchy(userId, courseId, packageId);
        if (doc == null) {
            log.warn("No course data found for userId={} and courseId={}", userId, courseId);
            throw new ResourceNotFoundException("Course data not found for user " + userId + " and courseId " + courseId);
        }

        String courseName = doc.getString("courseName");
        String courseDescription = doc.getString("courseDescription");
        String thumbnailUrl = doc.getString("thumbnailUrl");
        List<Document> chapters = (List<Document>) doc.get("chapters");
        List<Document> contents = (List<Document>) doc.get("contents");

        List<Document> contentStatus = ((List<Document>) doc.get("contentStatus")).stream()
                .filter(status -> userId.equals(status.getString("userId")))
                .toList();

        Set<String> completedIds = contentStatus.stream()
                .filter(status -> Boolean.TRUE.equals(status.getBoolean("isDone")))
                .map(status -> status.getString("contentId"))
                .collect(Collectors.toSet());

        Map<String, Document> contentMap = contents.stream()
                .collect(Collectors.toMap(c -> c.getString("_id"), c -> c));

        List<ChapterDetailsDTO> chapterDetails = chapters.stream().map(ch -> {
            List<String> contentIds = (List<String>) ch.get("contentIds");
            List<ContentStatusDTO> contentItems = contentIds.stream()
                    .map(cid -> {
                        Document cDoc = contentMap.get(cid);
                        if (cDoc == null) return null;
                        return new ContentStatusDTO(
                                cDoc.getString("_id"),
                                cDoc.getString("contentName"),
                                cDoc.get("contentType") != null ? cDoc.get("contentType").toString() : null,
                                completedIds.contains(cDoc.getString("_id"))
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return new ChapterDetailsDTO(
                    ch.getString("_id"),
                    ch.getString("chapterName"),
                    ch.getString("chapterDescription"),
                    contentItems
            );
        }).toList();

        int contentCount = contents.size();
        int chapterCount = chapters.size();
        double progress = contentCount == 0 ? 0.0 : (completedIds.size() * 100.0) / contentCount;

        log.debug("Course loaded: name='{}', chapters={}, contents={}, progress={}% for userId={}",
                courseName, chapterCount, contentCount, progress, userId);

        String certLink = doc.getString("certificateLink") != null
                ? doc.getString("certificateLink")
                : "https://strgblobstandardasatv2.blob.core.windows.net/strgblobstandardasatv2/cer_link_default.png";

        String imageCertLink = doc.getString("ImageCertificateLink") != null
                ? doc.getString("ImageCertificateLink")
                : "https://strgblobstandardasatv2.blob.core.windows.net/strgblobstandardasatv2/cer_link_default.png";

        if (certLink.contains("default")) {
            log.debug("Default certificate link used for courseId={}", courseId);
        }

        return new CourseDetailsResponseDTO(
                courseId,
                courseName,
                courseDescription,
                null,
                progress,
                chapterCount,
                contentCount,
                chapterDetails,
                thumbnailUrl,
                certLink,
                imageCertLink
        );
    }


    @Override
    public List<ClientCourseResponseDTO> getUserCourses(String userId, String packageId, String status, String search, Boolean isSaved, int offset, int pageSize) {
        log.info("Fetching user courses for userId={}, packageId={}, status={}, search={}", userId, packageId, status, search);

        try {
            offset = CommonUtil.getOffset(offset, pageSize);
            List<Document> docs;

            if (!CommonUtil.isNullOrBlank(packageId)) {
                boolean hasAccess = userSubPackageRepository.existsByUserIdAndSubPackageId(userId, packageId);
                if (!hasAccess) {
                    log.warn("User {} does not have access to package {}", userId, packageId);
                    return List.of();
                }

                docs = courseRepositoryCustom.getUserCoursesByPackage(userId, packageId, status, search, isSaved, offset, pageSize);
                log.debug("Courses fetched by package {} for userId={}: count={}", packageId, userId, docs.size());
            } else {
                docs = courseRepositoryCustom.getUserCourses(userId, status, search, isSaved, offset, pageSize);
                log.debug("Courses fetched by userId={} without package filter: count={}", userId, docs.size());
            }

            List<UserCourseProgress> progressList = userCourseProgressRepository.findByUserId(userId);
            Map<String, UserCourseProgress> progressMap = progressList.stream()
                    .collect(Collectors.toMap(
                            p -> p.getCourseId() + "::" + p.getPackageId(), // Composite key
                            p -> p,
                            (existing, replacement) -> existing // avoid duplicate key crash
                    ));

            return docs.stream().map(doc -> {
                String courseId = doc.getString("courseId");
                String pkgId = doc.getString("packageId"); // Ensure this is returned in query projection
                String key = courseId + "::" + pkgId;
                UserCourseProgress progress = progressMap.get(key);

                List<String> completedIds = progress != null ? progress.getCompletedContentIds() : List.of();
                List<String> chapterIds = castList(doc.get("chapterIds"));

                int completedContentCount = completedIds.size();
                int totalChapterCount = chapterIds.size();
                double progressVal = progress != null ? progress.getProgress() : 0.0;
                int totalContentCount = doc.get("totalContentCount") instanceof Number
                        ? ((Number) doc.get("totalContentCount")).intValue() : 0;

                Instant createdDate = Optional.ofNullable(doc.get("createdAt", Date.class))
                        .map(Date::toInstant).orElse(Instant.now());

                return new ClientCourseResponseDTO(
                        courseId,
                        doc.getString("courseName"),
                        totalContentCount,
                        completedContentCount,
                        totalChapterCount,
                        progressVal,
                        doc.getString("status"),
                        Boolean.TRUE.equals(doc.get("isSaved")),
                        doc.getString("certificateLink"),
                        false,
                        doc.getOrDefault("thumbnailUrl", "").toString(),
                        createdDate
                );
            }).toList();

        } catch (Exception e) {
            log.error("Failed to fetch user courses for userId={}", userId, e);
            return List.of();
        }
    }

    private List<String> castList(Object obj) {
        return obj instanceof List<?> ? (List<String>) obj : List.of();
    }


    @Override
    public long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved) {
        log.debug("Counting courses for packageId={}, status={}, search={}, isSaved={}",
                packageId, status, search, isSaved);

        long count = courseRepositoryCustom.countUserCoursesByPackage(packageId, status, search, isSaved);

        log.debug("Course count result for packageId={} is {}", packageId, count);
        return count;
    }

    @Override
    public long countUserCourses(String userId, String packageId, String status, String search, Boolean isSaved) {
        log.debug("Counting user courses for userId={}, packageId={}, status={}, search={}, isSaved={}",
                userId, packageId, status, search, isSaved);

        long count = courseRepositoryCustom.countUserCourses(userId, status, search, isSaved);

        log.debug("Course count result for userId={} is {}", userId, count);
        return count;
    }
}
