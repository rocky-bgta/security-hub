package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.SubscriptionSummaryReportController;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.QuickRange;
import com.aspire.asat.registration.data.reports.SubscriptionDetailRowDTO;
import com.aspire.asat.registration.data.reports.SubscriptionSummaryTotalsDTO;
import com.aspire.asat.registration.service.SubscriptionSummaryReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionSummaryReportControllerImpl implements SubscriptionSummaryReportController {

    private final SubscriptionSummaryReportService subscriptionSummaryReportService;

    @Override
    public ResponseEntity<ApiResponseDto<SubscriptionSummaryTotalsDTO>> getSubscriptionSummary(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            QuickRange quickRange) {
        try {
            validateDateRange(fromDate, toDate);
            SubscriptionSummaryTotalsDTO summary = subscriptionSummaryReportService.getSummary(
                    search, clientAdminId, mspId, status,
                    toStartOfDayUtc(fromDate), toStartOfDayUtcExclusive(toDate), quickRange);
            return ResponseEntity.ok(new ApiResponseDto<>("Subscription summary generated successfully", 200, summary));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription summary request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating subscription summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to generate subscription summary: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SubscriptionDetailRowDTO>>>> getSubscriptionDetailList(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            QuickRange quickRange,
            int offset,
            int pageSize) {
        try {
            validateDateRange(fromDate, toDate);
            AllResponseDto<List<SubscriptionDetailRowDTO>> list = subscriptionSummaryReportService.getDetailList(
                    search, clientAdminId, mspId, status,
                    toStartOfDayUtc(fromDate), toStartOfDayUtcExclusive(toDate), quickRange,
                    offset, pageSize);
            return ResponseEntity.ok(new ApiResponseDto<>("Subscription detail list generated successfully", 200, list));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription detail list request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating subscription detail list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to generate subscription detail list: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<Resource> exportSubscriptionSummaryReport(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            QuickRange quickRange) {
        try {
            validateDateRange(fromDate, toDate);
            byte[] data = subscriptionSummaryReportService.exportCsv(
                    search, clientAdminId, mspId, status,
                    toStartOfDayUtc(fromDate), toStartOfDayUtcExclusive(toDate), quickRange);
            String filename = "subscription-summary-" + System.currentTimeMillis() + ".csv";
            Resource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription summary export request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error exporting subscription summary report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be on or after fromDate");
        }
    }

    private static Instant toStartOfDayUtc(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private static Instant toStartOfDayUtcExclusive(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
