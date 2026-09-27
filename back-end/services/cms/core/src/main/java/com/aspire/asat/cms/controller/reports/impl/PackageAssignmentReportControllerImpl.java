package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.controller.reports.PackageAssignmentReportController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import com.aspire.asat.cms.service.reports.PackageAssignmentReportService;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PackageAssignmentReportControllerImpl implements PackageAssignmentReportController {

    private static final DateTimeFormatter FRONTEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final PackageAssignmentReportService packageAssignmentReportService;

    @Override
    public ResponseEntity<ApiResponseDto<PackageAssignmentReportDTO>> getPackageAssignmentReport(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String fromDate,
            String toDate,
            int offset,
            int pageSize) {
        try {
            LocalDate from = parseFlexibleLocalDate(fromDate, "fromDate");
            LocalDate to = parseFlexibleLocalDate(toDate, "toDate");
            validateDateRange(from, to);

            PackageAssignmentReportDTO report = packageAssignmentReportService.getPackageAssignmentReport(
                    clientAdminId, mspId, search, status, from, to, offset, pageSize);
            return ResponseEntity.ok(new ApiResponseDto<>("Package assignment report generated successfully", 200, report));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid package assignment report request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error generating package assignment report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to generate package assignment report: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<Resource> exportPackageAssignmentReport(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String fromDate,
            String toDate) {
        try {
            LocalDate from = parseFlexibleLocalDate(fromDate, "fromDate");
            LocalDate to = parseFlexibleLocalDate(toDate, "toDate");
            validateDateRange(from, to);

            byte[] csv = packageAssignmentReportService.exportPackageAssignmentReportCsv(
                    clientAdminId, mspId, search, status, from, to);

            String fileName = "package-assignment-report-" + Instant.now().toEpochMilli() + ".csv";
            ByteArrayResource resource = new ByteArrayResource(csv);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid package assignment report export request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error exporting package assignment report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be on or after fromDate");
        }
    }

    private LocalDate parseFlexibleLocalDate(String dateValue, String fieldName) {
        if (dateValue == null || dateValue.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateValue);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(dateValue, FRONTEND_DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                try {
                    return Instant.parse(dateValue).atZone(ZoneOffset.UTC).toLocalDate();
                } catch (DateTimeParseException ex2) {
                    throw new IllegalArgumentException(
                            "Invalid " + fieldName + " format. Use yyyy-MM-dd, yyyy/MM/dd, or ISO-8601");
                }
            }
        }
    }
}
