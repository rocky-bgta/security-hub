package com.aspire.asat.cms.service.certificate;

import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.*;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.dto.exam.CertificateLinksDTO;
import com.aspire.asat.common.dto.files.CurrentUserContext;

import java.util.List;

public interface CertificateService {

    List<CertificateResponseDTO> getUserCertificates(String userId, String packageId, int offset, int pageSize);
    long countUserCertificates(String userId, String packageId);
    CertificateStatsResponseDTO getCertificateStats(String userId);
    CertificateSummaryResponseDTO getCertificateSummary(CurrentUserContext context, Boolean isClientAdmin);
    CertificateLinksDTO generateCertificate(String userId, String subPackageName,String subPackageId, String certificateId, String clientAdminId, Boolean isTrial);

    /**
     * Generate certificate with explicit recipient name (for async context where request context is not available).
     */
    CertificateLinksDTO generateCertificate(String userId, String subPackageName, String subPackageId,
                                           String certificateId, String clientAdminId, String recipientFullName, Boolean isTrial);

    List<CertificateDetailsResponseDTO> getCertificateDetails(CurrentUserContext context, Boolean isClientAdmin,
                                                             String clientAdminId, String mspId,
                                                             String certificateName, String productId,
                                                             CertificateStatus status, int page, int size);
    long countCertificateDetails(CurrentUserContext context, Boolean isClientAdmin,
                                 String clientAdminId, String mspId, String certificateName,
                                 String productId, CertificateStatus status);

    /**
     * Get exam certificates with pagination, search, and filtering.
     * Joins UserSubPackage (examCompleted=true) with UserCertificate.
     */
    org.springframework.data.domain.Page<ExamCertificateResponseDTO> getExamCertificates(
            String countryId,
            String mspId,
            String search,
            String clientAdminId,
            String subpackageId,
            int offset,
            int pageSize);

    /**
     * Get certificate summary statistics including total issued, active certificates, and completion rate.
     */
    CertificateSummaryStatsResponseDTO getCertificateSummaryStats(
            String clientAdminId,
            String mspId,
            String productId,
            java.time.Instant fromDate,
            java.time.Instant toDate);

    /**
     * Get exam certificate by examId by joining Exam table with UserCertificate table.
     */
    ExamCertificateResponseDTO getExamCertificateByExamId(String examId);

}
