package com.aspire.asat.cms.controller.certificate;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateSummaryResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.CertificateDetailsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.service.certificate.CertificateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
public class CertificateControllerImpl implements CertificateController {
    private static final DateTimeFormatter FRONTEND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final CertificateService certificateService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateResponseDTO>>>> getUserCertificates(
            String userId, String subPackageId, int offset, int pageSize) {

        List<CertificateResponseDTO> items = certificateService.getUserCertificates(userId, subPackageId, offset, pageSize);
        long total = certificateService.countUserCertificates(userId, subPackageId);

        AllResponseDto<List<CertificateResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>("Certificates fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateStatsResponseDTO>> getCertificateStats(String userId) {
        CertificateStatsResponseDTO stats = certificateService.getCertificateStats(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("Certificate stats fetched", 200, stats));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateSummaryResponseDTO>> getCertificateSummary(Boolean isClientAdmin) {
        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();

        log.info("Getting certificate summary for userId={}, clientAdminId={}", currentUserContext.getUserId(), isClientAdmin);

        CertificateSummaryResponseDTO summary = certificateService.getCertificateSummary(currentUserContext, isClientAdmin);
        return ResponseEntity.ok(new ApiResponseDto<>("Certificate summary fetched successfully", 200, summary));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateDetailsResponseDTO>>>> getCertificateDetails(
            Boolean isClientAdmin, String clientAdminId, String mspId, String certificateName,
            String productId, CertificateStatus status, int offset, int pageSize) {

        CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();

        log.info("Getting certificate details for userId={}, isClientAdmin={}, clientAdminId={}, mspId={}, certificateName={}, productId={}, status={}, offset={}, pageSize={}",
                currentUserContext.getUserId(), isClientAdmin, clientAdminId, mspId, certificateName, productId, status, offset, pageSize);

        List<CertificateDetailsResponseDTO> items = certificateService.getCertificateDetails(
                currentUserContext, isClientAdmin, clientAdminId, mspId, certificateName, productId, status, offset, pageSize);
        long total = certificateService.countCertificateDetails(
                currentUserContext, isClientAdmin, clientAdminId, mspId, certificateName, productId, status);

        AllResponseDto<List<CertificateDetailsResponseDTO>> response = new AllResponseDto<>(offset * pageSize, pageSize, total, items);
        return ResponseEntity.ok(new ApiResponseDto<>("Certificate details fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ExamCertificateResponseDTO>>>> getExamCertificates(
            String countryId, String mspId, String search, String clientAdminId, String subpackageId, int offset, int pageSize) {

        log.info("Getting exam certificates - search: {}, clientAdminId: {}, subpackageId: {}, offset: {}, pageSize: {}",
                search, clientAdminId, subpackageId, offset, pageSize);

        Page<ExamCertificateResponseDTO> page = certificateService.getExamCertificates(countryId, mspId, search, clientAdminId, subpackageId, offset, pageSize);

        AllResponseDto<List<ExamCertificateResponseDTO>> response = new AllResponseDto<>(
                offset,
                pageSize,
                page.getTotalElements(),
                page.getContent()
        );

        return ResponseEntity.ok(new ApiResponseDto<>("Exam certificates fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateSummaryStatsResponseDTO>> getCertificateSummaryStats(
            String clientAdminId, String mspId, String productId, String fromDate, String toDate) {

        log.info("Getting certificate summary statistics - clientAdminId: {}, mspId: {}, productId: {}, fromDate: {}, toDate: {}",
                clientAdminId, mspId, productId, fromDate, toDate);

        try {
            Instant fromDateInstant = parseFlexibleDate(fromDate, false, "fromDate");
            Instant toDateInstant = parseFlexibleDate(toDate, true, "toDate");

            CertificateSummaryStatsResponseDTO stats = certificateService.getCertificateSummaryStats(
                    clientAdminId, mspId, productId, fromDateInstant, toDateInstant);

            return ResponseEntity.ok(new ApiResponseDto<>("Certificate summary statistics fetched successfully", 200, stats));
        } catch (Exception e) {
            log.error("Failed to get certificate summary statistics - clientAdminId: {}, mspId: {}, productId: {}, fromDate: {}, toDate: {}. Error: {}",
                    clientAdminId, mspId, productId, fromDate, toDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to fetch certificate summary statistics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamCertificateResponseDTO>> getExamCertificateByExamId(String examId) {
        try {
            log.info("Getting exam certificate by examId: {}", examId);

            ExamCertificateResponseDTO examCertificate = certificateService.getExamCertificateByExamId(examId);

            if (examCertificate == null) {
                log.warn("Exam certificate not found for examId: {}", examId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>("Exam certificate not found", 404, null));
            }

            return ResponseEntity.ok(new ApiResponseDto<>("Exam certificate fetched successfully", 200, examCertificate));
        } catch (Exception e) {
            log.error("Error getting exam certificate by examId: {}", examId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get exam certificate: " + e.getMessage(), 500, null));
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
