package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for campaign performance metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignPerformanceDto {

    private String campaignId;
    private String campaignName;
    private CampaignStatus status;
    private CampaignChannel channel;

    // Counts
    private int totalRecipients;
    private int emailsSent;
    private int emailsDelivered;
    private int opened;
    private int clicked;
    private int dataSubmitted;
    private int reported;
    private int bounced;

    // Rates
    private double deliveryRate;
    private double openRate;
    private double clickRate;
    private double compromiseRate;
    private double reportRate;

    // Timing
    private Instant startDate;
    private Instant endDate;
    private long durationDays;

    // Ranking
    private int riskRanking;
}
