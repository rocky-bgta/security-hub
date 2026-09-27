package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user risk distribution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskDistributionDto {

    private int totalUsers;
    private int lowRiskCount;
    private int mediumRiskCount;
    private int highRiskCount;
    private int criticalRiskCount;

    private double lowRiskPercentage;
    private double mediumRiskPercentage;
    private double highRiskPercentage;
    private double criticalRiskPercentage;

    private int repeatOffenders;
    private int compromisedUsers;
}
