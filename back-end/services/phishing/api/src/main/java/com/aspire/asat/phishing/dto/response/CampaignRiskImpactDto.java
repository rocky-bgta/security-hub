package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO representing top risk impact for a campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRiskImpactDto {

    private String campaignId;
    private String campaignName;
    private String type;
    private String targetGroup;
    private RiskLevel riskImpact;
    private String aiRating;
    private int impactedUserCount;
    private String status;
    private Instant startDate;
    private Instant endDate;
}
