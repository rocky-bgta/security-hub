package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.model.CampaignRecipient;

import java.util.Optional;

/**
 * Creates and resolves per-recipient shortened SMS links.
 */
public interface UrlShortenerService {

    /**
     * Persist (or reuse) a unique short code for the recipient and return the public SMS URL
     * {@code {shortOrigin}/{shortCode}}. Short codes are environment-prefixed
     * (e.g. {@code 01} + 8 random chars for development).
     */
    String shorten(String originalUrl, String shortOrigin, CampaignRecipient recipient);

    /**
     * Look up the original landing URL for a short code.
     */
    Optional<String> resolve(String shortCode);

    /**
     * Same length and shape as a real short URL, without persisting. Used for SMS 160-char checks.
     */
    String previewUrl(String shortOrigin);
}
