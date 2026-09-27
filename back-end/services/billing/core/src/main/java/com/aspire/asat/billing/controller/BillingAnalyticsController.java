package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.analytics.*;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.ANALYTICS_API, produces = "application/json")
@Tag(name = "Billing Analytics", description = "Endpoints for Billing Analytics & Reports")
public interface BillingAnalyticsController {

    @Operation(summary = "Get complete analytics data", description = "Returns complete billing analytics including summary, trends, and breakdowns")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<BillingAnalyticsResponseDTO>> getAnalytics(
            @RequestParam(defaultValue = "MONTHLY") AnalyticsPeriod period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Get analytics summary", description = "Returns summary cards data only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<BillingAnalyticsSummaryDTO>> getSummary(
            @RequestParam(defaultValue = "MONTHLY") AnalyticsPeriod period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Get revenue trend", description = "Returns revenue trend data grouped by period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revenue trend retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/revenue-trend")
    ResponseEntity<ApiResponseDto<List<RevenueByPeriodDTO>>> getRevenueTrend(
            @RequestParam(defaultValue = "MONTHLY") AnalyticsPeriod period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Get top performing packages", description = "Returns top packages by revenue and subscription count")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top packages retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/top-packages")
    ResponseEntity<ApiResponseDto<List<TopPackageDTO>>> getTopPackages(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId,
            @RequestParam(defaultValue = "5") int limit
    );

    @Operation(summary = "Get failed payments analysis", description = "Returns breakdown of failed payments by reason")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Failed payments analysis retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/failed-payments")
    ResponseEntity<ApiResponseDto<List<FailedPaymentAnalysisDTO>>> getFailedPaymentsAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Get payment success rate", description = "Returns payment success rate metrics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success rate retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/success-rate")
    ResponseEntity<ApiResponseDto<PaymentSuccessRateDTO>> getPaymentSuccessRate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Export analytics as CSV", description = "Generates and returns analytics data as CSV file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/export/csv")
    ResponseEntity<org.springframework.core.io.Resource> exportAnalyticsCsv(
            @RequestParam(defaultValue = "MONTHLY") AnalyticsPeriod period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );

    @Operation(summary = "Export analytics as PDF", description = "Generates and returns analytics report as PDF file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate PDF")
    })
    @GetMapping("/export/pdf")
    ResponseEntity<org.springframework.core.io.Resource> exportAnalyticsPdf(
            @RequestParam(defaultValue = "MONTHLY") AnalyticsPeriod period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String countryId
    );
}

