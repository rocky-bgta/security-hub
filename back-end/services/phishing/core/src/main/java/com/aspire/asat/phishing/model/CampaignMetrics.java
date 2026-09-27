package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded model for campaign-level metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMetrics {

    @Builder.Default
    private int totalCampaigns = 0;

    @Builder.Default
    private int activeCampaigns = 0;

    @Builder.Default
    private int completedCampaigns = 0;

    @Builder.Default
    private int draftCampaigns = 0;

    @Builder.Default
    private int scheduledCampaigns = 0;

    @Builder.Default
    private int cancelledCampaigns = 0;

    @Builder.Default
    private double avgSuccessRate = 0.0;  // % of users who didn't click
}
