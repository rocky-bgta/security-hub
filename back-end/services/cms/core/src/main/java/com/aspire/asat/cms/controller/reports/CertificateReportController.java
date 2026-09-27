package com.aspire.asat.cms.controller.reports;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificate.CertificateExportRequestDto;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.ExpiringCertificateResponseDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Certificate Reports API's", description = "Endpoints for certificate reports (expired/expiring and issued)")
@RequestMapping(value = WebApiUrlConstants.CLIENT_API, produces = "application/json")
public interface CertificateReportController {

    @Operation(summary = "Get expiring certificates",
            description = "Returns a paginated list of certificates expiring within the next 30 days.")
    @GetMapping("/expiring-certificates")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpiringCertificateResponseDTO>>>> getExpiringCertificates(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Export certificates to Excel and send via email",
            description = "Fetches UserCertificate records by business certificateId (or Mongo id), generates Excel, uploads to S3, and emails the client admin.")
    @PostMapping("/email-expiring-certificates")
    ResponseEntity<ApiResponseDto<Void>> exportCertificatesToExcelAndEmail(
            @Valid @RequestBody CertificateExportRequestDto requestDto);

    @Operation(summary = "Export certificates to Excel and download",
            description = "Fetches expiring UserCertificate records and returns an Excel download.")
    @PostMapping("/export-certificates")
    void exportCertificatesToExcel(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) @Min(0) Integer offset,
            @RequestParam(required = false) @Min(1) Integer pageSize,
            HttpServletResponse response);

    @Operation(summary = "Get expired/expiring certificate report summary")
    @GetMapping("/expired-certificate-report/summary")
    ResponseEntity<ApiResponseDto<ExpiredCertificateReportSummaryDTO>> getExpiredCertificateReportSummary(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer thresholdDays);

    @Operation(summary = "Get expired/expiring certificate report rows",
            description = "Returns expired certificates and those expiring within thresholdDays (default 60). "
                    + "Certificates valid beyond the threshold are excluded unless an explicit status filter is provided.")
    @GetMapping("/expired-certificate-report")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpiredCertificateReportRowDTO>>>> getExpiredCertificateReportRows(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by certificate status: VALID, EXPIRING_SOON, or EXPIRED")
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(required = false) Integer thresholdDays,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Export expired/expiring certificate report")
    @PostMapping("/expired-certificate-report/export")
    void exportExpiredCertificateReport(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Start date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "End date filter. Supported formats: yyyy-MM-dd, yyyy/MM/dd or ISO-8601")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by certificate status: VALID, EXPIRING_SOON, or EXPIRED")
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(required = false) Integer thresholdDays,
            HttpServletResponse response);

    @Operation(summary = "Get issued certificate report summary",
            description = "Returns total issued, this month, and this quarter counts.")
    @GetMapping("/issued-certificate-report/summary")
    ResponseEntity<ApiResponseDto<IssuedCertificateReportSummaryDTO>> getIssuedCertificateReportSummary(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Optional issued-date from filter (createdAt)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Optional issued-date to filter (createdAt)")
            @RequestParam(required = false) String toDate);

    @Operation(summary = "Get issued certificate report rows")
    @GetMapping("/issued-certificate-report")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<IssuedCertificateReportRowDTO>>>> getIssuedCertificateReportRows(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Optional issued-date from filter (createdAt)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Optional issued-date to filter (createdAt)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by certificate status: VALID, EXPIRING_SOON, or EXPIRED")
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Export issued certificate report")
    @PostMapping("/issued-certificate-report/export")
    void exportIssuedCertificateReport(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Optional issued-date from filter (createdAt)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Optional issued-date to filter (createdAt)")
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by certificate status: VALID, EXPIRING_SOON, or EXPIRED")
            @RequestParam(required = false) CertificateStatus status,
            HttpServletResponse response);
}
