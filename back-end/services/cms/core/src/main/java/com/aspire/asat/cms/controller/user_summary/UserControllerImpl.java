package com.aspire.asat.cms.controller.user_summary;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.CompletedTopicResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.DashboardSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserPackageGroupedResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageListResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageStatisticsDTO;
import com.aspire.asat.cms.service.user_operations.UserSummaryService;
import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class UserControllerImpl implements UserController {

    private final UserSummaryService userSummaryService;
    private final ClientUserOperationService clientUserOperationService;
    private final UserCurrentContextService userCurrentContextService;

    @Autowired
    public UserControllerImpl(UserSummaryService userSummaryService, 
                             ClientUserOperationService clientUserOperationService,
                             UserCurrentContextService userCurrentContextService) {
        this.userSummaryService = userSummaryService;
        this.clientUserOperationService = clientUserOperationService;
        this.userCurrentContextService = userCurrentContextService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<DashboardSummaryResponseDTO>> getDashboardSummary(String userId) {
        DashboardSummaryResponseDTO response = userSummaryService.getDashboardSummary(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("Dashboard summary fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserPackageGroupedResponseDTO>> getUserPackagesGroupedByStatus(String userId) {
        UserPackageGroupedResponseDTO response = userSummaryService.getGroupedUserPackages(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("Fetched grouped packages", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserSubPackageStatisticsDTO>> getUserSubPackageStatisticsCount() {
        log.info("Received request for current user subpackage statistics count");
        
        try {
            // Get current user ID from context
            String userId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved user ID from context: {}", userId);
            
            UserSubPackageStatisticsDTO response = clientUserOperationService.getUserSubPackageStatisticsCount(userId);
            return ResponseEntity.ok(new ApiResponseDto<>("Subpackage statistics count retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting current user subpackage statistics count", e);
            return ResponseEntity.status(401)
                    .body(new ApiResponseDto<>("Unauthorized - User context not found", 401, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CompletedTopicResponseDTO>>>> getCompletedTopics(int offset, int pageSize) {
        log.info("Received request for completed topics with offset: {}, pageSize: {}", offset, pageSize);
        
        try {
            // Get current user ID from context
            String userId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved user ID from context: {}", userId);
            
            // Get completed topics and total count
            List<CompletedTopicResponseDTO> items = clientUserOperationService.getCompletedTopics(userId, offset, pageSize);
            long total = clientUserOperationService.countCompletedTopics(userId);
            
            // Create paginated response
            AllResponseDto<List<CompletedTopicResponseDTO>> paginatedResponse = new AllResponseDto<>(
                offset, pageSize, total, items
            );
            
            return ResponseEntity.ok(new ApiResponseDto<>("Completed topics retrieved successfully", 200, paginatedResponse));
        } catch (Exception e) {
            log.error("Error getting completed topics", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>("Unauthorized - User context not found", 401, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserSubPackageListResponseDTO>>> getUserSubPackageList() {
        log.info("Received request for current user subpackage list");
        
        try {
            // Get current user ID from context
            String userId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved user ID from context: {}", userId);
            
            List<UserSubPackageListResponseDTO> response = clientUserOperationService.getUserSubPackageList(userId);
            return ResponseEntity.ok(new ApiResponseDto<>("Subpackage list retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting current user subpackage list", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>("Unauthorized - User context not found", 401, null));
        }
    }
}
