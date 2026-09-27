package com.aspire.asat.billing.controller.impl;

import com.aspire.asat.billing.controller.BillingAnalyticsController;
import com.aspire.asat.billing.dto.analytics.*;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.service.BillingAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BillingAnalyticsControllerImpl implements BillingAnalyticsController {

    private final BillingAnalyticsService analyticsService;

    @Override
    public ResponseEntity<ApiResponseDto<BillingAnalyticsResponseDTO>> getAnalytics(
            AnalyticsPeriod period, String startDate, String endDate, String mspId, String countryId) {
        log.info("Getting billing analytics - period: {}, startDate: {}, endDate: {}", period, startDate, endDate);
        
        BillingAnalyticsResponseDTO analytics = analyticsService.getAnalytics(period, startDate, endDate, mspId, countryId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Analytics retrieved successfully",
                HttpStatus.OK.value(),
                analytics
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BillingAnalyticsSummaryDTO>> getSummary(
            AnalyticsPeriod period, String startDate, String endDate, String mspId, String countryId) {
        log.info("Getting analytics summary - period: {}", period);
        
        BillingAnalyticsSummaryDTO summary = analyticsService.getSummary(period, startDate, endDate, mspId, countryId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Summary retrieved successfully",
                HttpStatus.OK.value(),
                summary
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<RevenueByPeriodDTO>>> getRevenueTrend(
            AnalyticsPeriod period, String startDate, String endDate, String mspId, String countryId) {
        log.info("Getting revenue trend - period: {}", period);
        
        List<RevenueByPeriodDTO> trend = analyticsService.getRevenueTrend(period, startDate, endDate, mspId, countryId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Revenue trend retrieved successfully",
                HttpStatus.OK.value(),
                trend
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<TopPackageDTO>>> getTopPackages(
            String startDate, String endDate, String mspId, String countryId, int limit) {
        log.info("Getting top packages - limit: {}", limit);
        
        List<TopPackageDTO> topPackages = analyticsService.getTopPackages(startDate, endDate, mspId, countryId, limit);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Top packages retrieved successfully",
                HttpStatus.OK.value(),
                topPackages
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<FailedPaymentAnalysisDTO>>> getFailedPaymentsAnalysis(
            String startDate, String endDate, String mspId, String countryId) {
        log.info("Getting failed payments analysis");
        
        List<FailedPaymentAnalysisDTO> analysis = analyticsService.getFailedPaymentsAnalysis(startDate, endDate, mspId, countryId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Failed payments analysis retrieved successfully",
                HttpStatus.OK.value(),
                analysis
        ));
    }

    @Override
    public ResponseEntity<ApiResponseDto<PaymentSuccessRateDTO>> getPaymentSuccessRate(
            String startDate, String endDate, String mspId, String countryId) {
        log.info("Getting payment success rate");
        
        PaymentSuccessRateDTO successRate = analyticsService.getPaymentSuccessRate(startDate, endDate, mspId, countryId);
        
        return ResponseEntity.ok(new ApiResponseDto<>(
                "Payment success rate retrieved successfully",
                HttpStatus.OK.value(),
                successRate
        ));
    }

    @Override
    public ResponseEntity<Resource> exportAnalyticsCsv(
            AnalyticsPeriod period, String startDate, String endDate, String mspId, String countryId) {
        log.info("Exporting analytics as CSV - period: {}", period);
        
        byte[] csvData = analyticsService.exportAnalyticsCsv(period, startDate, endDate, mspId, countryId);
        ByteArrayResource resource = new ByteArrayResource(csvData);

        String filename = "billing-analytics-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvData.length)
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> exportAnalyticsPdf(
            AnalyticsPeriod period, String startDate, String endDate, String mspId, String countryId) {
        log.info("Exporting analytics as PDF - period: {}", period);
        
        byte[] pdfData = analyticsService.exportAnalyticsPdf(period, startDate, endDate, mspId, countryId);
        ByteArrayResource resource = new ByteArrayResource(pdfData);

        String filename = "billing-analytics-report-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(resource);
    }
}

