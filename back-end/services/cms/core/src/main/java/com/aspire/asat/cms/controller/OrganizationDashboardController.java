package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.dashboard.ProductTopicSalesProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TimeFrame;
import com.aspire.asat.cms.dto.organizationDashboard.OrganizationDashboardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Organization Dashboard Management", description = "APIs for managing organization dashboard data")
@RequestMapping(value = WebApiUrlConstants.ORGANIZATION_DASHBOARD_API, produces = "application/json")
public interface OrganizationDashboardController {

    @GetMapping
    @Operation(summary = "Get consolidated organization dashboard", 
               description = "Retrieves consolidated organization dashboard data by summing totalProduct, totalPackage, totalLicense, totalClient, and totalMsp across all organization dashboard records.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization dashboard retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<OrganizationDashboardResponseDto>> getOrganizationDashboard();

    @GetMapping("/sales-progress")
    @Operation(summary = "Get sales progress by product-topics", 
               description = "Returns active topic counts per product. " +
                       "YEARLY: year-wise data for previous 6 years (including current year). " +
                       "MONTHLY: month-wise data for current year (Jan-Dec).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales progress data retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<ProductTopicSalesProgressResponseDto>> getProductTopicSalesProgress(
            @RequestParam(value = "timeFrame", defaultValue = "MONTHLY") TimeFrame timeFrame);

    @GetMapping("/msp-sales-progress")
    @Operation(summary = "Get MSP sales progress by product-topics",
               description = "Returns active topic counts per product scoped to an MSP's clients. "
                       + "Resolves client admins by mspId, then unique products from client_product_replica. "
                       + "If mspId is omitted, uses current user context userId. "
                       + "YEARLY: year-wise data for previous 6 years (including current year). "
                       + "MONTHLY: month-wise data for current year (Jan-Dec).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP sales progress data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "mspId missing and not available in context"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<ProductTopicSalesProgressResponseDto>> getMspProductTopicSalesProgress(
            @RequestParam(value = "timeFrame", defaultValue = "MONTHLY") TimeFrame timeFrame,
            @RequestParam(value = "mspId", required = false) String mspId);
}

