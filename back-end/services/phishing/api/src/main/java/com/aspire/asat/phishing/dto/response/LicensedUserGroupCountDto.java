package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Licensed-user count bucket by {@code riskGroup}, shaped like Registration
 * {@code RiskGroupUserCountResponseDTO} for FE chip reuse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensedUserGroupCountDto {

    /** Null when licence rows have no riskGroup snapshot (legacy). */
    private RiskGroup riskGroup;
    private long userCount;
}
