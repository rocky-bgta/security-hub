package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for campaign statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatsDto {

    private int totalRecipients;
    private int emailsSent;
    private int emailsDelivered;
    private int emailsOpened;
    private int linksClicked;
    private int attachmentsOpened;
    private int dataSubmitted;
    private int emailsReported;
    private int emailsBounced;
    private int smsSent;
    private int smsDelivered;
    private int smsFailed;
    private int callsTotal;
    private int callsAnswered;
    private int callsCompromised;
    private int callsNoAnswer;
    private int callsFailed;
    private int retriesTriggered;

    // Calculated rates
    private double deliveryRate;
    private double openRate;
    private double clickRate;
    private double submissionRate;
    private double reportRate;

    private Instant lastUpdatedAt;
}
