package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Embedded model for campaign statistics.
 * Tracks real-time metrics for the campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStats {

    @Builder.Default
    private int totalRecipients = 0;

    @Builder.Default
    private int emailsSent = 0;

    @Builder.Default
    private int emailsDelivered = 0;

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
    private int emailsBounced = 0;

    /** Number of recipients assigned a training sub-package (PHISHING_WITH_TRAINING campaigns). */
    @Builder.Default
    private int trainingAssignedCount = 0;

    /** Number of assigned recipients who have completed their training. */
    @Builder.Default
    private int trainingCompletedCount = 0;

    @Builder.Default
    private int smsSent = 0;

    @Builder.Default
    private int smsDelivered = 0;

    @Builder.Default
    private int smsFailed = 0;

    @Builder.Default
    private int callsTotal = 0;

    @Builder.Default
    private int callsAnswered = 0;

    @Builder.Default
    private int callsEngaged = 0;

    @Builder.Default
    private int callsReported = 0;

    @Builder.Default
    private int callsCompromised = 0;

    @Builder.Default
    private int callsNoAnswer = 0;

    @Builder.Default
    private int callsFailed = 0;

    @Builder.Default
    private int retriesTriggered = 0;

    private Instant lastUpdatedAt;

    /**
     * Calculate open rate percentage based on totalRecipients.
     */
    public double getOpenRate() {
        return totalRecipients > 0 ? (double) emailsOpened / totalRecipients * 100 : 0;
    }

    /**
     * Calculate click rate percentage based on totalRecipients.
     */
    public double getClickRate() {
        return totalRecipients > 0 ? (double) linksClicked / totalRecipients * 100 : 0;
    }

    /**
     * Calculate submission rate percentage based on totalRecipients.
     */
    public double getSubmissionRate() {
        return totalRecipients > 0 ? (double) dataSubmitted / totalRecipients * 100 : 0;
    }

    /**
     * Calculate report rate percentage based on totalRecipients.
     */
    public double getReportRate() {
        return totalRecipients > 0 ? (double) emailsReported / totalRecipients * 100 : 0;
    }
}
