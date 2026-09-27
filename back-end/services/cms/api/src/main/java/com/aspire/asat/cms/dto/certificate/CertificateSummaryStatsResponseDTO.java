package com.aspire.asat.cms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for certificate summary statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateSummaryStatsResponseDTO {
    private Long totalCertificatesIssued;
    private Long activeCertificatesCount;
    private Double averageCompletionRate; // Percentage (0-100)
    private Long expiringThisMonth;
}

