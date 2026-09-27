package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatsResponseDTO {
    private int total;
    private int expired;
    private int expiringSoon;
    private int valid; // packages with certificateLink present
}
