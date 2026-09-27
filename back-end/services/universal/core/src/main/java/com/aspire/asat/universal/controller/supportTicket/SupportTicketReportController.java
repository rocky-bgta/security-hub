package com.aspire.asat.universal.controller.supportTicket;

import com.aspire.asat.universal.constant.WebApiUrlConstants;
import com.aspire.asat.universal.supportTicket.response.SupportTicketRecentRowDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketReportSummaryDto;
import com.aspire.asat.universal.supportTicket.response.SupportTicketStatusDistributionDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Support Ticket Report APIs", description = "Endpoints for support ticket dashboard metrics, list, and export")
@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT + "/support-ticket-reports", produces = "application/json")
public interface SupportTicketReportController {

    @Operation(
            summary = "Get support ticket summary metrics",
            description = "Returns total ticket count, open count, closed count, and high-priority count for the selected date range and filters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary metrics fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<SupportTicketReportSummaryDto>> getOpenVsClosedSummary(
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T00:00:00Z)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T23:59:59Z)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer thresholdDays);

    @Operation(
            summary = "Get support ticket status distribution",
            description = "Returns ticket counts grouped by status (OPEN, IN_PROGRESS, CLOSED) for the selected date range and filters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status distribution fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status-distribution")
    ResponseEntity<ApiResponseDto<List<SupportTicketStatusDistributionDto>>> getOpenVsClosedStatusDistribution(
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T00:00:00Z)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T23:59:59Z)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer thresholdDays);

    @Operation(
            summary = "Get support ticket recent tickets",
            description = "Returns paginated recent tickets for the dashboard table, filtered by date range and search."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent tickets fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recent-tickets")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SupportTicketRecentRowDto>>>> getOpenVsClosedRecentTickets(
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T00:00:00Z)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T23:59:59Z)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer thresholdDays,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(
            summary = "Export support ticket report",
            description = "Exports filtered support ticket report rows as a downloadable CSV file."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report exported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/export")
    void exportOpenVsClosedReport(
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T00:00:00Z)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd (e.g., 2026-06-26), yyyy/MM/dd, or ISO-8601 (e.g., 2026-04-30T23:59:59Z)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer thresholdDays,
            HttpServletResponse response);
}
