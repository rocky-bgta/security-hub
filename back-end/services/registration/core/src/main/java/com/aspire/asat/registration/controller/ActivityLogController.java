package com.aspire.asat.registration.controller;

import com.aspire.asat.common.dto.activitylog.ActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.constant.WebApiUrlConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Activity Log", description = "Endpoints for managing activity logs")
@RequestMapping(value = WebApiUrlConstants.ACTIVITY_LOG_API, produces = "application/json")
public interface ActivityLogController {

    @Operation(summary = "Get client admin activity logs", description = "Retrieves paginated activity logs for a client admin with filtering and sorting options")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/client-admin")
    ResponseEntity<ApiResponseDto<ClientAdminActivityLogResponseDto>> getClientAdminActivityLogs(
            @Valid @RequestBody ClientAdminActivityLogRequestDto requestDto);

    @Operation(summary = "Get client admin activity log by ID", description = "Retrieves a single activity log entry by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity log retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Activity log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{activityId}")
    ResponseEntity<ApiResponseDto<ClientAdminActivityLogDto>> getClientAdminActivityLogById(
            @PathVariable("activityId") @NotBlank String activityId);

    @Operation(summary = "Get activity logs with role-based access control", 
               description = "Retrieves paginated activity logs based on user role. " +
                             "Aspire Admin sees MSP, CLIENT_ADMIN, USER. " +
                             "MSP Admin sees CLIENT_ADMIN, USER. " +
                             "Client Admin sees USER only. " +
                             "User sees only their own logs. " +
                             "Supports filtering by countryId, aspireAdminId, mspId, clientAdminId, user, userId+userType, activityType, activityStatus, date range, and search.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/list")
    ResponseEntity<ApiResponseDto<ActivityLogResponseDto>> getActivityLogs(
            @Valid @RequestBody ActivityLogRequestDto requestDto);

    @Operation(summary = "Create activity log", description = "Creates a new activity log entry with all provided fields except id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity log created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ActivityLogDto>> createActivityLog(
            @Valid @RequestBody CreateActivityLogDto requestDto);
}

