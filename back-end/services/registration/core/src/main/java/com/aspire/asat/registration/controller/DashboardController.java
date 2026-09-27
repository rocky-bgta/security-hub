package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.dashboard.LicenseDistributionResponseDto;
import com.aspire.asat.registration.data.dashboard.UserStatusCountResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller interface for dashboard-related endpoints
 */
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and analytics")
@RequestMapping(value = WebApiUrlConstants.DASHBOARD, produces = "application/json")
public interface DashboardController {

    @Operation(summary = "Get user status counts",
               description = "Returns total counts of Active, Inactive, and Suspended users from AspireUser. "
                       + "When mspId is provided (or when the caller is an MSP and mspId is omitted), "
                       + "counts are limited to users under that MSP's client admins.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status counts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user-status-counts")
    ResponseEntity<ApiResponseDto<UserStatusCountResponseDto>> getUserStatusCounts(
            @Parameter(description = "MSP ID (optional). For MSP callers, defaults to current context userId.")
            @RequestParam(value = "mspId", required = false) String mspId
    );

    @Operation(summary = "Get license distribution",
               description = "Returns totalAvailable, totalAllocated, totalActive, and totalExpired from msp_products. "
                       + "When mspId is provided (or when the caller is an MSP and mspId is omitted), "
                       + "counts are limited to that MSP. Aspire Admin / System User without mspId get platform-wide totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License distribution retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/license-distribution")
    ResponseEntity<ApiResponseDto<LicenseDistributionResponseDto>> getLicenseDistribution(
            @Parameter(description = "MSP ID (optional). For MSP callers, defaults to current context userId.")
            @RequestParam(value = "mspId", required = false) String mspId
    );
}
