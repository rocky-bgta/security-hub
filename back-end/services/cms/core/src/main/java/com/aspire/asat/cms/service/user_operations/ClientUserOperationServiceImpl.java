package com.aspire.asat.cms.service.user_operations;

import com.aspire.asat.cms.dto.client.responseDto.ChapterDetailsDTO;
import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CompletedCourseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CompletedTopicResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.ContentStatusDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CourseTopicStatsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageExamDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.SubPackageStatisticsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageListResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageStatisticsDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductIdListResponseDto;
import com.aspire.asat.cms.dto.dashboard.UserDashboardTopicProgressItemDto;
import com.aspire.asat.cms.dto.dashboard.UserDashboardTopicsProgressResponseDto;
import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.notification.CourseCompletionNotificationRequest;
import com.aspire.asat.cms.dto.user.UserSubPackageAssignRequest;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Chapter;
import com.aspire.asat.cms.model.Course;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.Package;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserContentStatus;
import com.aspire.asat.cms.model.UserCourse;
import com.aspire.asat.cms.model.UserCourseProgress;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.model.UserTopicProgress;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.ContentRepository;
import com.aspire.asat.cms.repository.CourseRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.PackageRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserContentStatusRepository;
import com.aspire.asat.cms.repository.UserCourseProgressRepository;
import com.aspire.asat.cms.repository.UserCourseRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.UserTopicProgressRepository;
import com.aspire.asat.cms.repository.custom.CourseRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.TrainingRiskScoreSyncService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.util.AzureCertificateUploader;
import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.cms.util.CertificateGenerator;
import com.aspire.asat.cms.util.CommonUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientUserOperationServiceImpl implements ClientUserOperationService {

    @Value("${service.registration.url}")
    private String registrationUrl;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final UserContentStatusRepository userContentStatusRepository;
    private final UserPackageRepositoryCustom userPackageRepositoryCustom;
    private final CourseRepositoryCustom courseRepositoryCustom;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final UserTopicProgressRepository userTopicProgressRepository;
    private final UserSubPackageRepository userSubPackageRepository;
    private final SubPackageRepository subPackageRepository;
    private final ContentRepository contentRepository;
    private final CertificateGenerator certificateGenerator;
    private final AzureCertificateUploader azureCertificateUploader;
    private final PackageRepository packageRepository;
    private final ExamRepository examRepository;
    private final ChapterRepository chapterRepository;
    private final MongoTemplate mongoTemplate;
    private final UserCurrentContextService userCurrentContextService;
    private final TrainingRiskScoreSyncService trainingRiskScoreSyncService;
    private final CmsNotificationClient cmsNotificationClient;
    private final RegistrationServiceClient registrationServiceClient;

    private static final DateTimeFormatter COMPLETION_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter COMPLETION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy").withZone(ZoneId.systemDefault());

    /**
     * Helper method to get current user ID from context safely
     * Returns null if context is not available (e.g., when called from registration service)
     */
    private String getCurrentUserId() {
        try {
            return userCurrentContextService.getCurrentUserContext().getUserId();
        } catch (Exception e) {
            log.debug("Unable to get current user context, returning null for audit fields");
            return null;
        }
    }

    static boolean shouldSendCourseCompletionNotification(String previousStatus) {
        return previousStatus == null
                || !SubPackageStatus.PHISHING_TRAINING_COMPLETED.name().equals(previousStatus);
    }

    boolean isPhishingDefaultTrainingSubPackage(SubPackage subPackage) {
        return subPackage != null && !ObjectUtils.isEmpty(subPackage.getAssignedFor());
    }

    void sendCourseCompletionNotification(String userId, UserSubPackage userSubPackage, SubPackage subPackage) {
        try {
            UserDataDto userData = registrationServiceClient.getUserData(userId);


            String userEmail = userSubPackage.getUserEmail();
            if ((userEmail == null || userEmail.isBlank()) && userData != null) {
                userEmail = userData.getEmail();
            }
            if (userEmail == null || userEmail.isBlank()) {
                log.warn("Skipping course completion notification: no email for userId={}, subPackageId={}",
                        userId, userSubPackage.getSubPackageId());
                return;
            }

            String userName = resolveUserDisplayName(userData, userEmail);
            String courseTitle = userSubPackage.getSubPackageName();
            if (courseTitle == null || courseTitle.isBlank()) {
                courseTitle = subPackage != null ? subPackage.getName() : "Training Course";
            }

            Instant completedAt = Instant.now();
            String completionTimestamp = COMPLETION_TIMESTAMP_FORMAT.format(completedAt);
            String completionDate = COMPLETION_DATE_FORMAT.format(completedAt);

            String clientAdminId = userSubPackage.getClientAdminId();
            if ((clientAdminId == null || clientAdminId.isBlank()) && userData != null) {
                clientAdminId = userData.getClientAdminId();
            }

            String adminEmail = userData != null ? userData.getClientAdminEmail() : null;
            String adminName = userData != null && userData.getClientAdminName() != null
                    ? userData.getClientAdminName()
                    : "Admin";

            CourseCompletionNotificationRequest request = new CourseCompletionNotificationRequest(
                    userEmail,
                    userId,
                    userName,
                    courseTitle,
                    completionDate,
                    completionTimestamp,
                    adminEmail,
                    adminName,
                    clientAdminId
            );

            boolean sent = cmsNotificationClient.sendCourseCompletionNotifications(request);
            if (sent) {
                log.info("Course completion notifications sent for userId={}, subPackageId={}", userId, userSubPackage.getSubPackageId());
            } else {
                log.warn("Course completion notifications failed for userId={}, subPackageId={}", userId, userSubPackage.getSubPackageId());
            }
        } catch (Exception e) {
            log.error("Error sending course completion notification for userId={}, subPackageId={}: {}",
                    userId, userSubPackage.getSubPackageId(), e.getMessage(), e);
        }
    }

    private static String resolveUserDisplayName(UserDataDto userData, String userEmail) {
        if (userData != null) {
            String firstName = userData.getFirstName() != null ? userData.getFirstName().trim() : "";
            String lastName = userData.getLastName() != null ? userData.getLastName().trim() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                return fullName;
            }
        }
        if (userEmail != null && userEmail.contains("@")) {
            return userEmail.substring(0, userEmail.indexOf('@'));
        }
        return "User";
    }





    /**
     * Retrieves a list of courses for a user with optional filtering by package ID, status, and search term.
     *
     * @param userId
     * @param packageId
     * @param status
     * @param search
     * @param offset
     * @param pageSize
     * @return
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
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


    @SuppressWarnings("unchecked")
    private List<String> castList(Object obj) {
        return obj instanceof List<?> ? (List<String>) obj : List.of();
    }

    /**
     * Extracts content list from interactive video/content's specific content
     * @param contentDoc The content document from MongoDB
     * @return List of InteractiveContentListItem or empty list if not applicable
     */
    @SuppressWarnings("unchecked")
    private List<ContentStatusDTO.InteractiveContentListItem> extractContentListFromDocument(Document contentDoc) {
        try {
            // Navigate: specificContent -> contentList
            Document specificContent = (Document) contentDoc.get("specificContent");
            if (specificContent == null) {
                return List.of();
            }
            
            List<Document> contentList = (List<Document>) specificContent.get("contentList");
            if (contentList == null || contentList.isEmpty()) {
                Object byLanguage = specificContent.get("interactiveVideoByLanguage");
                if (byLanguage instanceof Document byLanguageMap) {
                    contentList = byLanguageMap.values().stream()
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .map(videoDoc -> (List<Document>) videoDoc.get("contentList"))
                            .filter(Objects::nonNull)
                            .flatMap(List::stream)
                            .toList();
                } else if (byLanguage instanceof List<?> byLanguageList) {
                    contentList = byLanguageList.stream()
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .map(videoDoc -> (List<Document>) videoDoc.get("contentList"))
                            .filter(Objects::nonNull)
                            .flatMap(List::stream)
                            .toList();
                }
            }
            if (contentList == null || contentList.isEmpty()) {
                return List.of();
            }
            
            // Extract id and contentName from each contentListItem
            return contentList.stream()
                    .map(item -> {
                        // MongoDB uses _id field, try both id and _id for compatibility
                        String id = item.getString("id");
                        if (id == null || id.isEmpty()) {
                            id = item.getString("_id");
                        }
                        
                        // Extract contentName from contentBody.contentName or commonContent.contentName
                        Document contentBody = (Document) item.get("contentBody");
                        String contentName = null;
                        if (contentBody != null) {
                            // First try direct contentName at contentBody level
                            contentName = contentBody.getString("contentName");
                            if (contentName == null || contentName.isEmpty()) {
                                // Fallback to contentBody.commonContent.contentName
                                Document commonContent = (Document) contentBody.get("commonContent");
                                if (commonContent != null) {
                                    contentName = commonContent.getString("contentName");
                                }
                            }
                        }
                        
                        return new ContentStatusDTO.InteractiveContentListItem(id, contentName);
                    })
                    .filter(item -> item.getId() != null && !item.getId().isEmpty()) // Filter out items without ID
                    .collect(Collectors.toMap(
                            ContentStatusDTO.InteractiveContentListItem::getId,
                            item -> item,
                            (first, second) -> first,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("Error extracting contentList from content {}: {}", 
                    contentDoc.getString("_id"), e.getMessage());
            return List.of();
        }
    }


    /**
     * Retrieves the details of a specific course for a user.
     *
     * @param courseId The ID of the course.
     * @param userId   The ID of the user.
     * @return A DTO containing the course details.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public CourseDetailsResponseDTO getCourseDetails(String courseId, String userId, String packageId) {
        log.info("Fetching course details for userId={} and courseId={}", userId, courseId);

        Document doc = courseRepositoryCustom.getFullCourseHierarchy(userId, courseId, packageId);
        if (doc == null) {
            log.warn("No course data found for userId={} and courseId={}", userId, courseId);
            throw new RuntimeException("Course data not found for user " + userId + " and courseId " + courseId);
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
                        
                        // Extract basic info
                        String contentType = cDoc.get("contentType") != null ? cDoc.get("contentType").toString() : null;
                        ContentStatusDTO contentDTO = new ContentStatusDTO(
                                cDoc.getString("_id"),
                                cDoc.getString("contentName"),
                                contentType,
                                completedIds.contains(cDoc.getString("_id"))
                        );
                        
                        // For INTERACTIVE_VIDEO and INTERACTIVE_CONTENT, extract contentList
                        if ("INTERACTIVE_VIDEO".equals(contentType) || "INTERACTIVE_CONTENT".equals(contentType)) {
                            List<ContentStatusDTO.InteractiveContentListItem> contentList = extractContentListFromDocument(cDoc);
                            contentDTO.setContentList(contentList);
                        }
                        
                        return contentDTO;
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


    /**
     * Retrieves a list of packages for a user with pagination.
     *
     * @param userId   The ID of the user.
     * @param offset   The offset for pagination.
     * @param pageSize The number of items per page.
     * @return A list of package response DTOs.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public List<PackageResponseDTO> getUserPackages(String userId, int offset, int pageSize, String status) {
        log.info("Fetching user packages for userId={}, offset={}, pageSize={}, status={}", userId, offset, pageSize, status);
        offset = CommonUtil.getOffset(offset, pageSize);

        List<Document> docs = userPackageRepositoryCustom.getUserPackagesWithDetails(userId, offset, pageSize, status);

        return docs.stream().map(doc -> {
            List<String> courseIds = castList(doc.get("courseIds"));
            List<String> completedTopicIds = castList(doc.get("completedTopicIds"));
            int totalCourses = courseIds.size();
            int completedCourses = completedTopicIds.size();
            double progress = totalCourses == 0 ? 0.0 : (completedCourses * 100.0) / totalCourses;

            return new PackageResponseDTO(
                    doc.getString("subPackageId"),
                    doc.getString("subPackageName"),
                    totalCourses,
                    completedCourses,
                    progress,
                    doc.getString("validity"),
                    doc.containsKey("assignedDate") ? doc.get("assignedDate").toString() : "",
                    doc.containsKey("expiryDate") ? doc.get("expiryDate").toString() : "",
                    false
            );
        }).toList();
    }


    /**
     * Retrieves the details of a specific package for a user.
     *
     * @param packageId The ID of the package.
     * @param userId    The ID of the user.
     * @return A DTO containing the package details.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public PackageDetailsResponseDTO getPackageDetails(String packageId, String userId) {
        // Fetch user package
        UserSubPackage userPackage = userSubPackageRepository.findByUserIdAndSubPackageId(userId, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("User package not found"));

        // Fetch package
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        // Fetch completed course names
        List<String> completedIds = userPackage.getCompletedTopicIds() != null
                ? userPackage.getCompletedTopicIds()
                : List.of();

        List<Course> courses = completedIds.isEmpty()
                ? List.of()
                : courseRepository.findAllById(completedIds);

        List<CompletedCourseDTO> completedCourses = completedIds.stream()
                .map(id -> {
                    String name = courses.stream()
                            .filter(c -> c.getId().equals(id))
                            .map(Course::getCourseName)
                            .findFirst()
                            .orElse("");
                    return CompletedCourseDTO.builder()
                            .courseId(id)
                            .courseName(name)
                            .build();
                })
                .toList();

        // Get latest exam (by createdAt) from Exams table (includes both config and results)
        Optional<Exams> examOpt = examRepository.findFirstByUserIdAndSubPackageIdOrderByCreatedAtDesc(userId, packageId);
        
        // Build exam DTO from user-specific exam
        PackageExamDTO examDto = examOpt.map(exam -> PackageExamDTO.builder()
                        .id(exam.getExamId())
                        .title(exam.getTitle())
                        .passingScore(exam.getPassingScore())
                        .build())
                .orElse(null);
        
        // Build response
        PackageDetailsResponseDTO.PackageDetailsResponseDTOBuilder builder = PackageDetailsResponseDTO.builder()
                .packageId(pkg.getId())
                .packageName(pkg.getPackageName())
                .validity(userPackage.getValidity())
                .expireDate(userPackage.getExpiryDate() != null ? userPackage.getExpiryDate().toString() : null)
                .status(userPackage.getStatus())
                .progress(userPackage.getProgress())
                .completedCourses(completedCourses)
                .exam(examDto)
                .certificateLink(userPackage.getCertificateLink())
                .ImageCertificateLink(userPackage.getImageCertificateLink());
        
        // Set exam data from Exams table if exists, otherwise use defaults
        if (examOpt.isPresent()) {
            Exams exam = examOpt.get();
            builder.examCompleted(exam.isExamCompleted())
                   .examScore(exam.getExamScore())
                   .examPassed(exam.isExamPassed())
                   .correctAnswers(exam.getCorrectAnswers())
                   .incorrectAnswers(exam.getIncorrectAnswers())
                   .examCompletedAt(exam.getExamCompletedAt())
                   .examAttempts(exam.getExamAttempts());
        } else {
            builder.examCompleted(false)
                   .examScore(0.0)
                   .examPassed(false)
                   .correctAnswers(0)
                   .incorrectAnswers(0)
                   .examCompletedAt(null)
                   .examAttempts(0);
        }
        
        return builder.build();
    }

    @Override
    @Transactional
    public void markContentAsCompleted(String userId, String topicId, String contentId, String subPackageId) {
        log.info("Marking content as completed: userId={}, topicId={}, contentId={}, subPackageId={}",
                userId, topicId, contentId, subPackageId);

        // --- Guarded lookups ---
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + topicId));
        contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id " + contentId));

        // --- Step 1: upsert user_content_status ---
        userContentStatusRepository
                .findByUserIdAndTopicIdAndContentIdAndSubPackageId(userId, topicId, contentId, subPackageId)
                .ifPresentOrElse(status -> {
                    if (!status.isDone()) {
                        status.setIsDone(true);
                        userContentStatusRepository.save(status);
                        log.debug("Updated UserContentStatus to done for userId={}, topicId={}, contentId={}, subPackageId={}",
                                userId, topicId, contentId, subPackageId);
                    }
                }, () -> {
                    UserContentStatus status = new UserContentStatus();
                    status.setId(CommonUtil.generateUUID());
                    status.setUserId(userId);
                    status.setTopicId(topicId);
                    status.setContentId(contentId);
                    status.setSubPackageId(subPackageId);
                    status.setIsDone(true);
                    userContentStatusRepository.save(status);
                    log.debug("Inserted new UserContentStatus as done for userId={}, topicId={}, contentId={}, subPackageId={}",
                            userId, topicId, contentId, subPackageId);
                });

        // --- Step 2: update user_topics ---
        Instant now = Instant.now();
        String currentUserId = getCurrentUserId();
        UserTopicProgress progress = userTopicProgressRepository
                .findByUserIdAndTopicIdAndSubPackageId(userId, topicId, subPackageId)
                .orElseGet(() -> {
                    UserTopicProgress p = new UserTopicProgress();
                    p.setId(CommonUtil.generateUUID());
                    p.setUserId(userId);
                    p.setTopicId(topicId);
                    p.setSubPackageId(subPackageId);
                    p.setProgress(0.0);
                    p.setCompletedContentIds(new ArrayList<>());
                    p.setLastSynced(now);
                    p.setIsBookmarked(false);
                    p.setStatus(TopicStatus.NOT_STARTED.toString());
                    p.setIsSaved(false);
                    // Set audit fields for new record
                    p.setCreatedAt(now);
                    p.setCreatedBy(currentUserId);
                    p.setUpdatedAt(now);
                    p.setUpdatedBy(currentUserId);
                    return p;
                });

        // Set createdAt/createdBy if this is an existing record that doesn't have audit fields (for backward compatibility)
        if (progress.getCreatedAt() == null) {
            progress.setCreatedAt(now);
            progress.setCreatedBy(currentUserId);
        }

        Set<String> completedContentSet = new HashSet<>(
                progress.getCompletedContentIds() != null ? progress.getCompletedContentIds() : List.of()
        );
        completedContentSet.add(contentId);
        progress.setCompletedContentIds(new ArrayList<>(completedContentSet));

        int totalCount = topic.getTotalContentCount() != null ? topic.getTotalContentCount() : 0;
        // If totalCount is 0 (bad metadata), keep progress at 0 to avoid false 100%
        double newProgress = totalCount <= 0 ? 0.0 : (completedContentSet.size() * 100.0) / totalCount;

        progress.setProgress(newProgress);
        progress.setLastSynced(now);
        
        // --- Step 3: update status based on progress ---
        final double DONE_EPS = 99.999; // tolerate rounding issues
        String currentStatus = progress.getStatus();
        if (newProgress >= DONE_EPS) {
            if (currentStatus == null || !TopicStatus.COMPLETED.toString().equalsIgnoreCase(currentStatus)) {
                progress.setStatus(TopicStatus.COMPLETED.toString());
                log.info("Topic marked as COMPLETED for userId={}, topicId={}", userId, topicId);
            }
        } else if (newProgress > 0.0) {
            if (currentStatus == null || !TopicStatus.IN_PROGRESS.toString().equalsIgnoreCase(currentStatus)) {
                progress.setStatus(TopicStatus.IN_PROGRESS.toString());
                log.info("Topic status set to IN_PROGRESS for userId={}, topicId={}", userId, topicId);
            }
        } else {
            if (currentStatus == null || !TopicStatus.NOT_STARTED.toString().equalsIgnoreCase(currentStatus)) {
                progress.setStatus(TopicStatus.NOT_STARTED.toString());
                log.info("Topic status set to NOT_STARTED for userId={}, topicId={}", userId, topicId);
            }
        }
        
        // Update audit fields
        progress.setUpdatedAt(now);
        progress.setUpdatedBy(currentUserId);
        
        userTopicProgressRepository.save(progress);
        log.debug("Progress updated to {}% for userId={}, topicId={}, subPackageId={}", newProgress, userId, topicId, subPackageId);

        // --- Step 4: update user_subpackages progress & phase ---
        userSubPackageRepository.findByUserIdAndSubPackageId(userId, subPackageId).ifPresent(userSubPackage -> {
            String previousStatus = userSubPackage.getStatus();

            // Start with existing list; keep it consistent with actual completion state
            List<String> completedTopicIds = new ArrayList<>(
                    userSubPackage.getCompletedTopicIds() != null ? userSubPackage.getCompletedTopicIds() : List.of()
            );

            boolean topicNowCompleted = newProgress >= DONE_EPS;

            if (topicNowCompleted) {
                if (!completedTopicIds.contains(topicId)) {
                    completedTopicIds.add(topicId); // add only when truly completed
                }
            } else {
                // If user falls below completion (edge edits), remove from completed list
                completedTopicIds.remove(topicId);
            }
            userSubPackage.setCompletedTopicIds(completedTopicIds);

            // Use SubPackage metadata to compute overall progress
            SubPackage subPkg = subPackageRepository.findById(subPackageId)
                    .orElseThrow(() -> new ResourceNotFoundException("SubPackage not found with id: " + subPackageId));

            int totalTopics = (subPkg.getTopicId() != null) ? subPkg.getTopicId().size() : 0;
            int completedTopics = completedTopicIds.size();

            double overall = totalTopics == 0 ? 0.0 : (completedTopics * 100.0) / totalTopics;
            userSubPackage.setProgress(overall);
            userSubPackage.setLastSynced(Instant.now());

            if (overall >= 100.0) {
                if (isPhishingDefaultTrainingSubPackage(subPkg)) {
                    userSubPackage.setStatus(SubPackageStatus.PHISHING_TRAINING_COMPLETED.name());
                    log.info("UserSubPackage moved directly to COMPLETED for default training flow: userId={}, subPackageId={}", userId, subPackageId);

                    if (shouldSendCourseCompletionNotification(previousStatus)) {
                        sendCourseCompletionNotification(userId, userSubPackage, subPkg);
                    }

                } else {
                    userSubPackage.setStatus(SubPackageStatus.EXAM.name());
                    log.info("UserSubPackage moved to EXAM phase for userId={}, subPackageId={}", userId, subPackageId);
                }
            } else if (overall > 0.0) {
                userSubPackage.setStatus("IN_PROGRESS");
            } else {
                userSubPackage.setStatus("NOT_STARTED");
            }
            userSubPackage.setRiskScoreFromStatus();

            userSubPackageRepository.save(userSubPackage);
            trainingRiskScoreSyncService.syncTrainingRiskScore(previousStatus, userSubPackage);
        });
    }


    /**
     * Counts the number of courses for a user.
     *
     * @param userId
     * @param packageId
     * @param status
     * @param search
     * @return
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public long countUserCourses(String userId, String packageId, String status, String search, Boolean isSaved) {
        log.debug("Counting user courses for userId={}, packageId={}, status={}, search={}, isSaved={}",
                userId, packageId, status, search, isSaved);

        long count = courseRepositoryCustom.countUserCourses(userId, status, search, isSaved);

        log.debug("Course count result for userId={} is {}", userId, count);
        return count;
    }


    /**
     * Counts the number of courses for a user by package ID.
     *
     * @param packageId The ID of the package.
     * @param status    The status of the courses (e.g., "completed", "in_progress").
     * @param search    A search term to filter courses.
     * @return The count of courses.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public long countUserCoursesByPackage(String packageId, String status, String search, Boolean isSaved) {
        log.debug("Counting courses for packageId={}, status={}, search={}, isSaved={}",
                packageId, status, search, isSaved);

        long count = courseRepositoryCustom.countUserCoursesByPackage(packageId, status, search, isSaved);

        log.debug("Course count result for packageId={} is {}", packageId, count);
        return count;
    }


    /**
     * Counts the number of packages for a user.
     *
     * @param userId The ID of the user.
     * @return The count of packages.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
    @Override
    public long countUserPackages(String userId, String status) {
        return userPackageRepositoryCustom.countUserPackagesWithDetails(userId, status);
    }

    /**
     * Updates the bookmark status of a course for a user.
     *
     * @param courseId The ID of the course.
     * @param userId   The ID of the user.
     * @param isSaved  The new bookmark status.
     * @author Mahadi Hasan Joy
     * @since 2025-05-13
     */
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


    /**
     * Assigns a course to a specified user. This includes creating an entry for the user-course
     * assignment, initializing course progress, and setting up content status for the user.
     *
     * @param userId   The unique identifier of the user to whom the course is being assigned.
     * @param courseId The unique identifier of the course to be assigned to the user.
     * @throws IllegalStateException if the user is already assigned to the specified course.
     * @throws RuntimeException      if the course structure or content is not found.
     * @author Mahadi Hasan Joy
     * @since 20-05-2025
     */
    @Override
    @Transactional
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
            throw new RuntimeException("Course structure not found for courseId: " + courseId);
        }

        List<Document> contents = (List<Document>) doc.get("contents");
        if (contents == null || contents.isEmpty()) {
            log.error("No content found in courseId={} for userId={}", courseId, userId);
            throw new RuntimeException("No content found in course to assign to user");
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
    @Transactional
    public void assignPackageToUser(String userId, String packageId) {
        log.info("Assigning packageId={} to userId={}", packageId, userId);

        // Check if the user is already assigned to the package
        if (userSubPackageRepository.existsByUserIdAndSubPackageId(userId, packageId)) {
            log.warn("User {} is already assigned to package {}", userId, packageId);
            throw new IllegalStateException("User is already assigned to this package.");
        }

        // Fetch the Package from the repository
        Package selectedPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalStateException("Package not found with ID: " + packageId));

        // Prepare batch insert lists
        List<UserCourse> userCourses = new ArrayList<>();
        List<UserCourseProgress> userCourseProgresses = new ArrayList<>();
        List<UserContentStatus> userContentStatuses = new ArrayList<>();

        // Iterate through each courseId in the package
        for (String courseId : selectedPackage.getCourseIds()) {
            // Create UserCourse entry
            UserCourse userCourse = new UserCourse();
            userCourse.setId(UUID.randomUUID().toString());
            userCourse.setUserId(userId);
            userCourse.setPackageId(packageId);
            userCourse.setCourseId(courseId);
            userCourse.setStatus("NOT_STARTED");
            userCourse.setIsSaved(false);
            userCourses.add(userCourse);

            // Create UserCourseProgress entry
            UserCourseProgress userCourseProgress = new UserCourseProgress();
            userCourseProgress.setId(UUID.randomUUID().toString());
            userCourseProgress.setUserId(userId);
            userCourseProgress.setCourseId(courseId);
            userCourseProgress.setPackageId(packageId);
            userCourseProgress.setProgress(0.0);
            userCourseProgress.setCompletedContentIds(new ArrayList<>());
            userCourseProgress.setLastSynced(Instant.now());
            userCourseProgresses.add(userCourseProgress);

            // Fetch all chapters and their contentIds in one query
            List<Chapter> chapters = chapterRepository.findByTopicId(courseId);
            if (chapters == null || chapters.isEmpty()) {
                log.error("No chapters found for courseId={}", courseId);
                throw new RuntimeException("No chapters found for courseId: " + courseId);
            }

            // Collect all contentIds from chapters efficiently
            List<String> contentIds = chapters.stream()
                    .flatMap(chapter -> chapter.getContentIds().stream())
                    .collect(Collectors.toList());

            // Create UserContentStatus entries in bulk for the contentIds
            userContentStatuses.addAll(contentIds.stream()
                    .map(contentId -> {
                        UserContentStatus userContentStatus = new UserContentStatus();
                        userContentStatus.setId(UUID.randomUUID().toString());
                        userContentStatus.setUserId(userId);
                        userContentStatus.setCourseId(courseId);
                        userContentStatus.setPackageId(packageId);
                        userContentStatus.setContentId(contentId);
                        userContentStatus.setIsDone(false);
                        return userContentStatus;
                    })
                    .collect(Collectors.toList()));
        }

        // Perform batch insert for all entities
        userCourseRepository.saveAll(userCourses);
        userCourseProgressRepository.saveAll(userCourseProgresses);
        userContentStatusRepository.saveAll(userContentStatuses);

        log.info("Batch insertion completed for UserCourse, UserCourseProgress, and UserContentStatus");

        // Create UserPackage entry and save it
        UserSubPackage userPackage = new UserSubPackage();
        userPackage.setId(UUID.randomUUID().toString());
        userPackage.setUserId(userId);
        userPackage.setSubPackageId(packageId);
        userPackage.setStatus("NOT_STARTED");
        userPackage.setValidity("365 Days");
        userPackage.setExpiryDate(LocalDate.now().plusDays(365));
        userPackage.setCompletedTopicIds(Collections.emptyList()); // Use empty list for efficiency
        userPackage.setRiskScoreFromStatus();
        userSubPackageRepository.save(userPackage);
        trainingRiskScoreSyncService.syncTrainingRiskScore(null, userPackage);

        log.info("UserPackage saved for userId={} and packageId={}", userId, packageId);
    }

    @Override
    @Transactional
    public void assignPackageToMultipleUsers(String clientId, List<String> userIds, String packageId) {
        log.info("Assigning packageId={} to {} user(s)", packageId, userIds.size());

        // 1. Fetch the matched client product directly (only 1 expected now)
        ClientProductIdListResponseDto clientProductData = getProductIdsByClient(clientId, packageId);
        ClientProductDTO matchedProduct = clientProductData.getClientProductDTOS().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No client product found for packageId: " + packageId));

        int remainingLicenses = matchedProduct.getLicenseCount() - matchedProduct.getUsedLicenseCount();
        log.info("Validating license availability: {} available, {} users to assign", remainingLicenses, userIds.size());

        // Count users that actually need assignment
        List<String> newUsers = userIds.stream()
                .filter(userId -> !userSubPackageRepository.existsByUserIdAndSubPackageId(userId, packageId))
                .toList();

        if (remainingLicenses < newUsers.size()) {
            throw new IllegalStateException("Not enough licenses. Needed: " + newUsers.size() + ", Available: " + remainingLicenses);
        }

        // 2. Load package and related chapter-content structure
        Package selectedPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalStateException("Package not found with ID: " + packageId));

        List<Chapter> allChapters = selectedPackage.getCourseIds().stream()
                .flatMap(courseId -> chapterRepository.findByTopicId(courseId).stream())
                .toList();

        Map<String, List<String>> courseIdToContentIds = allChapters.stream()
                .collect(Collectors.groupingBy(
                        Chapter::getTopicId,
                        Collectors.flatMapping(ch -> ch.getContentIds().stream(), Collectors.toList())
                ));

        // 3. Assign to each new user
        for (String userId : newUsers) {
            List<UserCourse> userCourses = new ArrayList<>();
            List<UserCourseProgress> userCourseProgresses = new ArrayList<>();
            List<UserContentStatus> userContentStatuses = new ArrayList<>();

            for (String courseId : selectedPackage.getCourseIds()) {
                userCourses.add(UserCourse.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .courseId(courseId)
                        .packageId(packageId)
                        .status("NOT_STARTED")
                        .isSaved(false)
                        .build());

                userCourseProgresses.add(UserCourseProgress.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .courseId(courseId)
                        .packageId(packageId)
                        .progress(0.0)
                        .completedContentIds(new ArrayList<>())
                        .lastSynced(Instant.now())
                        .build());

                List<String> contentIds = courseIdToContentIds.getOrDefault(courseId, List.of());
                for (String contentId : contentIds) {
                    userContentStatuses.add(UserContentStatus.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(userId)
                            .courseId(courseId)
                            .packageId(packageId)
                            .contentId(contentId)
                            .isDone(false)
                            .build());
                }
            }

            userCourseRepository.saveAll(userCourses);
            userCourseProgressRepository.saveAll(userCourseProgresses);
            userContentStatusRepository.saveAll(userContentStatuses);

            userSubPackageRepository.save(UserSubPackage.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .subPackageId(packageId)
                    .validity("365 Days")
                    .expiryDate(LocalDate.now().plusDays(365))
                    .completedTopicIds(Collections.emptyList())
                    .build());

            log.info("Package {} assigned to user {}", packageId, userId);
        }

        // 4. Update used license count with actual new assignments
        int updatedUsedLicenseCount = matchedProduct.getUsedLicenseCount() + newUsers.size();
        updateUsedLicenseCount(clientId, matchedProduct.getProductId(), updatedUsedLicenseCount);

        assignEndUsersToProductInRegistration(packageId, newUsers);


        log.info("Completed assigning package {} to {} new user(s)", packageId, newUsers.size());
    }

    /**
     * Assigns subpackages to users based on the request from Registration service.
     * This method creates UserSubPackage, UserTopicProgress, and UserContentStatus records.
     *
     * @param request The subpackage assignment request containing subpackage data and user lists
     */
    @Override
    @Transactional
    public void assignSubPackagesToUsers(UserSubPackageAssignRequest request) {
        log.info("Processing subpackage assignment request with {} subpackages", 
                request.getSubPackageData().size());
        log.info("Request details: {}", request);

        for (UserSubPackageAssignRequest.SubPackageData subPackageData : request.getSubPackageData()) {
            log.info("Processing subpackage {} for {} users", 
                    subPackageData.getSubPackageId(), subPackageData.getUserIdList().size());

            // Fetch the SubPackage to get its topics
            SubPackage selectedSubPackage = subPackageRepository.findById(subPackageData.getSubPackageId())
                    .orElseThrow(() -> new IllegalStateException("SubPackage not found with ID: " + subPackageData.getSubPackageId()));

            // Process each user for this subpackage
            for (String userId : subPackageData.getUserIdList()) {
                try {
                    // Check if user is already assigned to this subpackage
                    if (userSubPackageRepository.existsByUserIdAndSubPackageId(userId, subPackageData.getSubPackageId())) {
                        log.info("User {} already assigned to subpackage {}", userId, subPackageData.getSubPackageId());
                        continue;
                    }

                    // Create UserSubPackage record
                    UserSubPackage userSubPackage = new UserSubPackage();
                    userSubPackage.setId(CommonUtil.generateUUID());
                    userSubPackage.setUserId(userId);
                    userSubPackage.setClientAdminId(subPackageData.getClientAdminId());
                    userSubPackage.setSubPackageId(subPackageData.getSubPackageId());
                    userSubPackage.setClientAdminId(subPackageData.getClientAdminId());
                    userSubPackage.setProductId(subPackageData.getProductId());
                    userSubPackage.setSubPackageName(selectedSubPackage.getName());
                    userSubPackage.setStatus("NOT_STARTED");
                    userSubPackage.setValidity(subPackageData.getValidFor() + " Days");
                    userSubPackage.setAssignedDate(LocalDate.now());
                    userSubPackage.setExpiryDate(LocalDate.now().plusDays(subPackageData.getValidFor()));
                    userSubPackage.setCompletedTopicIds(new ArrayList<>());
                    userSubPackage.setProgress(0.0);
                    userSubPackage.setLastSynced(Instant.now());

                    userSubPackage.setSecondaryEmails(subPackageData.getSecondaryEmails());
                    userSubPackage.setThirdLevelEmails(subPackageData.getThirdLevelEmails());
                    userSubPackage.setFourthHREmails(subPackageData.getFourthHREmails());

                    // Initialize reminder tracking flags
                    userSubPackage.setReminder50PercentSent(false);
                    userSubPackage.setReminder20PercentSent(false);
                    userSubPackage.setReminder10PercentSent(false);
                    userSubPackage.setReminderExpirySent(false);
                    userSubPackage.setIsTrial(selectedSubPackage.getIsTrial());
                    // userEmail will be set later in scheduler or can be fetched during assignment if needed
                    if (Boolean.TRUE.equals(selectedSubPackage.getIsPhishingSubpackage())) {
                        userSubPackage.setIsPhishingSubpackage(true);
                    }
                    String channel = subPackageData.getChannel() != null
                            ? subPackageData.getChannel()
                            : selectedSubPackage.getChannel();
                    if (channel != null && !channel.isBlank()) {
                        userSubPackage.setChannel(channel.trim());
                    }
                    userSubPackage.setRiskScoreFromStatus();

                    userSubPackageRepository.save(userSubPackage);
                    trainingRiskScoreSyncService.syncTrainingRiskScore(null, userSubPackage);

                    // Create UserTopicProgress records for each topic in the subpackage
                    Instant assignmentTime = Instant.now();
                    String assignmentUserId = getCurrentUserId();
                    for (String topicId : selectedSubPackage.getTopicId()) {
                        // Create UserTopicProgress record with all fields
                        UserTopicProgress userTopicProgress = new UserTopicProgress();
                        userTopicProgress.setId(CommonUtil.generateUUID());
                        userTopicProgress.setUserId(userId);
                        userTopicProgress.setTopicId(topicId);
                        userTopicProgress.setSubPackageId(subPackageData.getSubPackageId());
                        userTopicProgress.setProgress(0.0);
                        userTopicProgress.setCompletedContentIds(new ArrayList<>());
                        userTopicProgress.setLastSynced(assignmentTime);
                        userTopicProgress.setIsBookmarked(false);
                        userTopicProgress.setStatus(TopicStatus.NOT_STARTED.toString());
                        userTopicProgress.setIsSaved(false);
                        // Set audit fields for new record
                        userTopicProgress.setCreatedAt(assignmentTime);
                        userTopicProgress.setCreatedBy(assignmentUserId);
                        userTopicProgress.setUpdatedAt(assignmentTime);
                        userTopicProgress.setUpdatedBy(assignmentUserId);
                        userTopicProgressRepository.save(userTopicProgress);

                        // Create UserContentStatus records for all content in this topic
                        List<Chapter> chapters = chapterRepository.findByTopicId(topicId);
                        if (chapters != null && !chapters.isEmpty()) {
                            List<String> contentIds = chapters.stream()
                                    .flatMap(chapter -> chapter.getContentIds().stream())
                                    .collect(Collectors.toList());

                            List<UserContentStatus> contentStatuses = contentIds.stream()
                                    .map(contentId -> {
                                        UserContentStatus ucs = new UserContentStatus();
                                        ucs.setId(CommonUtil.generateUUID());
                                        ucs.setUserId(userId);
                                        ucs.setTopicId(topicId);
                                        ucs.setSubPackageId(subPackageData.getSubPackageId());
                                        ucs.setContentId(contentId);
                                        ucs.setIsDone(false);
                                        return ucs;
                                    })
                                    .collect(Collectors.toList());

                            userContentStatusRepository.saveAll(contentStatuses);
                            log.debug("Created {} UserContentStatus records for user {} and topic {}", 
                                    contentStatuses.size(), userId, topicId);
                        }
                    }

                    log.info("Successfully assigned subpackage {} to user {}", 
                            subPackageData.getSubPackageId(), userId);

                } catch (Exception e) {
                    log.error("Error assigning subpackage {} to user {}: {}", 
                            subPackageData.getSubPackageId(), userId, e.getMessage(), e);
                    // Continue with other users even if one fails
                }
            }
            // Mark subpackage as assigned
            selectedSubPackage.setIsAlreadyAssigned(true);
            subPackageRepository.save(selectedSubPackage);

        }
        // sent notification to end users

        log.info("Completed processing subpackage assignment request");
    }

    public void updateUsedLicenseCount(String clientId, String productId, int usedLicenseCount) {
        log.info("Calling registration API to update used license count: clientId={}, productId={}, count={}",
                clientId, productId, usedLicenseCount);

        String url = registrationUrl + "/client/admin/product-license/used-count" +
                "?clientAdminId=" + clientId +
                "&productId=" + productId +
                "&usedLicenseCount=" + usedLicenseCount;

        JsonNode response = webClient.put()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("statusCode")) {
            throw new RuntimeException("Registration service did not return a valid response for used license count update");
        }

        int statusCode = response.get("statusCode").asInt();
        if (statusCode != 200) {
            String errorMessage = response.has("message") ? response.get("message").asText() : "Unknown error";
            throw new RuntimeException("Failed to update used license count in registration module. Reason: " + errorMessage);
        }

        log.info("Used license count updated successfully for clientId={}, productId={}", clientId, productId);
    }

    public void assignEndUsersToProductInRegistration(String productId, List<String> endUserIds) {
        if (endUserIds == null || endUserIds.isEmpty()) {
            log.info("No new end users to assign for productId={}", productId);
            return;
        }

        String url = registrationUrl + "/end-user/assign-product?productId=" + productId;
        log.info("Calling Registration API to assign product: url={}, endUserCount={}", url, endUserIds.size());

        try {
            JsonNode response = webClient.post()
                    .uri(url)
                    .bodyValue(endUserIds) // JSON array body
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("statusCode")) {
                throw new RuntimeException("Registration service did not return a valid response for product assignment");
            }

            int statusCode = response.get("statusCode").asInt();
            if (statusCode != 200) {
                String errorMessage = response.has("message") ? response.get("message").asText() : "Unknown error";
                throw new RuntimeException("Failed to assign product to end users in registration module. Reason: " + errorMessage);
            }

            log.info("Assigned productId={} to {} end user(s) successfully via registration service", productId, endUserIds.size());
        } catch (Exception ex) {
            log.error("Error calling registration assign-product API for productId={}, users={}: {}", productId, endUserIds.size(), ex.getMessage(), ex);
            throw new RuntimeException("Registration assign-product API failed", ex);
        }
    }


    public ClientProductIdListResponseDto getProductIdsByClient(String clientAdminId, String productId) {
        String url = registrationUrl + "/client/admin/products?clientAdminId=" + clientAdminId;
        if (productId != null && !productId.isBlank()) {
            url += "&productId=" + productId;
        }

        JsonNode response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.get("data") == null) {
            throw new RuntimeException("No data field in registration response");
        }

        JsonNode dataNode = response.get("data");
        JsonNode clientIdsNode = dataNode.get("clientIds");
        JsonNode clientProductsNode = dataNode.get("clientProductDTOS");

        if (clientIdsNode == null || !clientIdsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientIds in registration response");
        }

        if (clientProductsNode == null || !clientProductsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientProductDTOS in registration response");
        }

        try {
            List<String> clientIds = objectMapper.treeToValue(clientIdsNode, new TypeReference<List<String>>() {
            });
            List<ClientProductDTO> clientProductDTOS = objectMapper
                    .readerFor(new TypeReference<List<ClientProductDTO>>() {
                    })
                    .readValue(clientProductsNode);

            return ClientProductIdListResponseDto.builder()
                    .clientIds(clientIds)
                    .clientProductDTOS(clientProductDTOS)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse clientIds or clientProductDTOS from registration response", e);
        }
    }



    @Override
    public List<CourseTopicStatsItemDTO> getCourseTopicStats(String userId, int offset, int pageSize) {
        offset = CommonUtil.getOffset(offset, pageSize);

        // Step 1: Fetch packageName and courseIds
        List<Document> userPackages = userPackageRepositoryCustom.getUserPackagesWithCourseIds(userId, offset, pageSize);

        // Step 2: Aggregate all courseIds
        Map<String, String> courseIdToPackageName = new HashMap<>();
        Map<String, String> courseIdToPackageId = new HashMap<>();

        for (Document doc : userPackages) {
            String packageName = doc.getString("packageName");
            String packageId = doc.getString("packageId");
            List<String> courseIds = (List<String>) doc.get("courseIds");
            if (courseIds != null) {
                for (String cid : courseIds) {
                    courseIdToPackageName.put(cid, packageName);
                    courseIdToPackageId.put(cid, packageId);
                }
            }
        }

        List<String> allCourseIds = new ArrayList<>(courseIdToPackageName.keySet());

        // Step 3: Fetch course statuses
        List<Document> courseStatuses = userPackageRepositoryCustom.getUserCourseStatuses(userId, allCourseIds);

        // Step 4: Count per package
        Map<String, Integer> completedMap = new HashMap<>();
        Map<String, Integer> pendingMap = new HashMap<>();

        for (String cid : allCourseIds) {
            String pid = courseIdToPackageId.get(cid);
            pendingMap.put(pid, pendingMap.getOrDefault(pid, 0) + 1); // default pending
        }

        for (Document doc : courseStatuses) {
            String cid = doc.getString("courseId");
            String status = doc.getString("status");
            String pid = courseIdToPackageId.get(cid);

            if ("COMPLETED".equals(status)) {
                completedMap.put(pid, completedMap.getOrDefault(pid, 0) + 1);
                pendingMap.put(pid, pendingMap.getOrDefault(pid, 0) - 1);
            }
        }

        // Step 5: Map to DTO
        return userPackages.stream().map(pkg -> {
            String pid = pkg.getString("packageId");
            String name = pkg.getString("packageName");
            List<String> courseIds = (List<String>) pkg.get("courseIds");
            int total = (courseIds != null) ? courseIds.size() : 0;

            return CourseTopicStatsItemDTO.builder()
                    .courseId(pid)
                    .courseName(name)
                    .total(total)
                    .completed(completedMap.getOrDefault(pid, 0))
                    .pending(pendingMap.getOrDefault(pid, total))
                    .build();
        }).toList();
    }

    @Override
    public void resetUserPackage(String userId, String packageId) {
        Instant now = Instant.now();

        // Reset UserContentStatus
        List<UserContentStatus> contentStatusList = userContentStatusRepository.findByUserIdAndPackageId(userId, packageId);
        for (UserContentStatus status : contentStatusList) {
            status.setIsDone(false);
        }
        userContentStatusRepository.saveAll(contentStatusList);

        // Reset UserCourse
        List<UserCourse> userCourses = userCourseRepository.findByUserIdAndPackageId(userId, packageId);
        for (UserCourse course : userCourses) {
            course.setStatus("NOT_STARTED");
            course.setIsSaved(false);
            course.setCertificateLink(null);
            course.setImageCertificateLink(null);
        }
        userCourseRepository.saveAll(userCourses);

        // Reset UserCourseProgress
        List<UserCourseProgress> courseProgressList = userCourseProgressRepository.findByUserIdAndPackageId(userId, packageId);
        for (UserCourseProgress progress : courseProgressList) {
            progress.setProgress(0.0);
            progress.setCompletedContentIds(List.of());
            progress.setLastSynced(now);
        }
        userCourseProgressRepository.saveAll(courseProgressList);

        // Reset UserPackage
        UserSubPackage userPackage = userSubPackageRepository.findByUserIdAndSubPackageId(userId, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("User package not found"));

        String previousStatus = userPackage.getStatus();
        userPackage.setProgress(0.0);
        userPackage.setStatus("NOT_STARTED");
        userPackage.setCompletedTopicIds(List.of());
        userPackage.setCertificateLink(null);
        userPackage.setImageCertificateLink(null);
        userPackage.setLastSynced(now);
        userPackage.setRiskScoreFromStatus();

        userSubPackageRepository.save(userPackage);
        trainingRiskScoreSyncService.syncTrainingRiskScore(previousStatus, userPackage);

        // Reset all exam records for this user+package (handles multiple attempts / resume records)
        List<Exams> exams = examRepository.findByUserIdAndSubPackageIdOrderByCreatedAtDesc(userId, packageId);
        for (Exams exam : exams) {
            exam.setExamCompleted(false);
            exam.setExamScore(0.0);
            exam.setExamPassed(false);
            exam.setCorrectAnswers(0);
            exam.setIncorrectAnswers(0);
            exam.setExamCompletedAt(null);
            exam.setCertificateLink(null);
            exam.setImageCertificateLink(null);
            examRepository.save(exam);
        }
    }

    @Override
    @Transactional
    public void resetUserSubPackage(String userId, String subPackageId) {
        Instant now = Instant.now();

        // Reset UserContentStatus for the sub-package
        List<UserContentStatus> contentStatusList = userContentStatusRepository.findByUserIdAndSubPackageId(userId, subPackageId);
        for (UserContentStatus status : contentStatusList) {
            status.setIsDone(false);
        }
        userContentStatusRepository.saveAll(contentStatusList);

        // Reset UserTopicProgress for the sub-package (now includes all UserTopic fields)
        String resetUserId = getCurrentUserId();
        List<UserTopicProgress> topicProgressList = userTopicProgressRepository.findByUserIdAndSubPackageId(userId, subPackageId);
        for (UserTopicProgress progress : topicProgressList) {
            progress.setProgress(0.0);
            progress.setCompletedContentIds(List.of());
            progress.setLastSynced(now);
            progress.setStatus("NOT_STARTED");
            progress.setIsBookmarked(false); // Reset bookmark field
            progress.setIsSaved(false); // Keep in sync with isBookmarked
            // Update audit fields
            progress.setUpdatedAt(now);
            progress.setUpdatedBy(resetUserId);
        }
        userTopicProgressRepository.saveAll(topicProgressList);

        // Reset UserSubPackage (main sub-package record)
        UserSubPackage userSubPackage = userSubPackageRepository.findByUserIdAndSubPackageId(userId, subPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("User sub-package not found"));

        String previousStatus = userSubPackage.getStatus();
        userSubPackage.setProgress(0.0);
        userSubPackage.setStatus("NOT_STARTED");
        userSubPackage.setCompletedTopicIds(List.of());
        userSubPackage.setCertificateLink(null);
        userSubPackage.setImageCertificateLink(null);
        userSubPackage.setLastSynced(now);
        userSubPackage.setRiskScoreFromStatus();

        userSubPackageRepository.save(userSubPackage);
        trainingRiskScoreSyncService.syncTrainingRiskScore(previousStatus, userSubPackage);

        // Reset all exam records for this user+subPackage (handles multiple attempts / resume records)
        List<Exams> exams = examRepository.findByUserIdAndSubPackageIdOrderByCreatedAtDesc(userId, subPackageId);
        for (Exams exam : exams) {
            exam.setExamCompleted(false);
            exam.setExamScore(0.0);
            exam.setExamPassed(false);
            exam.setCorrectAnswers(0);
            exam.setIncorrectAnswers(0);
            exam.setExamCompletedAt(null);
            exam.setCertificateLink(null);
            exam.setImageCertificateLink(null);
            examRepository.save(exam);
        }
    }

    @Override
    public List<SubPackageStatisticsItemDTO> getUserSubPackageStatistics(String userId, int offset, int pageSize) {
        log.info("Getting user subpackage statistics for user: {} with offset: {}, pageSize: {}", userId, offset, pageSize);

        
        Query userSubPackageQuery = new Query(Criteria.where("userId").is(userId));
        userSubPackageQuery.with(Sort.by(Sort.Direction.ASC, "assignedDate")
                .and(Sort.by(Sort.Direction.ASC, "_id")));

        int skipSize = offset * pageSize;
        userSubPackageQuery.skip(skipSize);
        userSubPackageQuery.limit(pageSize);
        
        log.debug("Pagination calculation: offset={}, pageSize={}, skipSize={}", offset, pageSize, skipSize);
        
        List<UserSubPackage> userSubPackages = mongoTemplate.find(userSubPackageQuery, UserSubPackage.class);
        
        log.debug("Step 1: Fetched {} user subpackages for userId: {} with offset: {}, pageSize: {}", 
                userSubPackages.size(), userId, offset, pageSize);
        
        if (userSubPackages.isEmpty()) {
            log.info("No subpackages found for user: {} after pagination (offset: {}, pageSize: {})", userId, offset, pageSize);
            return new ArrayList<>();
        }
        
        // Extract unique subPackageIds from the paginated UserSubPackage list
        // Since userId + subPackageId is unique, we shouldn't have duplicates, but use distinct() for safety
        List<String> subPackageIds = userSubPackages.stream()
                .map(UserSubPackage::getSubPackageId)
                .distinct()
                .collect(Collectors.toList());
        
        log.debug("Step 1 Complete: Extracted {} unique subPackageIds from paginated UserSubPackage list", subPackageIds.size());
        
        List<UserTopicProgress> allUserTopicProgress = userTopicProgressRepository.findByUserIdAndSubPackageIdIn(userId, subPackageIds);
        
        log.debug("Step 2: Fetched {} UserTopicProgress records for userId: {} and subPackageIds: {}", 
                allUserTopicProgress.size(), userId, subPackageIds);
        
        // Group UserTopicProgress by subPackageId
        Map<String, List<UserTopicProgress>> topicsBySubPackage = allUserTopicProgress.stream()
                .collect(Collectors.groupingBy(UserTopicProgress::getSubPackageId));
        
        log.debug("Step 2 Complete: Grouped UserTopicProgress into {} groups by subPackageId", topicsBySubPackage.size());
        
        List<SubPackageStatisticsItemDTO> statistics = new ArrayList<>();
        
        for (UserSubPackage userSubPackage : userSubPackages) {
            String subPackageId = userSubPackage.getSubPackageId();
            String subPackageName = userSubPackage.getSubPackageName();
            
            // Get UserTopicProgress for this subPackage (from the grouped map)
            List<UserTopicProgress> userTopicProgressList = topicsBySubPackage.getOrDefault(subPackageId, new ArrayList<>());
            
            // Count statuses for this subPackage
            int completed = 0;
            int inProgress = 0;
            int pending = 0;
            
            for (UserTopicProgress userTopicProgress : userTopicProgressList) {
                String status = userTopicProgress.getStatus();
                if (status != null) {
                    if ("COMPLETED".equalsIgnoreCase(status)) {
                        completed++;
                    } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                        inProgress++;
                    } else if ("NOT_STARTED".equalsIgnoreCase(status)) {
                        pending++;
                    }
                }
            }
            
            int total = userTopicProgressList.size();
            
            // Build statistics DTO for this subPackage
            SubPackageStatisticsItemDTO statsItem = SubPackageStatisticsItemDTO.builder()
                    .subPackageId(subPackageId)
                    .subPackageName(subPackageName)
                    .total(total)
                    .completed(completed)
                    .inProgress(inProgress)
                    .pending(pending)
                    .build();
            
            statistics.add(statsItem);
            
            log.debug("Step 3: SubPackage {} - Total: {}, Completed: {}, InProgress: {}, Pending: {}", 
                    subPackageId, total, completed, inProgress, pending);
        }
        
        // Get total count for logging
        long totalCount = userSubPackageRepository.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int currentPage = (offset / pageSize) + 1;
        
        log.info("Retrieved {} subpackage statistics for user: {} (offset: {}, pageSize: {}, page {} of {}, total: {})", 
                statistics.size(), userId, offset, pageSize, currentPage, totalPages, totalCount);
        
        return statistics;
    }

    @Override
    public long countUserSubPackages(String userId) {
        log.info("Counting user subpackages for user: {}", userId);
        return userSubPackageRepository.countByUserId(userId);
    }


    @Override
    public UserDashboardTopicsProgressResponseDto getUserTopicsProgress(String userId, int offset, int pageSize, String search) {
        log.info("Getting user topics progress for user: {} with offset: {}, pageSize: {}, search: {}", userId, offset, pageSize, search);
        
        // Step 1: Determine if we need to search or just paginate
        String searchPattern = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        List<UserTopicProgress> progressList;
        long total;
        
        if (searchPattern != null) {
            // For search, we'll use a different approach:
            // 1. Get all user topic progress records
            // 2. Get all topics for this user
            // 3. Filter topics by search pattern
            // 4. Filter progress records to only include matching topics
            // 5. Apply pagination
            
            List<UserTopicProgress> allProgress = userTopicProgressRepository.findByUserId(userId);
            if (allProgress.isEmpty()) {
                return UserDashboardTopicsProgressResponseDto.builder()
                        .items(List.of())
                        .offset(offset)
                        .pageSize(pageSize)
                        .total(0)
                        .build();
            }
            
            // Get all topic IDs
            List<String> allTopicIds = allProgress.stream()
                    .map(UserTopicProgress::getTopicId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // Get all topics and filter by search pattern
            List<Topic> allTopics = topicRepository.findAllById(allTopicIds);
            List<String> matchingTopicIds = allTopics.stream()
                    .filter(topic -> topic.getTopicName() != null && 
                            topic.getTopicName().toLowerCase().contains(searchPattern.toLowerCase()))
                    .map(Topic::getId)
                    .collect(Collectors.toList());
            
            // Filter progress records to only include matching topics
            List<UserTopicProgress> filteredProgress = allProgress.stream()
                    .filter(progress -> matchingTopicIds.contains(progress.getTopicId()))
                    .sorted((a, b) -> b.getLastSynced().compareTo(a.getLastSynced())) // Sort by lastSynced desc
                    .collect(Collectors.toList());
            
            total = filteredProgress.size();
            
            // Apply pagination
            int start = Math.min(offset, filteredProgress.size());
            int end = Math.min(start + pageSize, filteredProgress.size());
            progressList = filteredProgress.subList(start, end);
            
        } else {
            // Use simple pagination
            progressList = userTopicProgressRepository.findByUserIdWithPagination(userId, offset, pageSize);
            total = userTopicProgressRepository.countByUserId(userId);
        }
        
        if (progressList.isEmpty()) {
            log.info("No topic progress found for user: {} with search: {}", userId, search);
            return UserDashboardTopicsProgressResponseDto.builder()
                    .items(List.of())
                    .offset(offset)
                    .pageSize(pageSize)
                    .total(total)
                    .build();
        }
        
        // Step 2: Extract unique topicIds and subPackageIds from progress docs
        List<String> topicIds = progressList.stream()
                .map(UserTopicProgress::getTopicId)
                .distinct()
                .collect(Collectors.toList());
        List<String> subPackageIds = progressList.stream()
                .map(UserTopicProgress::getSubPackageId)
                .distinct()
                .collect(Collectors.toList());
        
        log.debug("Processing {} topics and {} subpackages for user: {}", topicIds.size(), subPackageIds.size(), userId);
        
        // Step 3: Batch query for Topics (for names)
        Map<String, Topic> topicMap = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));
        
        // Step 4: Batch query for UserSubPackages (for dates) - optimized query
        Map<String, UserSubPackage> userSubPkgMap = userSubPackageRepository.findByUserIdAndSubPackageIdIn(userId, subPackageIds).stream()
                .collect(Collectors.toMap(UserSubPackage::getSubPackageId, usp -> usp, (a, b) -> a));
        
        // Step 5: Build response items with proper date formatting
        List<UserDashboardTopicProgressItemDto> items = progressList.stream()
                .map(p -> {
                    Topic t = topicMap.get(p.getTopicId());
                    UserSubPackage usp = userSubPkgMap.get(p.getSubPackageId());
                    
                    String name = (t != null) ? t.getTopicName() : "Unknown Topic";
                    String startDate = formatDate(usp != null ? usp.getAssignedDate() : null);
                    String expireDate = formatDate(usp != null ? usp.getExpiryDate() : null);
                    String progressStr = Math.round(p.getProgress()) + "%";
                    
                    return UserDashboardTopicProgressItemDto.builder()
                            .topicId(p.getTopicId())
                            .name(name)
                            .start_date(startDate)
                            .expire_date(expireDate)
                            .progress(progressStr)
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("Retrieved {} topic progress items for user: {} (page {} of {})", 
                items.size(), userId, (offset / pageSize) + 1, (total + pageSize - 1) / pageSize);
        
        return UserDashboardTopicsProgressResponseDto.builder()
                .items(items)
                .offset(offset)
                .pageSize(pageSize)
                .total(total)
                .build();
    }
    
    /**
     * Formats LocalDate to "M/d/yy" format with proper null handling
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("%d/%d/%s", 
                date.getMonthValue(), 
                date.getDayOfMonth(), 
                String.valueOf(date.getYear()).substring(2));
    }

    /**
     * Gets user subpackage statistics count for dashboard
     * Returns counts of subpackages by status (total, completed, exam, inProgress, notStarted)
     *
     * @param userId the ID of the user
     * @return UserSubPackageStatisticsDTO containing counts by status
     */
    @Override
    public UserSubPackageStatisticsDTO getUserSubPackageStatisticsCount(String userId) {
        log.info("Getting user subpackage statistics count for userId: {}", userId);

        try {
            // Get all user subpackages for the given user
            List<UserSubPackage> userSubPackages = userSubPackageRepository.findByUserId(userId);
            
            if (userSubPackages.isEmpty()) {
                log.info("No subpackages found for userId: {}", userId);
                return UserSubPackageStatisticsDTO.builder()
                        .total(0)
                        .completed(0)
                        .exam(0)
                        .inProgress(0)
                        .notStarted(0)
                        .build();
            }

            // Count subpackages by status
            int total = userSubPackages.size();
            int completed = 0;
            int exam = 0;
            int inProgress = 0;
            int notStarted = 0;

            for (UserSubPackage userSubPackage : userSubPackages) {
                String status = userSubPackage.getStatus();
                if (status != null) {
                    switch (status.toUpperCase()) {
                        case "COMPLETED", "PHISHING_TRAINING_COMPLETED" -> completed++;

                        case "EXAM" -> exam++;

                        case "IN_PROGRESS" -> inProgress++;

                        case "NOT_STARTED" -> notStarted++;

                        default -> log.warn("Unknown status '{}' for userSubPackage: {}", status, userSubPackage.getId());
                    }
                } else {
                    log.warn("Null status for userSubPackage: {}", userSubPackage.getId());
                }
            }

            log.info("User subpackage statistics for userId {}: total={}, completed={}, exam={}, inProgress={}, notStarted={}", 
                    userId, total, completed, exam, inProgress, notStarted);

            return UserSubPackageStatisticsDTO.builder()
                    .total(total)
                    .completed(completed)
                    .exam(exam)
                    .inProgress(inProgress)
                    .notStarted(notStarted)
                    .build();

        } catch (Exception e) {
            log.error("Error getting user subpackage statistics count for userId: {}", userId, e);
            // Return empty statistics in case of error
            return UserSubPackageStatisticsDTO.builder()
                    .total(0)
                    .completed(0)
                    .exam(0)
                    .inProgress(0)
                    .notStarted(0)
                    .build();
        }
    }

    @Override
    public List<CompletedTopicResponseDTO> getCompletedTopics(String userId, int offset, int pageSize) {
        log.info("Getting completed topics for userId: {} with offset: {}, pageSize: {}", userId, offset, pageSize);
        
        try {
            // Create pageable with sorting by lastSynced descending (most recent first)
            Pageable pageable = PageRequest.of(offset / pageSize, pageSize, 
                Sort.by(Sort.Direction.DESC, "lastSynced"));
            
            // Get completed topics (progress = 100) for the user
            Page<UserTopicProgress> completedTopicsPage = userTopicProgressRepository
                .findCompletedTopicsByUserId(userId, pageable);
            
            if (completedTopicsPage.isEmpty()) {
                log.info("No completed topics found for userId: {}", userId);
                return List.of();
            }
            
            // Extract topic IDs from completed topics
            List<String> topicIds = completedTopicsPage.getContent().stream()
                .map(UserTopicProgress::getTopicId)
                .distinct()
                .toList();
            
            // Load topic details
            Map<String, Topic> topicMap = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));
            
            // Build response DTOs
            List<CompletedTopicResponseDTO> completedTopics = completedTopicsPage.getContent().stream()
                .map(progress -> {
                    Topic topic = topicMap.get(progress.getTopicId());
                    String topicName = (topic != null) ? topic.getTopicName() : "Unknown Topic";
                    
                    return CompletedTopicResponseDTO.builder()
                        .topicId(progress.getTopicId())
                        .topicName(topicName)
                        .completionDate(progress.getLastSynced())
                        .build();
                })
                .toList();
            
            log.info("Successfully retrieved {} completed topics for userId: {}", 
                completedTopics.size(), userId);
            
            return completedTopics;
            
        } catch (Exception e) {
            log.error("Error getting completed topics for userId: {}", userId, e);
            throw new RuntimeException("Failed to retrieve completed topics", e);
        }
    }

    @Override
    public long countCompletedTopics(String userId) {
        log.info("Counting completed topics for userId: {}", userId);
        
        try {
            // Count completed topics (progress = 100) for the user
            long count = userTopicProgressRepository.countByUserIdAndProgress(userId, 100.0);
            
            log.info("Found {} completed topics for userId: {}", count, userId);
            return count;
            
        } catch (Exception e) {
            log.error("Error counting completed topics for userId: {}", userId, e);
            throw new RuntimeException("Failed to count completed topics", e);
        }
    }

    @Override
    public List<UserSubPackageListResponseDTO> getUserSubPackageList(String userId) {
        log.info("Getting subpackage list for userId: {}", userId);

        try {
            // Fetch all UserSubPackage records for the user
            List<UserSubPackage> userSubPackages = userSubPackageRepository.findByUserId(userId);

            if (userSubPackages.isEmpty()) {
                log.info("No subpackages found for userId: {}", userId);
                return List.of();
            }

            // Convert to response DTOs
            List<UserSubPackageListResponseDTO> responseList = userSubPackages.stream()
                    .map(userSubPackage -> UserSubPackageListResponseDTO.builder()
                            .subPackageId(userSubPackage.getSubPackageId())
                            .subPackageName(userSubPackage.getSubPackageName())
                            .build())
                    .toList();

            log.info("Retrieved {} subpackages for userId: {}", responseList.size(), userId);
            return responseList;

        } catch (Exception e) {
            log.error("Error getting subpackage list for userId: {}", userId, e);
            throw new RuntimeException("Failed to get subpackage list: " + e.getMessage(), e);
        }
    }
}
