package com.aspire.asat.phishing.repository.custom;

import java.time.Instant;

/**
 * Atomic update operations for {@code campaign_recipients} that avoid the
 * read-modify-write pattern and silently-skipped updates.
 */
public interface CampaignRecipientRepositoryCustom {

    /**
     * Atomically transitions a recipient to {@code SENT} when current status is {@code PENDING}.
     * Tries by {@code _id} first, falls back to {@code trackingId} if no match.
     *
     * @return {@code true} if exactly one document was modified, {@code false} otherwise.
     */
    boolean markSentByIdOrTrackingId(String recipientId, String trackingId, Instant emailSentAt);

    /**
     * Atomically transitions a recipient to {@code BOUNCED} when current status is {@code PENDING} or {@code SENT}.
     * Tries by {@code _id} first, falls back to {@code trackingId} if no match.
     *
     * @return {@code true} if exactly one document was modified, {@code false} otherwise.
     */
    boolean markBouncedByIdOrTrackingId(String recipientId, String trackingId, Instant bouncedAt);

    /**
     * Counts campaign recipient rows for an end user (total campaigns enrolled).
     */
    long countByUserId(String userId);
}
