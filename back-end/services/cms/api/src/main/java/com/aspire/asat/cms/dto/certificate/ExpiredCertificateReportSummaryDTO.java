package com.aspire.asat.cms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredCertificateReportSummaryDTO {
    private Long totalCertificates;
    private Long totalValidCertificates;
    private Long totalExpiredCertificates;
    private Long totalExpiringCertificates;
}
