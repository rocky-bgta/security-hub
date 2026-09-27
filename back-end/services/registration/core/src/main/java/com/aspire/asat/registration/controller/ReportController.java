package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@RequestMapping(WebApiUrlConstants.USER_SUMMARY_REPORT_API)
@Tag(name = "User Summary Report",
        description = "High-level overview of users (totals, growth trend, detail rows) with optional client/MSP scoping and CSV export")
public interface ReportController {

    @Operation(
            summary = "Get user summary report",
            description = "Returns the four summary cards (Total, Active, Suspended, New Sign-ups in last 30 days), a monthly cumulative growth trend and a paginated user details table. " +
                    "When both clientAdminId and mspId are omitted the report is system-wide; when either is supplied the data is scoped to that organisation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User summary report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<UserSummaryReportDTO>> getUserSummaryReport(
            @Parameter(description = "Client Admin ID to scope the report (optional)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the report (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Search term applied to email/username")
            @RequestParam(required = false) String search,
            @Parameter(description = "User status filter (e.g. ACTIVE, INACTIVE, SUSPEND)")
            @RequestParam(required = false) String status,
            @Parameter(description = "User type filter (e.g. USER, CLIENT_ADMIN, MSP, ADMIN)")
            @RequestParam(required = false) String userType,
            @Parameter(description = "Country filter")
            @RequestParam(required = false) String country,
            @Parameter(description = "Include only users whose createdAt is on or after this date (yyyy-MM-dd, inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Include only users whose createdAt is on or before this date (yyyy-MM-dd, inclusive)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Page offset for the details table", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size for the details table", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "Number of months to include in the growth trend", example = "6")
            @RequestParam(defaultValue = "6") int trendMonths
    );

    @Operation(
            summary = "Export user details as CSV",
            description = "Exports the User Details rows (one row per matching user) using the same filters as the report endpoint. Returns a UTF-8 CSV file with a BOM so it renders correctly in Excel."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/export")
    ResponseEntity<Resource> exportUserSummaryReport(
            @Parameter(description = "Client Admin ID to scope the export (optional)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the export (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Search term applied to email/username")
            @RequestParam(required = false) String search,
            @Parameter(description = "User status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "User type filter")
            @RequestParam(required = false) String userType,
            @Parameter(description = "Country filter")
            @RequestParam(required = false) String country,
            @Parameter(description = "Include only users whose createdAt is on or after this date (yyyy-MM-dd, inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Include only users whose createdAt is on or before this date (yyyy-MM-dd, inclusive)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    );
}
