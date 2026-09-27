package com.aspire.asat.phishing.service;

/**
 * Summed email counters rolled up from all client campaigns (embedded {@code CampaignStats}).
 */
public record CampaignEmailCounters(
        int totalRecipients,
        int totalEmailsSent,
        int emailsDelivered,
        int emailsBounced,
        int emailsOpened,
        int linksClicked,
        int attachmentsOpened,
        int dataSubmitted,
        int emailsReported
) {
    public static CampaignEmailCounters empty() {
        return new CampaignEmailCounters(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
