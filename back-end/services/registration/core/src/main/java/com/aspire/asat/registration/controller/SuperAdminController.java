package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.LicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.MspLicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.constant.WebApiUrlConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Super Admin", description = "Endpoints for super admin operations")
@RequestMapping(value = WebApiUrlConstants.SUPER_ADMIN_API, produces = "application/json")
public interface SuperAdminController {

    @Operation(
            summary = "Get license history for all client admins",
            description = "Retrieves paginated license history for all client admins with optional filtering by clientAdminId, productId, packageId, countryId, mspId, and search by organization name. Product and package names are fetched from CMS service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/license-history")
    ResponseEntity<ApiResponseDto<LicenseHistoryPaginatedResponseDto>> getLicenseHistory(
            @Parameter(description = "Filter by client admin ID", required = false)
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,

            @Parameter(description = "Filter by product ID", required = false)
            @RequestParam(value = "productId", required = false) String productId,

            @Parameter(description = "Filter by package ID", required = false)
            @RequestParam(value = "packageId", required = false) String packageId,

            @Parameter(description = "Filter by country (matches ClientAdmin country or countryCode)", required = false)
            @RequestParam(value = "countryId", required = false) String countryId,

            @Parameter(description = "Filter by MSP ID", required = false)
            @RequestParam(value = "mspId", required = false) String mspId,

            @Parameter(description = "Search by client admin organization name", required = false)
            @RequestParam(value = "search", required = false) String search,

            @Parameter(description = "Page offset", required = false)
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,

            @Parameter(description = "Page size", required = false)
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,

            @Parameter(description = "Sort field", required = false)
            @RequestParam(value = "sortBy", required = false, defaultValue = "assignedAt") String sortBy,

            @Parameter(description = "Sort order (asc/desc)", required = false)
            @RequestParam(value = "order", required = false, defaultValue = "desc") String order
    );

    @Operation(
            summary = "Get license history for all MSPs",
            description = "Retrieves paginated license history for all MSPs with optional filtering by mspId, productId, and packageId. Product and package names are fetched from CMS service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP license history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/msp-license-history")
    ResponseEntity<ApiResponseDto<MspLicenseHistoryPaginatedResponseDto>> getMspLicenseHistory(
            @Parameter(description = "Filter by MSP ID", required = false)
            @RequestParam(value = "mspId", required = false) String mspId,

            @Parameter(description = "Filter by product ID", required = false)
            @RequestParam(value = "productId", required = false) String productId,

            @Parameter(description = "Filter by package ID", required = false)
            @RequestParam(value = "packageId", required = false) String packageId,

            @Parameter(description = "Filter by country", required = false)
            @RequestParam(value = "countryId", required = false) String countryId,

            @Parameter(description = "Search by MSP name or email", required = false)
            @RequestParam(value = "search", required = false) String search,

            @Parameter(description = "Page offset", required = false)
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,

            @Parameter(description = "Page size", required = false)
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,

            @Parameter(description = "Sort field", required = false)
            @RequestParam(value = "sortBy", required = false, defaultValue = "assignedAt") String sortBy,

            @Parameter(description = "Sort order (asc/desc)", required = false)
            @RequestParam(value = "order", required = false, defaultValue = "desc") String order
    );
}

