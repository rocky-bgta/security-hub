package com.aspire.asat.cms.controller.reports.impl;

import com.aspire.asat.cms.controller.reports.CertificateReportController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificate.CertificateExportRequestDto;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.ExpiringCertificateResponseDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.service.reports.CertificateReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CertificateReportControllerImpl implements CertificateReportController {

    private static final DateTimeFormatter FRONTEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final CertificateReportService certificateReportService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpiringCertificateResponseDTO>>>> getExpiringCertificates(
            String clientAdminId, String mspId, int offset, int pageSize) {
        try {
            Page<ExpiringCertificateResponseDTO> page = certificateReportService.getExpiringCertificates(
                    clientAdminId, mspId, offset, pageSize);
            AllResponseDto<List<ExpiringCertificateResponseDTO>> response = new AllResponseDto<>(
                    offset, pageSize, page.getTotalElements(), page.getContent());
            return ResponseEntity.ok(new ApiResponseDto<>("Expiring certificates fetched successfully", 200, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error getting expiring certificates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get expiring certificates: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> exportCertificatesToExcelAndEmail(CertificateExportRequestDto requestDto) {
        try {
            certificateReportService.exportCertificatesToExcelAndEmail(
                    requestDto.getAdminId(), requestDto.getCertificateIds());
            return ResponseEntity.ok(new ApiResponseDto<>("Certificates exported successfully and email sent", 200, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("Invalid request: " + e.getMessage(), 400, null));
        } catch (com.aspire.asat.cms.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>("Resource not found: " + e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Failed to export certificates and send email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to export certificates: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public void exportCertificatesToExcel(
            String adminId, String mspId, Integer offset, Integer pageSize, HttpServletResponse response) {
        try {
            byte[] excelBytes = certificateReportService.exportCertificatesToExcel(adminId, mspId, offset, pageSize);
            String scope = adminId != null && !adminId.isBlank()
                    ? adminId.trim()
                    : (mspId != null && !mspId.isBlank() ? "msp" : "all");
            writeExcelDownloadResponse(response, excelBytes,
                    "certificates_export_" + scope + "_" + Instant.now().toEpochMilli() + ".xlsx");
        } catch (IllegalArgumentException e) {
            sendDownloadError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage(), e);
        } catch (com.aspire.asat.cms.exception.ResourceNotFoundException e) {
            sendDownloadError(response, HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + e.getMessage(), e);
        } catch (Exception e) {
            sendDownloadError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to export certificates: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExpiredCertificateReportSummaryDTO>> getExpiredCertificateReportSummary(
            String clientAdminId, String mspId, String fromDate, String toDate, Integer thresholdDays) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            ExpiredCertificateReportSummaryDTO summary = certificateReportService.getExpiredCertificateReportSummary(
                    clientAdminId, mspId, fromDateInstant, toDateInstant, thresholdDays);
            return ResponseEntity.ok(new ApiResponseDto<>("Expired certificate report summary fetched successfully", 200, summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get expired certificate report summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch expired certificate report summary: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpiredCertificateReportRowDTO>>>> getExpiredCertificateReportRows(
            String clientAdminId, String mspId, String fromDate, String toDate, String search,
            CertificateStatus status, Integer thresholdDays, int offset, int pageSize) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            Page<ExpiredCertificateReportRowDTO> page = certificateReportService.getExpiredCertificateReportRows(
                    clientAdminId, mspId, fromDateInstant, toDateInstant, search, status, thresholdDays, offset, pageSize);
            AllResponseDto<List<ExpiredCertificateReportRowDTO>> response = new AllResponseDto<>(
                    offset, pageSize, page.getTotalElements(), page.getContent());
            return ResponseEntity.ok(new ApiResponseDto<>("Expired certificate report rows fetched successfully", 200, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get expired certificate report rows", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch expired certificate report rows: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public void exportExpiredCertificateReport(
            String clientAdminId, String mspId, String fromDate, String toDate, String search,
            CertificateStatus status, Integer thresholdDays, HttpServletResponse response) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            byte[] excelBytes = certificateReportService.exportExpiredCertificateReportToExcel(
                    clientAdminId, mspId, fromDateInstant, toDateInstant, search, status, thresholdDays);
            writeExcelDownloadResponse(response, excelBytes,
                    "Expired_Certificate_Report_" + Instant.now().toEpochMilli() + ".xlsx");
        } catch (IllegalArgumentException e) {
            sendDownloadError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage(), e);
        } catch (com.aspire.asat.cms.exception.ResourceNotFoundException e) {
            sendDownloadError(response, HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + e.getMessage(), e);
        } catch (Exception e) {
            sendDownloadError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to export expired certificate report: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<IssuedCertificateReportSummaryDTO>> getIssuedCertificateReportSummary(
            String clientAdminId, String mspId, String fromDate, String toDate) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            IssuedCertificateReportSummaryDTO summary = certificateReportService.getIssuedCertificateReportSummary(
                    clientAdminId, mspId, fromDateInstant, toDateInstant);
            return ResponseEntity.ok(new ApiResponseDto<>("Issued certificate report summary fetched successfully", 200, summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get issued certificate report summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch issued certificate report summary: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<IssuedCertificateReportRowDTO>>>> getIssuedCertificateReportRows(
            String clientAdminId, String mspId, String fromDate, String toDate, String search,
            CertificateStatus status, int offset, int pageSize) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            Page<IssuedCertificateReportRowDTO> page = certificateReportService.getIssuedCertificateReportRows(
                    clientAdminId, mspId, fromDateInstant, toDateInstant, search, status, offset, pageSize);
            AllResponseDto<List<IssuedCertificateReportRowDTO>> response = new AllResponseDto<>(
                    offset, pageSize, page.getTotalElements(), page.getContent());
            return ResponseEntity.ok(new ApiResponseDto<>("Issued certificate report rows fetched successfully", 200, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to get issued certificate report rows", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch issued certificate report rows: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public void exportIssuedCertificateReport(
            String clientAdminId, String mspId, String fromDate, String toDate, String search,
            CertificateStatus status, HttpServletResponse response) {
        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");
            byte[] excelBytes = certificateReportService.exportIssuedCertificateReportToExcel(
                    clientAdminId, mspId, fromDateInstant, toDateInstant, search, status);
            writeExcelDownloadResponse(response, excelBytes,
                    "Issued_Certificate_Report_" + Instant.now().toEpochMilli() + ".xlsx");
        } catch (IllegalArgumentException e) {
            sendDownloadError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage(), e);
        } catch (com.aspire.asat.cms.exception.ResourceNotFoundException e) {
            sendDownloadError(response, HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + e.getMessage(), e);
        } catch (Exception e) {
            sendDownloadError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to export issued certificate report: " + e.getMessage(), e);
        }
    }

    private void writeExcelDownloadResponse(HttpServletResponse response, byte[] excelBytes, String fileName)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    private void sendDownloadError(HttpServletResponse response, int status, String message, Exception e) {
        log.error(message, e);
        try {
            response.sendError(status, message);
        } catch (IOException ioException) {
            log.error("Failed to send error response", ioException);
        }
    }

    private Instant parseFlexibleDate(String dateValue, boolean endOfDay, String fieldName) {
        if (dateValue == null || dateValue.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(dateValue);
        } catch (DateTimeParseException ignored) {
            try {
                return localDateToInstant(LocalDate.parse(dateValue), endOfDay);
            } catch (DateTimeParseException ignoredIso) {
                try {
                    return localDateToInstant(LocalDate.parse(dateValue, FRONTEND_DATE_FORMAT), endOfDay);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException(
                            "Invalid " + fieldName + " format. Use yyyy-MM-dd, yyyy/MM/dd, or ISO-8601 (e.g., 2024-01-01T00:00:00Z)");
                }
            }
        }
    }

    private Instant localDateToInstant(LocalDate date, boolean endOfDay) {
        return endOfDay
                ? date.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC)
                : date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
