package com.aspire.asat.phishing.service;

/**
 * Service for publishing campaign emails to SQS for asynchronous sending.
 */
public interface CampaignEmailService {

    /**
     * Loads campaign data (template, sender profile, recipients), personalizes
     * each email, and publishes one SQS message per PENDING recipient.
     *
     * @param campaignId the campaign to send emails for
     */
    void publishCampaignEmails(String campaignId);
}
