package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.bookmark.BookmarkRequestDto;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.subPackage.UserSubpackageResponse;
import com.aspire.asat.cms.dto.topic.ChapterDto;
import com.aspire.asat.cms.dto.topic.TopicDetailResponseDto;
import com.aspire.asat.cms.dto.topic.TopicSummaryDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Chapter;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.model.UserTopicProgress;
import com.aspire.asat.cms.repository.ChapterRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.UserTopicProgressRepository;
import com.aspire.asat.cms.service.SubPackageTopicService;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubPackageTopicServiceImpl implements SubPackageTopicService {
    private final SubPackageRepository subPackageRepository;
    private final ChapterRepository chapterRepository;
    private final TopicService topicService;
    private final ProductRepository productRepository;
    private final UserTopicProgressRepository userTopicProgressRepository;
    private final UserSubPackageRepository userSubPackageRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public List<TopicSummaryDto> getTopicDetailsBySubPackageId(String subPackageId, String userId) {
        log.info("Getting topic details for sub-package: {} and user: {}", subPackageId, userId);

        try {
            // 1. Find the subpackage to get topic IDs
            SubPackage subPackage = subPackageRepository.findById(subPackageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-package not found: " + subPackageId));

            List<String> topicIds = subPackage.getTopicId();
            if (topicIds == null || topicIds.isEmpty()) {
                log.info("No topics found for sub-package: {}", subPackageId);
                return List.of();
            }

            log.info("Found {} topics for sub-package: {}", topicIds.size(), subPackageId);

            // 2. Get topic summary for all topic IDs
            List<TopicSummaryDto> topicSummaries = topicIds.stream()
                    .map(topicId -> {
                        try {
                            return getTopicSummaryByIdForUserSubpackage(topicId, userId, subPackageId);
                        } catch (ResourceNotFoundException e) {
                            log.warn("Topic not found: {} for sub-package: {}", topicId, subPackageId);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            log.info("Successfully retrieved {} topic summaries for sub-package: {} and user: {}",
                    topicSummaries.size(), subPackageId, userId);
            return topicSummaries;

        } catch (Exception e) {
            log.error("Error retrieving topic details for sub-package: {} and user: {}", subPackageId, userId, e);
            throw new ResourceNotFoundException("Failed to retrieve topic details for sub-package: " + subPackageId);
        }
    }

    @Override
    public List<UserSubpackageResponse> getTopicsByUserSubPackage(String userId, String status) {
        log.info("Getting topics for user sub-packages: {} with status filter: {}", userId, status);

        List<UserSubPackage> userSubPackages;

        if (status != null && !status.trim().isEmpty()) {
            String statusFilter = status.trim().toUpperCase();

            if (SubPackageStatus.COMPLETED.name().equalsIgnoreCase(statusFilter)) {
                // If the status is COMPLETE, return only COMPLETE status data
                userSubPackages = userSubPackageRepository.findByUserIdAndStatus(userId, SubPackageStatus.COMPLETED.name());
                log.info("Filtering for COMPLETE status only");
            } else {
                // If status is not COMPLETE, return all except COMPLETE ordered by assignment date
                List<UserSubPackage> allUserSubPackages = userSubPackageRepository.findByUserIdOrderByAssignedDateAsc(userId);
                userSubPackages = allUserSubPackages.stream()
                        .filter(subPackage -> !SubPackageStatus.COMPLETED.name().equals(subPackage.getStatus()))
                        .toList();
                log.info("Filtering for all statuses except COMPLETE, ordered by assignment date");
            }
        } else {
            // If no status filter provided, return all subpackages ordered by assignment date
            userSubPackages = userSubPackageRepository.findByUserIdOrderByAssignedDateAsc(userId);
            log.info("No status filter provided, returning all subpackages ordered by assignment date");
        }

        if (userSubPackages.isEmpty()) {
            log.info("No sub-packages found for user: {} with status filter: {}", userId, status);
            return List.of();
        }

        log.info("Found {} sub-packages for user: {} with status filter: {}", userSubPackages.size(), userId, status);

        return userSubPackages.stream()
                .map(this::convertToUserSubpackageResponse)
                .toList();
    }


    private UserSubpackageResponse convertToUserSubpackageResponse(UserSubPackage userSubPackage) {
        // Get subpackage details
        SubPackage subPackage = subPackageRepository.findById(userSubPackage.getSubPackageId())
                .orElse(null);

        // Get product details
        Product product = null;
        if (subPackage != null && subPackage.getProductId() != null) {
            product = productRepository.findById(subPackage.getProductId()).orElse(null);
        }

        // Get topic count
        long topicCount = 0L;
        if (subPackage != null && subPackage.getTopicId() != null) {
            topicCount = subPackage.getTopicId().size();
        }

        return UserSubpackageResponse.builder()
                .subPackageId(userSubPackage.getSubPackageId())
                .subPackageName(userSubPackage.getSubPackageName())
                .productId(subPackage != null ? subPackage.getProductId() : null)
                .productName(product != null ? product.getProductName() : null)
                .progress(userSubPackage.getProgress())
                .topicCount(topicCount)
                .status(userSubPackage.getStatus())
                .assignedAt(userSubPackage.getAssignedDate() != null ?
                        userSubPackage.getAssignedDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null)
                .expiryDate(userSubPackage.getExpiryDate() != null ?
                        userSubPackage.getExpiryDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null)
                .build();
    }

    private TopicSummaryDto getTopicSummaryById(String topicId, String userId, String subPackageId, Boolean isBookmarked) {
        // Get topic details
        TopicDetailResponseDto topicDetail = topicService.getByIdWithDetails(topicId);

        // Get chapter details
        List<ChapterDto> chapterDtos = List.of();
        if (topicDetail.getChapterIds() != null && !topicDetail.getChapterIds().isEmpty()) {
            List<Chapter> chapters = chapterRepository.findAllById(topicDetail.getChapterIds());
            chapterDtos = chapters.stream()
                    .map(chapter -> ChapterDto.builder()
                            .id(chapter.getId())
                            .chapterName(chapter.getChapterName())
                            .build())
                    .toList();
        }

        return TopicSummaryDto.builder()
                .id(topicDetail.getId())
                .topicName(topicDetail.getTopicName())
                .description(topicDetail.getDescription())
                .status(getUserTopicStatus(userId, topicId, subPackageId))
                .topicProgress(getTopicProgress(topicId, userId, subPackageId)) // Default progress can be calculated based on user progress
                .chapterIds(chapterDtos)
                .thumbnailUrl(topicDetail.getThumbnailUrl())
                .durationMinutes(topicDetail.getDurationMinutes())
                .totalContentCount(topicDetail.getTotalContentCount())
                .subPackageValidity(getSubPackageExpiryDate(userId, subPackageId))
                .subPackageId(subPackageId)
                .isBookmarked(isBookmarked != null ? isBookmarked : false)
                .build();
    }

    private TopicStatus getUserTopicStatus(String userId, String topicId, String subPackageId){
        return userTopicProgressRepository.findByUserIdAndTopicIdAndSubPackageId(userId, topicId, subPackageId)
                .map(ut -> TopicStatus.getCompletedOrPendingStatus(ut.getStatus()))
                .orElse(TopicStatus.PENDING);
    }

    private LocalDate getSubPackageExpiryDate(String userId, String subPackageId) {
        UserSubPackage userSubPackage = userSubPackageRepository.findByUserIdAndSubPackageId(userId, subPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("User Sub Package not found with userId: " + userId + " and subPackageId: " + subPackageId));
        return userSubPackage.getExpiryDate();
    }


    private double getTopicProgress(String topicId, String userId, String subPackageId) {
        return userTopicProgressRepository.findByUserIdAndTopicIdAndSubPackageId(userId, topicId, subPackageId)
                .map(UserTopicProgress::getProgress)
                .orElse(0.0);
    }

    @Override
    public boolean toggleBookmark(BookmarkRequestDto request) {
        log.info("Toggling bookmark for user: {}, topic: {}, subPackage: {}, isBookmarked: {}",
                request.getUserId(), request.getTopicId(), request.getSubPackageId(), request.isBookmarked());

        UserTopicProgress existingProgress = userTopicProgressRepository
                .findByUserIdAndTopicIdAndSubPackageId(request.getUserId(), request.getTopicId(), request.getSubPackageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Topic progress not found with userId: %s, topicId: %s, subPackageId: %s",
                                request.getUserId(), request.getTopicId(), request.getSubPackageId())
                ));

        // can't bookmark if already bookmarked
        if (existingProgress.getIsBookmarked() != null && existingProgress.getIsBookmarked() && request.isBookmarked()) {
            throw new ResourceAlreadyExistsException("Topic is already bookmarked for this user and sub-package.");
        }

        // Update bookmark status based on the request
        Instant now = Instant.now();
        String currentUserId = getCurrentUserId();
        existingProgress.setIsBookmarked(request.isBookmarked());
        existingProgress.setLastSynced(now);
        // Update audit fields
        existingProgress.setUpdatedAt(now);
        existingProgress.setUpdatedBy(currentUserId);
        userTopicProgressRepository.save(existingProgress);

        String action = request.isBookmarked() ? "bookmarked" : "unbookmarked";
        log.info("Topic {} successfully for user: {}", action, request.getUserId());

        return request.isBookmarked();
    }

    @Override
    public List<TopicSummaryDto> getBookmarkedTopicsByUserId(String userId) {
        log.info("Getting all bookmarked topics for user: {}", userId);

        List<UserTopicProgress> bookmarkedProgress = userTopicProgressRepository
                .findByUserIdAndIsBookmarkedTrue(userId);

        if (bookmarkedProgress.isEmpty()) {
            log.info("No bookmarked topics found for user: {}", userId);
            return List.of();
        }

        log.info("Found {} bookmarked topics for user: {}", bookmarkedProgress.size(), userId);

        // Convert to TopicSummaryDto with isBookmarked = true
        return bookmarkedProgress.stream()
                .map(progress -> {
                    TopicSummaryDto topicSummary = getTopicSummaryById(progress.getTopicId(), userId, progress.getSubPackageId(), progress.getIsBookmarked());
                    if (topicSummary != null) {
                        topicSummary.setIsBookmarked(true);
                    }
                    return topicSummary;
                })
                .filter(Objects::nonNull)
                .toList();

    }

    /**
     * Helper method to get current user ID from context safely
     * Returns null if context is not available
     */
    private String getCurrentUserId() {
        try {
            return userCurrentContextService.getCurrentUserContext().getUserId();
        } catch (Exception e) {
            log.debug("Unable to get current user context, returning null for audit fields");
            return null;
        }
    }

    private TopicSummaryDto getTopicSummaryByIdForUserSubpackage(String topicId, String userId, String subPackageId) {
        // Get topic details
        TopicDetailResponseDto topicDetail = topicService.getByIdWithDetails(topicId);

        UserTopicProgress userTopicProgress = userTopicProgressRepository
                .findByUserIdAndTopicIdAndSubPackageId(userId, topicId, subPackageId)
                .orElse(null);
        Boolean isBookmarked = null;
        if (userTopicProgress != null) {
            isBookmarked = userTopicProgress.getIsBookmarked();
        }

        // Get chapter details
        List<ChapterDto> chapterDtos = List.of();
        if (topicDetail.getChapterIds() != null && !topicDetail.getChapterIds().isEmpty()) {
            List<Chapter> chapters = chapterRepository.findAllById(topicDetail.getChapterIds());
            chapterDtos = chapters.stream()
                    .map(chapter -> ChapterDto.builder()
                            .id(chapter.getId())
                            .chapterName(chapter.getChapterName())
                            .build())
                    .toList();
        }

        return TopicSummaryDto.builder()
                .id(topicDetail.getId())
                .topicName(topicDetail.getTopicName())
                .description(topicDetail.getDescription())
                .status(getUserTopicStatus(userId, topicId, subPackageId))
                .topicProgress(getTopicProgress(topicId, userId, subPackageId)) // Default progress can be calculated based on user progress
                .chapterIds(chapterDtos)
                .thumbnailUrl(topicDetail.getThumbnailUrl())
                .durationMinutes(topicDetail.getDurationMinutes())
                .totalContentCount(topicDetail.getTotalContentCount())
                .subPackageValidity(getSubPackageExpiryDate(userId, subPackageId))
                .subPackageId(subPackageId)
                .isBookmarked(isBookmarked != null ? isBookmarked : false)
                .build();
    }

}
