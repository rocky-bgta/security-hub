package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspDashboardResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspTopicCountsResponseDto;
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
 * Controller for MSP dashboard summary statistics.
 */
@Tag(name = "MSP Dashboard", description = "APIs for MSP dashboard totals")
@RequestMapping(value = WebApiUrlConstants.MSP_DASHBOARD, produces = "application/json")
public interface MspDashboardController {

    @GetMapping
    @Operation(
            summary = "Get MSP dashboard totals",
            description = "Returns total products, licenses, packages, and clients for the given MSP. "
                    + "If mspId is not provided, it is taken from the current user context userId. "
                    + "Products, packages, and licenses are aggregated from msp_products; "
                    + "clients are counted from client_admins."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP dashboard totals retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "mspId missing and not available in context"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<MspDashboardResponseDto>> getMspDashboard(
            @Parameter(description = "MSP ID (optional; defaults to current user context userId)", example = "msp-123")
            @RequestParam(value = "mspId", required = false) String mspId
    );

    @GetMapping("/topic-counts")
    @Operation(
            summary = "Get MSP topic monthly distribution",
            description = "Returns 12 months for the current year (same shape as CMS /topics/distribution). "
                    + "totalContent = topics matching msp_products pairs; "
                    + "usedContent = topics matching client_products pairs. "
                    + "If mspId is not provided, it is taken from the current user context userId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP topic distribution retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "mspId missing and not available in context"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<MspTopicCountsResponseDto>> getMspTopicCounts(
            @Parameter(description = "MSP ID (optional; defaults to current user context userId)", example = "msp-123")
            @RequestParam(value = "mspId", required = false) String mspId
    );
}
