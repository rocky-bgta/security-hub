package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.ProductLicenseOnboardingReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(WebApiUrlConstants.PRODUCT_LICENSE_ONBOARDING_REPORT_API)
@Tag(name = "Product License Onboarding Report",
        description = "Client Admin product license and user onboarding metrics")
public interface ProductLicenseOnboardingReportController {

    @Operation(
            summary = "Get product license user onboarding report",
            description = "Returns license usage and onboarding funnel metrics for a Client Admin. "
                    + "When the caller is CLIENT_ADMIN, clientAdminId may be omitted and the current user id is used."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing clientAdminId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<ProductLicenseOnboardingReportDto>> getProductLicenseOnboardingReport(
            @Parameter(description = "Client Admin ID (optional when caller is CLIENT_ADMIN)")
            @RequestParam(required = false) String clientAdminId
    );
}
