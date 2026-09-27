package com.aspire.asat.cms.controller.impl.topic;

import com.aspire.asat.cms.controller.topic.TopicController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.CourseTopicStatsItemDTO;
import com.aspire.asat.cms.dto.client.responseDto.TopicDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.SubPackageStatisticsItemDTO;
import com.aspire.asat.cms.dto.dashboard.UserDashboardTopicsProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.TopicDetailResponseDto;
import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.dto.topic.TopicFilterResponse;
import com.aspire.asat.cms.dto.topic.TopicDistributionResponseDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesPageDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesResponseDto;
import com.aspire.asat.cms.dto.topic.MspTopicViewResponseDto;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TopicControllerImpl implements TopicController {
    private final TopicService topicService;
    private final ClientUserOperationService clientUserOperationService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<TopicRespDto>> createTopic(@Valid TopicReqDto topicReqDto) {
        log.info("Request received to create topic: {}", topicReqDto);
        TopicRespDto createdTopic = topicService.createTopic(topicReqDto);
        ApiResponseDto<TopicRespDto> response = new ApiResponseDto<>("Topic created successfully", 201, createdTopic);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicRespDto>> updateTopic(String id, TopicReqDto topicReqDto) {
        log.info("Request received to update topic with id: {} and details: {}", id, topicReqDto);
        TopicRespDto updatedTopic = topicService.updateTopic(id, topicReqDto);
        ApiResponseDto<TopicRespDto> response = new ApiResponseDto<>("Topic updated successfully", 200, updatedTopic);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicDetailResponseDto>> getTopicByIdWithDetails(String id) {
        log.info("Request received to fetch topic with details for id: {}", id);
        TopicDetailResponseDto topic = topicService.getByIdWithDetails(id);
        ApiResponseDto<TopicDetailResponseDto> response = new ApiResponseDto<>("Topic fetched successfully with details", 200, topic);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicRespDto>>>> getAllTopics(String search, TopicStatus status, Integer page, Integer size, String sortBy, String order) {
        log.info("Request received to fetch all topics with search: {}, status: {}, page: {}, size: {}, sortBy: {}, order: {}", search, status, page, size, sortBy, order);
        
        // Get paginated topics with filtering
        Page<TopicRespDto> topicPage = topicService.getAllTopics(search, status, page, size, sortBy, order);
        
        AllResponseDto<List<TopicRespDto>> allResponseDto = new AllResponseDto<>(
            page, 
            size, 
            topicPage.getTotalElements(), 
            topicPage.getContent()
        );
        
        ApiResponseDto<AllResponseDto<List<TopicRespDto>>> response = new ApiResponseDto<>("Topics fetched successfully", 200, allResponseDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicRespDto>>>> getPrivateTopics(
            String search, TopicStatus status, Integer page, Integer size, String sortBy, String order) {
        log.info("Request received to fetch private topics search={}, status={}, page={}, size={}",
                search, status, page, size);
        Page<TopicRespDto> topicPage = topicService.getPrivateTopicsForCurrentClient(
                search, status, page, size, sortBy, order);
        AllResponseDto<List<TopicRespDto>> allResponseDto = new AllResponseDto<>(
                page,
                size,
                topicPage.getTotalElements(),
                topicPage.getContent()
        );
        ApiResponseDto<AllResponseDto<List<TopicRespDto>>> response =
                new ApiResponseDto<>("Private topics fetched successfully", 200, allResponseDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> existsByTopicName(String topicName) {
        log.info("Request received to check if topic name exists: {}", topicName);
        boolean exists = topicService.existsByTopicName(topicName);
        return ResponseEntity.ok(new ApiResponseDto<>("Check completed", HttpStatus.OK.value(), exists));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteTopicById(String id) {
        log.info("Request received to delete topic with id: {}", id);
        topicService.deleteTopicById(id);
        ApiResponseDto<Void> response = new ApiResponseDto<>("Topic deleted successfully", 200, null);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopicsByPackage(String packageId, TopicFilterRequest topicFilterRequest) {
        log.info("Request received to filter topics for package: {} with criteria: {}", packageId, topicFilterRequest);
        
        // Validate packageId
        if (packageId == null || packageId.trim().isEmpty()) {
            log.error("PackageId is mandatory for topic filtering");
            ApiResponseDto<TopicFilterResponse> errorResponse = new ApiResponseDto<>("PackageId is mandatory for topic filtering", 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        TopicFilterResponse filterResponse = topicService.filterTopicsByPackage(packageId, topicFilterRequest);
        ApiResponseDto<TopicFilterResponse> response = new ApiResponseDto<>("Topics filtered successfully", 200, filterResponse);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopicsByProduct(String productId, TopicFilterRequest topicFilterRequest) {
        log.info("Request received to filter topics for product: {} with criteria: {}", productId, topicFilterRequest);
        
        // Validate productId
        if (productId == null || productId.trim().isEmpty()) {
            log.error("ProductId is mandatory for topic filtering");
            ApiResponseDto<TopicFilterResponse> errorResponse = new ApiResponseDto<>("ProductId is mandatory for topic filtering", 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        TopicFilterResponse filterResponse = topicService.filterTopicsByProduct(productId, topicFilterRequest);
        ApiResponseDto<TopicFilterResponse> response = new ApiResponseDto<>("Topics filtered successfully", 200, filterResponse);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> filterTopics(TopicFilterRequest topicFilterRequest) {
        log.info("Request received to filter topics (unscoped) with criteria: {}", topicFilterRequest);
        TopicFilterResponse filterResponse = topicService.filterTopics(topicFilterRequest);
        ApiResponseDto<TopicFilterResponse> response = new ApiResponseDto<>("Topics filtered successfully", 200, filterResponse);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopicsByProduct(
            String productId, TopicFilterRequest topicFilterRequest) {
        log.info("Request received to recommend topics for product: {} with criteria: {}", productId, topicFilterRequest);
        if (productId == null || productId.trim().isEmpty()) {
            ApiResponseDto<TopicFilterResponse> errorResponse =
                    new ApiResponseDto<>("ProductId is mandatory for topic recommendation", 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        TopicFilterResponse recommendResponse = topicService.recommendTopicsByProduct(productId, topicFilterRequest);
        return ResponseEntity.ok(new ApiResponseDto<>("Recommended topics retrieved successfully", 200, recommendResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopicsByPackage(
            String packageId, TopicFilterRequest topicFilterRequest) {
        log.info("Request received to recommend topics for package: {} with criteria: {}", packageId, topicFilterRequest);
        if (packageId == null || packageId.trim().isEmpty()) {
            ApiResponseDto<TopicFilterResponse> errorResponse =
                    new ApiResponseDto<>("PackageId is mandatory for topic recommendation", 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        TopicFilterResponse recommendResponse = topicService.recommendTopicsByPackage(packageId, topicFilterRequest);
        return ResponseEntity.ok(new ApiResponseDto<>("Recommended topics retrieved successfully", 200, recommendResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicFilterResponse>> recommendTopics(TopicFilterRequest topicFilterRequest) {
        log.info("Request received to recommend topics (unscoped) with criteria: {}", topicFilterRequest);
        TopicFilterResponse recommendResponse = topicService.recommendTopics(topicFilterRequest);
        return ResponseEntity.ok(new ApiResponseDto<>("Recommended topics retrieved successfully", 200, recommendResponse));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CourseTopicStatsItemDTO>>>> getCourseTopicStats(
            String userId, int offset, int pageSize) {

        log.info("Getting course topic stats for user: {} with offset: {}, pageSize: {}", userId, offset, pageSize);
        List<CourseTopicStatsItemDTO> items = clientUserOperationService.getCourseTopicStats(userId, offset, pageSize);
        long total = clientUserOperationService.countUserPackages(userId, null);

        AllResponseDto<List<CourseTopicStatsItemDTO>> response = new AllResponseDto<>(offset, pageSize, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>("Course topic statistics fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicDetailsResponseDTO>> getTopicDetails(String topicId, String userId, String subPackageId) {
        log.info("Getting topic details for topic: {}, user: {}, subpackage: {}", topicId, userId, subPackageId);
        TopicDetailsResponseDTO response = topicService.getTopicDetails(topicId, userId, subPackageId);
        return ResponseEntity.ok(new ApiResponseDto<>("Topic details fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageStatisticsItemDTO>>>> getUserSubPackageStatistics(
            int offset, int pageSize) {
        
        log.info("Getting user subpackage statistics with offset: {}, pageSize: {}", offset, pageSize);
        
        try {
            // Get current user ID from context
            String userId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved user ID from context: {}", userId);
            
            // Get subpackage statistics from service
            List<SubPackageStatisticsItemDTO> statistics = clientUserOperationService.getUserSubPackageStatistics(userId, offset, pageSize);
            
            // Get total count for pagination
            long total = clientUserOperationService.countUserSubPackages(userId);
            
            AllResponseDto<List<SubPackageStatisticsItemDTO>> response = new AllResponseDto<>(offset, pageSize, total, statistics);
            return ResponseEntity.ok(new ApiResponseDto<>("User subpackage statistics fetched successfully", 200, response));
            
        } catch (Exception e) {
            log.error("Error getting user subpackage statistics", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>("Unauthorized - User context not found", 401, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserDashboardTopicsProgressResponseDto>> getCurrentUserTopicsProgress(int offset, int pageSize, String search) {
        try {
            String userId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Fetching current user's topics progress, userId={}, offset={}, pageSize={}, search={}", userId, offset, pageSize, search);
            UserDashboardTopicsProgressResponseDto data = clientUserOperationService.getUserTopicsProgress(userId, offset, pageSize, search);
            return ResponseEntity.ok(new ApiResponseDto<>("Topics progress fetched successfully", 200, data));
        } catch (Exception e) {
            log.error("Failed to fetch current user topics progress", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>("Unauthorized - User context not found", 401, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>>> getTopicsByProductAndPackage(
            String productId, String packageId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        
        log.info("Request received to get topics by productId: {}, packageId: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                productId, packageId, search, offset, pageSize, sortBy, order);
        
        try {
            // Get paginated topics with filtering
            Page<TopicMinimalDto> topicPage = topicService.getTopicsByProductAndPackage(
                    productId, packageId, search, offset, pageSize, sortBy, order);
            
            AllResponseDto<List<TopicMinimalDto>> allResponseDto = new AllResponseDto<>(
                offset != null ? offset : 0,
                pageSize != null ? pageSize : 10,
                topicPage.getTotalElements(),
                topicPage.getContent()
            );
            
            ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>> response = 
                    new ApiResponseDto<>("Topics fetched successfully", 200, allResponseDto);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>> errorResponse = 
                    new ApiResponseDto<>(e.getMessage(), 400, null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error fetching topics by product and package", e);
            ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>> errorResponse = 
                    new ApiResponseDto<>("Internal server error", 500, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>>> getTopicsByProductPackages(
            TopicsByProductPackagesRequest request) {
        log.info("Request received to get topics by {} product/package pairs",
                request != null && request.getProductPackages() != null ? request.getProductPackages().size() : 0);

        try {
            TopicsByProductPackagesPageDto page = topicService.getTopicsByProductPackagePairs(request);

            AllResponseDto<List<TopicMinimalDto>> allResponseDto = new AllResponseDto<>(
                    page.getOffset(),
                    page.getPageSize(),
                    page.getTotal(),
                    page.getTotalLocked(),
                    page.getItems()
            );

            return ResponseEntity.ok(new ApiResponseDto<>("Topics fetched successfully", 200, allResponseDto));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error fetching topics by product/package pairs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Internal server error", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicCountsByProductPackagesResponseDto>> countTopicsByProductPackages(
            TopicCountsByProductPackagesRequest request) {
        log.info("Request received to count topics by product/package pairs (mspPairs={}, clientPairs={})",
                request != null && request.getMspProductPackages() != null
                        ? request.getMspProductPackages().size() : 0,
                request != null && request.getClientProductPackages() != null
                        ? request.getClientProductPackages().size() : 0);

        try {
            TopicCountsByProductPackagesResponseDto response =
                    topicService.countTopicsByProductPackages(request);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Topic distribution retrieved successfully", 200, response));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error counting topics by product/package pairs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Internal server error", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicDistributionResponseDto>> getTopicDistribution() {
        log.info("Request received to fetch topic distribution for 12 months");
        try {
            TopicDistributionResponseDto distribution = topicService.getTopicDistribution();
            ApiResponseDto<TopicDistributionResponseDto> response = 
                    new ApiResponseDto<>("Topic distribution fetched successfully", 200, distribution);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching topic distribution", e);
            ApiResponseDto<TopicDistributionResponseDto> errorResponse = 
                    new ApiResponseDto<>("Failed to fetch topic distribution", 500, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspTopicViewResponseDto>> getTopicForMsp(String topicId, String mspId) {
        try {
            String resolvedMspId = resolveMspId(mspId);
            log.info("Request received to fetch MSP topic view for topicId: {}, mspId: {}", topicId, resolvedMspId);
            MspTopicViewResponseDto topic = topicService.getTopicForMsp(topicId, resolvedMspId);
            return ResponseEntity.ok(new ApiResponseDto<>("Topic fetched successfully for MSP", 200, topic));
        } catch (IllegalArgumentException e) {
            log.error("Invalid MSP topic view request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (com.aspire.asat.cms.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error fetching MSP topic view for topicId: {}", topicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch topic for MSP", 500, null));
        }
    }

    /**
     * For MSP callers, use context userId (MSP document id used in msp_products.mspId).
     * For other callers, use the mspId query param.
     */
    private String resolveMspId(String requestMspId) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            return userContext.getUserId();
        }

        if (!StringUtils.hasText(requestMspId)) {
            throw new IllegalArgumentException("mspId is required");
        }
        return requestMspId.trim();
    }

    /**
     * Create / update / delete are restricted to platform admins.
     * Other authenticated users may only list and get topics by id.
     */
    private void assertPlatformAdminCanModifyTopics() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UserType userType;
        try {
            userType = UserType.fromString(context.getUserType());
        } catch (Exception e) {
            userType = null;
        }
        if (userType != UserType.ASPIRE_ADMIN
                && userType != UserType.SUPER_ADMIN
                && userType != UserType.SYSTEM_USER) {
            log.warn("User {} with type {} is not allowed to create/update/delete topics",
                    context.getUserId(), context.getUserType());
            throw new CmsServiceException(
                    "Only ASPIRE_ADMIN, SUPER_ADMIN, or SYSTEM_USER can create, update, or delete topics",
                    HttpStatus.FORBIDDEN
            );
        }
    }

}
