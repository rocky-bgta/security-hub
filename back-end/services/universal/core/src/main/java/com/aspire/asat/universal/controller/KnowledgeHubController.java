package com.aspire.asat.universal.controller;

import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubCommentRequest;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubDto;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubLikeRequest;
import com.aspire.asat.universal.knowledgehub.KnowledgeHubRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.service.KnowledgeHubCommentService;
import com.aspire.asat.universal.service.KnowledgeHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge-hub")
@RequiredArgsConstructor
public class KnowledgeHubController {

    private final KnowledgeHubService knowledgeHubService;
    private final KnowledgeHubCommentService knowledgeHubCommentService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<OffsetPageDto<KnowledgeHubDto>>> getAllKnowledge(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "resourceType", required = false) String resourceType,
            @RequestParam(name = "status", required = false) String status) {

        OffsetPageDto<KnowledgeHubDto> pageDto = knowledgeHubService.getAllKnowledgePage(offset, pageSize, search, categoryId, resourceType, status);
        ApiResponseDto<OffsetPageDto<KnowledgeHubDto>> response = new ApiResponseDto<>(
                "Knowledge hub items fetched",
                HttpStatus.OK.value(),
                pageDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<OffsetPageDto<KnowledgeHubDto>>> getActiveKnowledge(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "resourceType", required = false) String resourceType) {

        OffsetPageDto<KnowledgeHubDto> pageDto = knowledgeHubService.getActiveKnowledgePage(offset, pageSize, search, categoryId, resourceType);
        ApiResponseDto<OffsetPageDto<KnowledgeHubDto>> response = new ApiResponseDto<>(
                "Active knowledge hub items fetched",
                HttpStatus.OK.value(),
                pageDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> getKnowledgeById(@PathVariable String id) {
        KnowledgeHubDto knowledge = knowledgeHubService.getKnowledgeById(id);
        if (knowledge != null) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge retrieved successfully",
                    HttpStatus.OK.value(),
                    knowledge
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                "Knowledge not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> getKnowledgeBySlug(@PathVariable String slug) {
        KnowledgeHubDto knowledge = knowledgeHubService.getKnowledgeBySlug(slug);
        if (knowledge != null) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge retrieved successfully",
                    HttpStatus.OK.value(),
                    knowledge
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                "Knowledge not found with slug: " + slug,
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> createKnowledge(@RequestBody KnowledgeHubRequest request) {
        try {
            KnowledgeHubDto created = knowledgeHubService.createKnowledge(request);
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge created successfully",
                    HttpStatus.CREATED.value(),
                    created
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> updateKnowledge(@PathVariable String id,
                                                                            @RequestBody KnowledgeHubRequest request) {
        try {
            KnowledgeHubDto updated = knowledgeHubService.updateKnowledge(id, request);
            if (updated != null) {
                ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                        "Knowledge updated successfully",
                        HttpStatus.OK.value(),
                        updated
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            if (isMspUpdateForbiddenError(e)) {
                return forbiddenResponse(e.getMessage());
            }
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteKnowledge(@PathVariable String id) {
        boolean deleted = knowledgeHubService.deleteKnowledge(id);
        if (deleted) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    "Knowledge deleted successfully",
                    HttpStatus.NO_CONTENT.value(),
                    null
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Knowledge not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PutMapping("/sequence")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> updateKnowledgeSequence(@RequestBody NewsSequenceRequest sequenceRequest) {
        KnowledgeHubDto updated = knowledgeHubService.updateKnowledgeSequence(sequenceRequest);
        if (updated != null) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge sequence updated successfully",
                    HttpStatus.OK.value(),
                    updated
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                "Knowledge not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> likeKnowledge(@PathVariable String id,
                                                                         @RequestBody KnowledgeHubLikeRequest likeRequest) {
        KnowledgeHubDto knowledge = knowledgeHubService.likeKnowledge(id, likeRequest.isLiked());
        if (knowledge != null) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge like status updated successfully",
                    HttpStatus.OK.value(),
                    knowledge
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                "Knowledge not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // --- Comments endpoints ---
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponseDto<List<KnowledgeHubCommentDto>>> getComments(@PathVariable String id) {
        try {
            UUID knowledgeId = UUID.fromString(id);
            List<KnowledgeHubCommentDto> comments = knowledgeHubCommentService.getCommentsForKnowledgeHub(knowledgeId);
            ApiResponseDto<List<KnowledgeHubCommentDto>> response = new ApiResponseDto<>(
                    "Comments retrieved successfully",
                    HttpStatus.OK.value(),
                    comments
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ApiResponseDto<List<KnowledgeHubCommentDto>> response = new ApiResponseDto<>(
                    "Invalid knowledge id format",
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponseDto<KnowledgeHubCommentDto>> addComment(@PathVariable String id,
                                                                             @RequestBody KnowledgeHubCommentRequest request) {
        try {
            UUID knowledgeId = UUID.fromString(id);
            KnowledgeHubCommentDto created = knowledgeHubCommentService.createComment(knowledgeId, request);
            ApiResponseDto<KnowledgeHubCommentDto> response = new ApiResponseDto<>(
                    "Comment created successfully",
                    HttpStatus.CREATED.value(),
                    created
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponseDto<KnowledgeHubCommentDto> response = new ApiResponseDto<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<KnowledgeHubDto>> updateStatus(@PathVariable String id,
                                                                         @RequestParam String status) {
        try {
            Status s = Status.valueOf(status.toUpperCase());
            KnowledgeHubDto updated = knowledgeHubService.updateKnowledgeStatus(id, s);
            if (updated != null) {
                ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                        "Knowledge status updated successfully",
                        HttpStatus.OK.value(),
                        updated
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Knowledge not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Invalid status value: " + status,
                    HttpStatus.BAD_REQUEST.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                    "Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<ApiResponseDto<KnowledgeHubDto>> forbiddenResponse(String message) {
        ApiResponseDto<KnowledgeHubDto> response = new ApiResponseDto<>(
                message,
                HttpStatus.UNAUTHORIZED.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    private boolean isMspUpdateForbiddenError(IllegalArgumentException e) {
        return e.getMessage() != null && e.getMessage().contains("cannot be updated by MSP");
    }

}
