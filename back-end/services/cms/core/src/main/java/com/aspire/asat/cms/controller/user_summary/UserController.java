package com.aspire.asat.cms.controller.user_summary;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.CompletedTopicResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.DashboardSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserPackageGroupedResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageListResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageStatisticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dashboard APIs", description = "Endpoints for user dashboard and user-related operations")
@RequestMapping(value = WebApiUrlConstants.CLIENT_API, produces = "application/json")
public interface UserController {

    @Operation(summary = "Get dashboard summary", description = "Returns counts of courses (completed, in-progress, pending) and earned certificates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard summary fetched"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard/summary")
    ResponseEntity<ApiResponseDto<DashboardSummaryResponseDTO>> getDashboardSummary(@RequestParam @NotBlank String userId);

    @Operation(summary = "Get grouped user packages by status", description = "Returns user's packages grouped by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grouped package list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user-packages/status-grouped")
    ResponseEntity<ApiResponseDto<UserPackageGroupedResponseDTO>> getUserPackagesGroupedByStatus(
            @RequestParam @NotBlank String userId);

    @Operation(summary = "Get user subpackage statistics count", description = "Returns counts of current user's subpackages by status (total, completed, exam, inProgress, notStarted)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subpackage statistics count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User context not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/subpackages/statistics-count")
    ResponseEntity<ApiResponseDto<UserSubPackageStatisticsDTO>> getUserSubPackageStatisticsCount();

    @Operation(summary = "Get completed topics list", description = "Returns paginated list of completed topics with completion dates for the current user, sorted by most recent completion first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Completed topics list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User context not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/topics/completed")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CompletedTopicResponseDTO>>>> getCompletedTopics(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get user subpackage list", description = "Returns list of subpackage IDs and names assigned to the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subpackage list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User context not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/subpackages/list")
    ResponseEntity<ApiResponseDto<List<UserSubPackageListResponseDTO>>> getUserSubPackageList();
}
