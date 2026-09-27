package com.aspire.asat.universal.controller;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.news.LatestNewsDto;
import com.aspire.asat.universal.news.LatestNewsRequest;
import com.aspire.asat.universal.news.NewsCommentDto;
import com.aspire.asat.universal.news.NewsCommentRequest;
import com.aspire.asat.universal.news.NewsLikeRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.service.LatestNewsService;
import com.aspire.asat.universal.service.NewsCommentService;
import com.aspire.asat.common.enums.ActivityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/latest-news")
public class LatestNewsController {

    private final LatestNewsService latestNewsService;
    private final NewsCommentService newsCommentService;

    @Autowired
    public LatestNewsController(LatestNewsService latestNewsService, NewsCommentService newsCommentService) {
        this.latestNewsService = latestNewsService;
        this.newsCommentService = newsCommentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<OffsetPageDto<LatestNewsDto>>> getAllNews(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search) {

        OffsetPageDto<LatestNewsDto> pageDto = latestNewsService.getAllNewsPage(offset, pageSize, status, category, search);
        ApiResponseDto<OffsetPageDto<LatestNewsDto>> response = new ApiResponseDto<>(
                "News fetched successfully",
                HttpStatus.OK.value(),
                pageDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<OffsetPageDto<LatestNewsDto>>> getActiveNews(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search) {

        OffsetPageDto<LatestNewsDto> pageDto = latestNewsService.getActiveNewsPage(offset, pageSize, status, category, search);
        ApiResponseDto<OffsetPageDto<LatestNewsDto>> response = new ApiResponseDto<>(
                "Active news fetched successfully",
                HttpStatus.OK.value(),
                pageDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> getNewsById(@PathVariable String id) {
        LatestNewsDto news = latestNewsService.getNewsById(id);
        if (news != null) {
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News retrieved successfully",
                    HttpStatus.OK.value(),
                    news
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                "News not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> getNewsBySlug(@PathVariable String slug) {
        LatestNewsDto news = latestNewsService.getNewsBySlug(slug);
        if (news != null) {
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News retrieved successfully",
                    HttpStatus.OK.value(),
                    news
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                "News not found with slug: " + slug,
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created news: #{#newsRequest.title != null ? #newsRequest.title : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> createNews(@RequestBody LatestNewsRequest newsRequest) {
        try {
            LatestNewsDto createdNews = latestNewsService.createNews(newsRequest);
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News created successfully",
                    HttpStatus.CREATED.value(),
                    createdNews
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if (isMspForbiddenError(e)) {
                return forbiddenResponse(e.getMessage());
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}")
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated news: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#newsRequest.title != null ? #newsRequest.title : #id}"
    )
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> updateNews(@PathVariable String id,
                                                  @RequestBody LatestNewsRequest newsRequest) {
        try {
            LatestNewsDto updatedNews = latestNewsService.updateNews(id, newsRequest);
            if (updatedNews != null) {
                ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                        "News updated successfully",
                        HttpStatus.OK.value(),
                        updatedNews
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            if (isMspForbiddenError(e)) {
                return forbiddenResponse(e.getMessage());
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteNews(@PathVariable String id) {
        try {
            boolean deleted = latestNewsService.deleteNews(id);
            if (deleted) {
                ApiResponseDto<String> response = new ApiResponseDto<>(
                        "News deleted successfully",
                        HttpStatus.NO_CONTENT.value(),
                        null
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    "News not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            if (isMspForbiddenError(e)) {
                ApiResponseDto<String> response = new ApiResponseDto<>(
                        e.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        null
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/sequence")
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> updateNewsSequence(@RequestBody NewsSequenceRequest sequenceRequest) {
        try {
            LatestNewsDto updatedNews = latestNewsService.updateNewsSequence(sequenceRequest);
            if (updatedNews != null) {
                ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                        "News sequence updated successfully",
                        HttpStatus.OK.value(),
                        updatedNews
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            if (isMspForbiddenError(e)) {
                return forbiddenResponse(e.getMessage());
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> likeNews(@PathVariable String id,
                                               @RequestBody NewsLikeRequest likeRequest) {
        LatestNewsDto news = latestNewsService.likeNews(id, likeRequest.isLiked());
        if (news != null) {
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News like status updated successfully",
                    HttpStatus.OK.value(),
                    news
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                "News not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // --- Comments endpoints ---
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponseDto<List<NewsCommentDto>>> getComments(@PathVariable String id) {
        try {
            UUID newsId = UUID.fromString(id);
            List<NewsCommentDto> comments = newsCommentService.getCommentsForNews(newsId);
            ApiResponseDto<List<NewsCommentDto>> response = new ApiResponseDto<>(
                    "Comments retrieved successfully",
                    HttpStatus.OK.value(),
                    comments
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponseDto<List<NewsCommentDto>> response = new ApiResponseDto<>(
                    "Invalid news id format",
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponseDto<NewsCommentDto>> addComment(@PathVariable String id,
                                                                     @RequestBody NewsCommentRequest request) {
        try {
            UUID newsId = UUID.fromString(id);
            NewsCommentDto created = newsCommentService.createComment(newsId, request);
            ApiResponseDto<NewsCommentDto> response = new ApiResponseDto<>(
                    "Comment created successfully",
                    HttpStatus.CREATED.value(),
                    created
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponseDto<NewsCommentDto> response = new ApiResponseDto<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // --- Status update endpoint (update only the status field) ---
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<LatestNewsDto>> updateStatus(@PathVariable String id,
                                                                      @RequestParam String status) {
        try {
            // validate and parse status
            Status s = Status.valueOf(status.toUpperCase());
            LatestNewsDto updated = latestNewsService.updateNewsStatus(id, s);
            if (updated != null) {
                ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                        "News status updated successfully",
                        HttpStatus.OK.value(),
                        updated
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "News not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException ex) {
            if (isMspForbiddenError(ex)) {
                return forbiddenResponse(ex.getMessage());
            }
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "Invalid status value: " + status,
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                    "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<ApiResponseDto<LatestNewsDto>> forbiddenResponse(String message) {
        ApiResponseDto<LatestNewsDto> response = new ApiResponseDto<>(
                message,
                HttpStatus.UNAUTHORIZED.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    private boolean isMspForbiddenError(IllegalArgumentException e) {
        return e.getMessage() != null && e.getMessage().contains("by MSP");
    }

}
