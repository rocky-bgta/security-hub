package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.QuickRange;
import com.aspire.asat.registration.data.reports.SubscriptionDetailRowDTO;
import com.aspire.asat.registration.data.reports.SubscriptionSummaryTotalsDTO;
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
import java.util.List;

@RequestMapping(WebApiUrlConstants.SUBSCRIPTION_SUMMARY_REPORT_API)
@Tag(name = "Subscription Summary Report",
        description = "License/subscription overview (summary counts, paginated detail rows and CSV export) over client products, with optional client/MSP scoping, status, date range and quick-range filters")
public interface SubscriptionSummaryReportController {

    @Operation(
            summary = "Get subscription summary counts",
            description = "Returns aggregate license counts (total products, total packages, total licenses, active/used, unused, pending and expired licenses) over the filtered subscription set. " +
                    "CLIENT_ADMIN callers are auto-scoped to their own organisation and MSP callers to their own MSP; system users may pass clientAdminId/mspId. " +
                    "When quickRange is supplied it overrides fromDate/toDate, which otherwise filter on the subscription start date (assignedAt)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription summary generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<SubscriptionSummaryTotalsDTO>> getSubscriptionSummary(
            @Parameter(description = "Search term applied to client / product / package name (case-insensitive)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Client Admin ID to scope the report (optional)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the report (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Subscription status filter (ACTIVE, PENDING, EXPIRED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Include only subscriptions assigned on or after this date (yyyy-MM-dd, inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Include only subscriptions assigned on or before this date (yyyy-MM-dd, inclusive)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Relative date window; overrides fromDate/toDate when supplied", example = "LAST_30_DAYS")
            @RequestParam(required = false) QuickRange quickRange
    );

    @Operation(
            summary = "Get paginated subscription detail list",
            description = "Returns a paginated list of subscription detail rows (client, product, package, start/end date, status, total/used license) for the same filters as the summary endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription detail list generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SubscriptionDetailRowDTO>>>> getSubscriptionDetailList(
            @Parameter(description = "Search term applied to client / product / package name (case-insensitive)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Client Admin ID to scope the report (optional)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the report (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Subscription status filter (ACTIVE, PENDING, EXPIRED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Include only subscriptions assigned on or after this date (yyyy-MM-dd, inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Include only subscriptions assigned on or before this date (yyyy-MM-dd, inclusive)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Relative date window; overrides fromDate/toDate when supplied", example = "LAST_30_DAYS")
            @RequestParam(required = false) QuickRange quickRange,
            @Parameter(description = "Page offset (page index) for the detail table", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size for the detail table (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int pageSize
    );

    @Operation(
            summary = "Export subscription details as CSV",
            description = "Exports the subscription detail rows (one row per matching subscription) using the same filters as the summary/list endpoints. Returns a UTF-8 CSV file with a BOM so it renders correctly in Excel."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/export")
    ResponseEntity<Resource> exportSubscriptionSummaryReport(
            @Parameter(description = "Search term applied to client / product / package name (case-insensitive)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Client Admin ID to scope the export (optional)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the export (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Subscription status filter (ACTIVE, PENDING, EXPIRED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Include only subscriptions assigned on or after this date (yyyy-MM-dd, inclusive)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Include only subscriptions assigned on or before this date (yyyy-MM-dd, inclusive)", example = "2026-03-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Relative date window; overrides fromDate/toDate when supplied", example = "LAST_30_DAYS")
            @RequestParam(required = false) QuickRange quickRange
    );
}
