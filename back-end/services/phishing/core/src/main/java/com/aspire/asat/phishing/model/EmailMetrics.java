package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded model for email-level metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMetrics {

    @Builder.Default
    private int totalRecipients = 0;

    @Builder.Default
    private int totalEmailsSent = 0;

    @Builder.Default
    private int emailsDelivered = 0;

    @Builder.Default
    private int emailsBounced = 0;

    @Builder.Default
    private int emailsOpened = 0;

    @Builder.Default
    private int linksClicked = 0;

    @Builder.Default
    private int attachmentsOpened = 0;

    @Builder.Default
    private int dataSubmitted = 0;

    @Builder.Default
    private int emailsReported = 0;

    @Builder.Default
    private double deliveryRate = 0.0;

    @Builder.Default
    private double openRate = 0.0;

    @Builder.Default
    private double clickRate = 0.0;

    @Builder.Default
    private double submissionRate = 0.0;

    @Builder.Default
    private double reportRate = 0.0;

    @Builder.Default
    private double phishPronePercentage = 0.0;

    /**
     * Calculate derived rates
     */
    public void calculateRates() {
        this.deliveryRate = totalEmailsSent > 0
            ? (double) emailsDelivered / totalEmailsSent * 100 : 0;
        this.openRate = totalRecipients > 0
            ? (double) emailsOpened / totalRecipients * 100 : 0;
        this.clickRate = totalRecipients > 0
            ? (double) linksClicked / totalRecipients * 100 : 0;
        this.submissionRate = totalRecipients > 0
            ? (double) dataSubmitted / totalRecipients * 100 : 0;
        this.reportRate = totalRecipients > 0
            ? (double) emailsReported / totalRecipients * 100 : 0;
        this.phishPronePercentage = totalEmailsSent > 0
            ? (double) linksClicked / totalEmailsSent * 100 : 0;
    }
}
