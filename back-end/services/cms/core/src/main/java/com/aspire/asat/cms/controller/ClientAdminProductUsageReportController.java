package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientAdminProductUsageReport.ClientAdminProductUsageReportResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Client Admin Product Usage Report", description = "APIs for client admin product usage reporting")
@RequestMapping(value = WebApiUrlConstants.CLIENT_ADMIN_PRODUCT_USAGE_REPORT_API)
public interface ClientAdminProductUsageReportController {

    @GetMapping(produces = "application/json")
    @Operation(summary = "Get client admin product usage report",
            description = "Returns product usage summary for a client admin including products, active modules (topics), "
                    + "license interaction totals, average engagement, and per-product utilization. "
                    + "Uses clientAdminId query parameter when provided; otherwise resolves from current context for CLIENT_ADMIN users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client admin product usage report retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing client admin ID"),
            @ApiResponse(responseCode = "404", description = "Client admin data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<ClientAdminProductUsageReportResponseDto>> getClientAdminProductUsageReport(
            @Parameter(description = "Client admin ID (optional for CLIENT_ADMIN users)")
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId
    );

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_EXPORT, produces = "text/csv")
    @Operation(summary = "Export client admin product usage report products as CSV",
            description = "Downloads the products list from the product usage report as a CSV file. "
                    + "Uses the same clientAdminId resolution rules as the report endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file downloaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing client admin ID"),
            @ApiResponse(responseCode = "404", description = "Client admin data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<Resource> exportClientAdminProductUsageReportCsv(
            @Parameter(description = "Client admin ID (optional for CLIENT_ADMIN users)")
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId
    );
}
