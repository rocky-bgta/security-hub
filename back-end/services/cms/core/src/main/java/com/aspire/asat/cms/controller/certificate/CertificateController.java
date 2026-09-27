package com.aspire.asat.cms.controller.certificate;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Certificate Management API's", description = "Endpoints for certificate management and statistics")
@RequestMapping(value = WebApiUrlConstants.CLIENT_API, produces = "application/json")
public interface CertificateController {

    @Operation(summary = "Get all certificates", description = "Fetches completed courses with certificates for a user")
    @GetMapping("/certificates")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateResponseDTO>>>> getUserCertificates(
            @RequestParam @NotBlank String userId,
            @RequestParam(required = false) String packageId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get certificate statistics", description = "Returns a summary of valid, expiring, and expired certificates for a user")
    @GetMapping("/certificates/stats")
    ResponseEntity<ApiResponseDto<CertificateStatsResponseDTO>> getCertificateStats(
            @RequestParam @NotBlank String userId);

    @Operation(summary = "Certificate summary for Dashboard, Both user, client admin", description = "Returns count of certificates by status (valid, expired, expiring soon) for a user or all users under a client admin")
    @GetMapping("/certificate-summary")
    ResponseEntity<ApiResponseDto<CertificateSummaryResponseDTO>> getCertificateSummary(
            @RequestParam(required = false) Boolean isClientAdmin);

    @Operation(summary = "Get certificate details with pagination and filtering",
            description = "Fetches detailed certificate information with pagination and optional filters. "
                    + "For end users, returns their own certificates. For client admins (isClientAdmin=true), returns all certificates under the client admin. "
                    + "For MSP users (or when mspId is provided), returns certificates for client admins under the MSP. "
                    + "If clientAdminId is provided in MSP context, returns certificates for that client admin only. "
                    + "Supports filtering by productId and status (VALID, EXPIRING_SOON, EXPIRED).")
    @GetMapping("/certificate-details")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateDetailsResponseDTO>>>> getCertificateDetails(
            @RequestParam(required = false) Boolean isClientAdmin,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String certificateName,
            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) String productId,
            @Parameter(description = "Filter by certificate status: VALID, EXPIRING_SOON, or EXPIRED")
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get exam certificates with pagination, search and filtering",
            description = "Fetches exam certificates by joining UserSubPackage (examCompleted=true) with UserCertificate. Supports search by fullName and productName, and filtering by clientAdminId and subpackageId.")
    @GetMapping("/exam-certificates")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ExamCertificateResponseDTO>>>> getExamCertificates(
            @RequestParam(required = false) String countryId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String subpackageId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Get certificate summary statistics",
            description = "Returns total certificates issued, active certificates count (expiryDate > current date), and average completion rate. "
                    + "Supports filtering by productId and date range. "
                    + "For MSP users (or when mspId is provided), returns aggregated stats for client admins under the MSP. "
                    + "If clientAdminId is provided in MSP context, returns stats for that client admin only.")
    @GetMapping("/certificate-summary-stats")
    ResponseEntity<ApiResponseDto<CertificateSummaryStatsResponseDTO>> getCertificateSummaryStats(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate);

    @Operation(summary = "Get exam certificate by exam ID",
            description = "Fetches exam certificate by joining Exam table with UserCertificate table using examId. Returns ExamCertificateResponseDTO with all certificate details.")
    @GetMapping("/exam-certificate/{examId}")
    ResponseEntity<ApiResponseDto<ExamCertificateResponseDTO>> getExamCertificateByExamId(
            @PathVariable @NotBlank @Parameter(description = "The exam ID", required = true) String examId);

}
