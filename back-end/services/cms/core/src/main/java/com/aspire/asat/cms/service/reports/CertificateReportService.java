package com.aspire.asat.cms.service.reports;

import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.ExpiringCertificateResponseDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public interface CertificateReportService {

    Page<ExpiringCertificateResponseDTO> getExpiringCertificates(
            String clientAdminId, String mspId, int offset, int pageSize);

    void exportCertificatesToExcelAndEmail(String adminId, List<String> certificateIds);

    byte[] exportCertificatesToExcel(String adminId, String mspId, Integer offset, Integer pageSize);

    ExpiredCertificateReportSummaryDTO getExpiredCertificateReportSummary(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, Integer thresholdDays);

    Page<ExpiredCertificateReportRowDTO> getExpiredCertificateReportRows(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, Integer thresholdDays, int offset, int pageSize);

    byte[] exportExpiredCertificateReportToExcel(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, Integer thresholdDays);

    IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummary(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate);

    Page<IssuedCertificateReportRowDTO> getIssuedCertificateReportRows(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status, int offset, int pageSize);

    byte[] exportIssuedCertificateReportToExcel(
            String clientAdminId, String mspId, Instant fromDate, Instant toDate, String search,
            CertificateStatus status);
}
