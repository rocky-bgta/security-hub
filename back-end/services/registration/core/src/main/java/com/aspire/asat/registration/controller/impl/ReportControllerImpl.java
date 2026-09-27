package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.ReportController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;
import com.aspire.asat.registration.service.ReportService;
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

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportControllerImpl implements ReportController {

    private final ReportService reportService;

    @Override
    public ResponseEntity<ApiResponseDto<UserSummaryReportDTO>> getUserSummaryReport(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String userType,
            String country,
            LocalDate fromDate,
            LocalDate toDate,
            int offset,
            int pageSize,
            int trendMonths) {
        try {
            validateDateRange(fromDate, toDate);
            UserSummaryReportDTO report = reportService.getUserSummaryReport(
                    clientAdminId, mspId, search, status, userType, country,
                    toStartOfDayUtc(fromDate), toStartOfDayUtcExclusive(toDate),
                    offset, pageSize, trendMonths);
            return ResponseEntity.ok(new ApiResponseDto<>("User summary report generated successfully", 200, report));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user summary report request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating user summary report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to generate user summary report: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<Resource> exportUserSummaryReport(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String userType,
            String country,
            LocalDate fromDate,
            LocalDate toDate) {
        try {
            validateDateRange(fromDate, toDate);
            byte[] data = reportService.exportUserDetailsCsv(
                    clientAdminId, mspId, search, status, userType, country,
                    toStartOfDayUtc(fromDate), toStartOfDayUtcExclusive(toDate));
            String filename = "user-summary-" + System.currentTimeMillis() + ".csv";
            Resource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user summary export request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error exporting user summary report", e);
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
