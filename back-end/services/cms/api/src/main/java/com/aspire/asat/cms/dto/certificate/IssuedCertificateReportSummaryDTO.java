package com.aspire.asat.cms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuedCertificateReportSummaryDTO {
    private Long totalIssued;
    private Long thisMonth;
    private Long thisQuarter;
}
