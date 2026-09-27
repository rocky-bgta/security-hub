package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for email statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatsDto {

    private int totalRecipients;
    private int totalEmailsSent;
    private int emailsDelivered;
    private int emailsBounced;
    private int emailsOpened;
    private int linksClicked;
    private int attachmentsOpened;
    private int dataSubmitted;
    private int emailsReported;

    private double deliveryRate;
    private double openRate;
    private double clickRate;
    private double compromiseRate;
    private double reportRate;
    private double phishPronePercentage;
}
