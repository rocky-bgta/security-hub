package com.aspire.asat.cms.controller.topic;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.CourseTopicStatsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.TopicDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.SubPackageStatisticsItemDTO;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.dashboard.UserDashboardTopicsProgressResponseDto;
import com.aspire.asat.cms.dto.topic.TopicDetailResponseDto;
import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.dto.topic.TopicFilterResponse;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.TOPIC_API)
@Tag(name = "Topic Management",
        description = "APIs for managing topics. Create/update/delete: ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER only. "
                + "List and get-by-id are available to other authenticated users.")
public interface TopicController {

    @PostMapping
    @Operation(summary = "Create a new topic",
            description = "Creates a new topic with the provided details. Platform admin only "
                    + "(ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Topic created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Topic with same name already exists")
    })
    ResponseEntity<ApiResponseDto<TopicRespDto>> createTopic(@Valid @RequestBody TopicReqDto topicReqDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update an existing topic",
            description = "Updates topic details by ID. Platform admin only "
                    + "(ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Topic not found"),
            @ApiResponse(responseCode = "409", description = "Topic with same name already exists")
    })
    ResponseEntity<ApiResponseDto<TopicRespDto>> updateTopic(@PathVariable("id") String id, @RequestBody TopicReqDto topicReqDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get topic by ID with details", description = "Retrieves topic details by ID with category, country, compliance, and content type details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic retrieved successfully with details"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    ResponseEntity<ApiResponseDto<TopicDetailResponseDto>> getTopicByIdWithDetails(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all topics", description = "Retrieves paginated list of topics with optional status filtering")
    @ApiResponse(responseCode = "200", description = "Topics retrieved successfully")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicRespDto>>>> getAllTopics(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) TopicStatus status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order
    );

    @GetMapping("/private")
    @Operation(summary = "Get current client's private topics",
            description = "Returns a paginated list of private topics owned by the clientAdminId from CurrentContext "
                    + "(isPrivate=true). Does not include public catalog topics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Private topics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - client context not found")
    })
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicRespDto>>>> getPrivateTopics(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) TopicStatus status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order
    );

    @GetMapping(WebApiUrlConstants.EXISTS)
    @Operation(summary = "Check if topic name exists",
            description = "Returns true when a topic with the given name already exists (case-insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    ResponseEntity<ApiResponseDto<Boolean>> existsByTopicName(
            @Parameter(description = "Topic name to check")
            @RequestParam(value = "topicName", required = true) String topicName
    );

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete a topic",
            description = "Soft deletes a topic by setting status to INACTIVE. Platform admin only "
                    + "(ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Topic deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    ResponseEntity<ApiResponseDto<Void>> deleteTopicById(@PathVariable("id") String id);

    @PostMapping(value = "/filter/by-package/{packageId}")
    @Operation(summary = "Filter topics by package",
            description = "Filter topics based on various criteria for a specific package. "
                    + "When selectedTopicId is provided and non-empty, those topics are returned first, "
                    + "followed by the remaining paginated topics. Empty/null selectedTopicId keeps existing behavior.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered topics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid packageId")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopicsByPackage(
            @PathVariable("packageId") String packageId,
            @RequestBody TopicFilterRequest topicFilterRequest);

    @PostMapping(value = "/filter/by-product/{productId}")
    @Operation(summary = "Filter topics by product", description = "Filter topics based on various criteria for a specific product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered topics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid productId")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopicsByProduct(
            @PathVariable("productId") String productId,
            @RequestBody TopicFilterRequest topicFilterRequest);

    @PostMapping(value = "/filter")
    @Operation(summary = "Filter topics", description = "Filter topics by metadata criteria without product or package scope.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered topics retrieved successfully")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopics(
            @RequestBody TopicFilterRequest topicFilterRequest);

    @PostMapping(value = "/recommend/by-product/{productId}")
    @Operation(summary = "Recommend topics by product",
            description = "Ranks ENABLED topics for a product by maximum soft-matching criteria. "
                    + "Unmatched criteria (e.g. industry) do not exclude topics. Existing /filter APIs are unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommended topics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid productId")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopicsByProduct(
            @PathVariable("productId") String productId,
            @RequestBody TopicFilterRequest topicFilterRequest);

    @PostMapping(value = "/recommend/by-package/{packageId}")
    @Operation(summary = "Recommend topics by package",
            description = "Ranks ENABLED topics for a package by maximum soft-matching criteria. "
                    + "Unmatched criteria do not exclude topics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommended topics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid packageId")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopicsByPackage(
            @PathVariable("packageId") String packageId,
            @RequestBody TopicFilterRequest topicFilterRequest);

    @PostMapping(value = "/recommend")
    @Operation(summary = "Recommend topics",
            description = "Ranks topics without product/package scope by maximum soft-matching criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommended topics retrieved successfully")
    })
    ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopics(
            @RequestBody TopicFilterRequest topicFilterRequest);

    // ========== CLIENT-FACING TOPIC APIs ==========

    @Operation(summary = "Get course topic statistics", description = "Returns content stats (total, completed, pending) per course for a user with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course topic stats fetched"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/topics/stats")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseTopicStatsItemDTO>>>> getCourseTopicStats(
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get topic details", description = "Returns topic metadata and which contents are completed by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic details fetched"),
            @ApiResponse(responseCode = "404", description = "Topic not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/topics/{topicId}")
    ResponseEntity<ApiResponseDto<TopicDetailsResponseDTO>> getTopicDetails(
            @PathVariable("topicId") @NotBlank String topicId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String subPackageId);

    @Operation(summary = "Get user subpackage statistics", description = "Returns subpackage statistics (total, completed, pending, in-progress, exam) for the current user with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subpackage statistics fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User context not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/user/topics/statistics")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageStatisticsItemDTO>>>> getUserSubPackageStatistics(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get topics progress for current user", description = "Returns each topic progress with start and expiry dates for the current user context with optional search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topics progress fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User context not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/user/topics-progress")
    ResponseEntity<ApiResponseDto<UserDashboardTopicsProgressResponseDto>> getCurrentUserTopicsProgress(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) String search
    );

    @Operation(summary = "Get topics by product and package", description = "Retrieves minimal topic information (name, description, duration) filtered by productId and packageId with optional search, pagination and sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topics fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/product/package/topics")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>>> getTopicsByProductAndPackage(
            @RequestParam @NotBlank String productId,
            @RequestParam @NotBlank String packageId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "desc", required = false) String order
    );

    @Operation(summary = "Get topics by product/package pairs",
            description = "Retrieves ENABLED topics matching any of the provided productId/packageId key-value pairs. "
                    + "Set excludeMatchingPairs=true to return topics that do not match any pair.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topics fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/by-product-packages")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>>> getTopicsByProductPackages(
            @Valid @RequestBody TopicsByProductPackagesRequest request
    );

    @Operation(summary = "Monthly topic distribution by MSP and client product/package pairs",
            description = "Returns 12 months for the current year (same shape as /topics/distribution). "
                    + "totalContent = ENABLED topics matching mspProductPackages created that month; "
                    + "usedContent = ENABLED topics matching clientProductPackages created that month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic distribution retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/counts-by-product-packages")
    ResponseEntity<ApiResponseDto<TopicCountsByProductPackagesResponseDto>> countTopicsByProductPackages(
            @Valid @RequestBody TopicCountsByProductPackagesRequest request
    );

    @Operation(summary = "Get topic distribution for 12 months", description = "Returns topic distribution data showing total active topics and used unique topics per month for the current year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic distribution fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/distribution")
    ResponseEntity<ApiResponseDto<com.aspire.asat.cms.dto.topic.TopicDistributionResponseDto>> getTopicDistribution();

    @Operation(summary = "Get topic by ID for MSP",
            description = "Returns topic category names, available country names, product name, content type, duration, "
                    + "and all packages for the topic's product from product_packages with isPurchase flags "
                    + "based on msp_products for the given MSP. "
                    + "When the caller is an MSP user, mspId is taken from context; otherwise mspId must be passed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic retrieved successfully for MSP"),
            @ApiResponse(responseCode = "400", description = "Missing mspId"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @GetMapping("/msp/{topicId}")
    ResponseEntity<ApiResponseDto<com.aspire.asat.cms.dto.topic.MspTopicViewResponseDto>> getTopicForMsp(
            @PathVariable("topicId") @NotBlank String topicId,
            @RequestParam(value = "mspId", required = false) String mspId
    );

}
