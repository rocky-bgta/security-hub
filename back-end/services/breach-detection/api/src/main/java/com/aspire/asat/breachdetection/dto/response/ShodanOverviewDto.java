package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Backs the three stat tiles at the top of the dashboard
 * ("Number of IPs / Domains", "Total Breaches", "Last Scan").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanOverviewDto {
    private ShodanSubjectType subjectType;
    private long subjectsMonitored;
    private long totalBreaches;
    private long openBreaches;
    private long resolvedBreaches;
    private Instant lastScanAt;
}
