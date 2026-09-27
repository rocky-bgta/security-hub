package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.LicenseAssignmentController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentExportDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;
import com.aspire.asat.cms.service.LicenseAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LicenseAssignmentControllerImpl implements LicenseAssignmentController {

    private static final DateTimeFormatter FRONTEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final LicenseAssignmentService licenseAssignmentService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<LicenseAssignmentRowDto>>>> listLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            String fromDate,
            String toDate,
            int offset,
            int pageSize) {
        try {
            LocalDate from = parseFlexibleLocalDate(fromDate, "fromDate");
            LocalDate to = parseFlexibleLocalDate(toDate, "toDate");
            validateDateRange(from, to);

            AllResponseDto<List<LicenseAssignmentRowDto>> page = licenseAssignmentService.listLicenseAssignments(
                    clientAdminId, mspId, search, packageId, productId, department, status,
                    from, to, offset, pageSize);
            return ResponseEntity.ok(new ApiResponseDto<>("License assignments retrieved successfully", 200, page));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid license assignments request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error listing license assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to list license assignments: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<byte[]> exportLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            String fromDate,
            String toDate,
            String format) {
        try {
            LocalDate from = parseFlexibleLocalDate(fromDate, "fromDate");
            LocalDate to = parseFlexibleLocalDate(toDate, "toDate");
            validateDateRange(from, to);

            LicenseAssignmentExportDto export = licenseAssignmentService.exportLicenseAssignments(
                    clientAdminId, mspId, search, packageId, productId, department, status,
                    from, to, format);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(export.getContentType()))
                    .body(export.getContent());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid license assignments export request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error exporting license assignments", e);
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
