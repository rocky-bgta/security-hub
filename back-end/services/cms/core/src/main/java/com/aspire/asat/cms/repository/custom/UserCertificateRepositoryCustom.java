package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.model.UserCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Custom repository interface for UserCertificate with advanced search capabilities.
 */
public interface UserCertificateRepositoryCustom {

    /**
     * Find user certificates by clientAdminId with optional search, product, and status filters.
     */
    Page<UserCertificate> findByClientAdminIdWithSearch(
            String clientAdminId, String search, String productId, CertificateStatus status, Pageable pageable);

    Page<UserCertificate> findByClientAdminIdIn(
            List<String> clientAdminIds, String productId, CertificateStatus status, Pageable pageable);

    Page<UserCertificate> findByClientAdminIdInWithSearch(
            List<String> clientAdminIds, String search, String productId, CertificateStatus status, Pageable pageable);

    long countByClientAdminIdIn(List<String> clientAdminIds, String productId, CertificateStatus status);

    long countByClientAdminIdInWithSearch(
            List<String> clientAdminIds, String search, String productId, CertificateStatus status);

    /**
     * Find user certificates by userId with optional search, product, and status filters.
     */
    Page<UserCertificate> findByUserIdWithSearch(
            String userId, String search, String productId, CertificateStatus status, Pageable pageable);

    /**
     * Count user certificates by clientAdminId with optional search, product, and status filters.
     */
    long countByClientAdminIdWithSearch(
            String clientAdminId, String search, String productId, CertificateStatus status);

    /**
     * Count user certificates by userId with optional search, product, and status filters.
     */
    long countByUserIdWithSearch(String userId, String search, String productId, CertificateStatus status);

    /**
     * Find exam certificates by joining Exam (examCompleted=true) with UserCertificate.
     * Supports search by fullName and productName, and filter by countryId, mspId, clientAdminId and subpackageId.
     *
     * @param countryId     filter by countryId (optional)
     * @param mspId        filter by mspId (optional)
     * @param search        search term for fullName or productName (case-insensitive, optional)
     * @param clientAdminId filter by clientAdminId (optional)
     * @param subpackageId  filter by subpackageId (optional)
     * @param pageable      pagination information
     * @return Page of exam certificates with joined data
     */
    Page<ExamCertificateResponseDTO> findExamCertificatesWithJoin(
            String countryId,
            String mspId,
            String search,
            String clientAdminId,
            String subpackageId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Count exam certificates by joining Exam (examCompleted=true) with UserCertificate.
     * Supports search by fullName and productName, and filter by countryId, mspId, clientAdminId and subpackageId.
     *
     * @param countryId     filter by countryId (optional)
     * @param mspId         filter by mspId (optional)
     * @param search        search term for fullName or productName (case-insensitive, optional)
     * @param clientAdminId filter by clientAdminId (optional)
     * @param subpackageId  filter by subpackageId (optional)
     * @return count of matching exam certificates
     */
    long countExamCertificatesWithJoin(String countryId, String mspId, String search, String clientAdminId, String subpackageId);

    /**
     * Get certificate summary statistics including total issued, active certificates, and completion rate.
     * Aggregates data from UserCertificate and UserSubPackage collections.
     *
     * @param productId filter by productId (optional)
     * @param fromDate  filter certificates issued from this date (optional)
     * @param toDate    filter certificates issued until this date (optional)
     * @return CertificateSummaryStatsResponseDTO with statistics
     */
   CertificateSummaryStatsResponseDTO getCertificateSummaryStats(
            String productId,
            Instant fromDate,
            Instant toDate);

    /**
     * Get certificate summary statistics scoped to the given client admin IDs.
     *
     * @param clientAdminIds list of client admin IDs to include in aggregation
     * @param productId      filter by productId (optional)
     * @param fromDate       filter certificates issued from this date (optional)
     * @param toDate         filter certificates issued until this date (optional)
     * @return CertificateSummaryStatsResponseDTO with aggregated statistics
     */
    CertificateSummaryStatsResponseDTO getCertificateSummaryStatsForClientAdmins(
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate);

    ExpiredCertificateReportSummaryDTO getCertificateReportSummaryStats(
            String clientAdminId,
            Instant fromDate,
            Instant toDate,
            Integer thresholdDays,
            String productId);

    ExpiredCertificateReportSummaryDTO getCertificateReportSummaryStatsForClientAdminIds(
            List<String> clientAdminIds,
            Instant fromDate,
            Instant toDate,
            Integer thresholdDays,
            String productId);

    Page<UserCertificate> findCertificatesForReport(
            String clientAdminId,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField,
            Pageable pageable);

    Page<UserCertificate> findCertificatesForReportByClientAdminIds(
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField,
            Pageable pageable);

    List<UserCertificate> findCertificatesForReportExport(
            String clientAdminId,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField);

    List<UserCertificate> findCertificatesForReportExportByClientAdminIds(
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField);

    IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummaryStats(
            String clientAdminId,
            Instant fromDate,
            Instant toDate);

    IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummaryStatsForClientAdminIds(
            List<String> clientAdminIds,
            Instant fromDate,
            Instant toDate);

    /**
     * Find exam certificate by examId by joining Exam table with UserCertificate table.
     *
     * @param examId the exam ID
     * @return ExamCertificateResponseDTO with joined data, or null if not found
     */
    ExamCertificateResponseDTO findExamCertificateByExamId(String examId);
}

