package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateSummaryResponseDTO {
    private int validCount;
    private int expiredCount;
    private int expiringSoonCount;
    private int notCompleteCount;
}
