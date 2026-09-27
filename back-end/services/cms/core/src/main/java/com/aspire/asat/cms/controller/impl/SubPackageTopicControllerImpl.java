package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.SubPackageTopicController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.bookmark.BookmarkRequestDto;
import com.aspire.asat.cms.dto.subPackage.UserSubpackageResponse;
import com.aspire.asat.cms.dto.topic.TopicSummaryDto;
import com.aspire.asat.cms.service.SubPackageTopicService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SubPackageTopicControllerImpl implements SubPackageTopicController {

    private final SubPackageTopicService subPackageTopicService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<List<TopicSummaryDto>>> getTopicDetailsBySubPackageId(String subPackageId, String userId) {
        if (subPackageId == null || subPackageId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("SubPackageId is mandatory", HttpStatus.BAD_REQUEST.value(), null));
        }

        if (userId == null || userId.trim().isEmpty()) {
            userId = userCurrentContextService.getCurrentUserContext().getUserId();
        }

        try {
            List<TopicSummaryDto> topics = subPackageTopicService.getTopicDetailsBySubPackageId(subPackageId, userId);
            return ResponseEntity.ok(new ApiResponseDto<>("Sub Package List fetched successfully", HttpStatus.OK.value(), topics));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid argument: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        } catch (Exception ex) {
            log.error("Error fetching topic details", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch topic details", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserSubpackageResponse>>> getTopicsByUserSubPackage(String userId, String status) {
        try {
            List<UserSubpackageResponse> topics = subPackageTopicService.getTopicsByUserSubPackage(userId, status);
            return ResponseEntity.ok(new ApiResponseDto<>("Topics fetched successfully for user sub-packages", HttpStatus.OK.value(), topics));
        } catch (Exception ex) {
            log.error("Error fetching topics for user sub-packages", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch topics for user sub-packages", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> toggleBookmark(BookmarkRequestDto request) {
        boolean result = subPackageTopicService.toggleBookmark(request);
        String message = result ? "Topic bookmarked successfully" : "Topic unbookmarked successfully";
        return ResponseEntity.ok(new ApiResponseDto<>(message, HttpStatus.OK.value(), result));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<TopicSummaryDto>>> getBookmarkedTopicsByUserId(String userId) {

        List<TopicSummaryDto> bookmarkedTopics = subPackageTopicService.getBookmarkedTopicsByUserId(userId.trim());
        return ResponseEntity.ok(new ApiResponseDto<>("Bookmarked topics fetched successfully", HttpStatus.OK.value(), bookmarkedTopics));
    }
}
