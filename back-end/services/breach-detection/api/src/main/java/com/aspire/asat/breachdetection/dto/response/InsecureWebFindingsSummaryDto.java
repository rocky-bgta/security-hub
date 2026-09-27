package com.aspire.asat.breachdetection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebFindingsSummaryDto {
    private long domainCount;
    private long breachCount;
}
