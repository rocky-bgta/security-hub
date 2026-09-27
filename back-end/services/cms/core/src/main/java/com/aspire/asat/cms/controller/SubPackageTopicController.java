package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.bookmark.BookmarkRequestDto;
import com.aspire.asat.cms.dto.subPackage.UserSubpackageResponse;
import com.aspire.asat.cms.dto.topic.TopicSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.SUB_PACKAGE_TOPIC_API)
@Tag(name = "SubPackage Topic and Bookmark Management", description = "APIs for managing topics within sub-packages")
public interface SubPackageTopicController {

    @GetMapping("/{subPackageId}")
    @Operation(summary = "Get topic details by sub-package ID", description = "Retrieves detailed topic information for all topics in a specific sub-package for the specified user")
    ResponseEntity<ApiResponseDto<List<TopicSummaryDto>>> getTopicDetailsBySubPackageId(
            @Parameter(description = "Unique identifier of the sub-package", required = true)
            @PathVariable @NotBlank String subPackageId,
            @Parameter(description = "Unique identifier of the user")
            @RequestParam String userId
    );

    @GetMapping()
    @Operation(summary = "Get User assigned Sub Packages", description = "fetch all sub-packages assigned to a user along with their topics")
    ResponseEntity<ApiResponseDto<List<UserSubpackageResponse>>> getTopicsByUserSubPackage(
            @RequestParam @NotBlank String userId,
            @RequestParam(value = "status", required = false) String status
    );

    @PostMapping("/bookmark")
    @Operation(summary = "Toggle bookmark for a topic", description = "Bookmark or remove bookmark from a topic based on the isBookmarked field")
    ResponseEntity<ApiResponseDto<Boolean>> toggleBookmark(
            @Valid @RequestBody BookmarkRequestDto request
    );

    @GetMapping("/bookmarked")
    @Operation(summary = "Get all bookmarked topics for a user", description = "Retrieves all topics that have been bookmarked by the specified user")
    ResponseEntity<ApiResponseDto<List<TopicSummaryDto>>> getBookmarkedTopicsByUserId(
            @Parameter(description = "Unique identifier of the user", required = true)
            @RequestParam @NotBlank String userId
    );
}
