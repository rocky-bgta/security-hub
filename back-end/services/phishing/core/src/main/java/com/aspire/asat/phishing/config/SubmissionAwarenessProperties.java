package com.aspire.asat.phishing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the post-submit phishing awareness interstitial page.
 */
@Data
@ConfigurationProperties(prefix = "phishing.submission-awareness")
public class SubmissionAwarenessProperties {

    private static final int MIN_DELAY_SECONDS = 1;
    private static final int MAX_DELAY_SECONDS = 30;

    /**
     * Seconds to display the awareness message before redirecting.
     */
    private int redirectDelaySeconds = 3;

    /**
     * Optional URL for the phishing awareness guide link.
     */
    private String guideUrl = "";

    public int getEffectiveRedirectDelaySeconds() {
        return Math.min(MAX_DELAY_SECONDS, Math.max(MIN_DELAY_SECONDS, redirectDelaySeconds));
    }
}
