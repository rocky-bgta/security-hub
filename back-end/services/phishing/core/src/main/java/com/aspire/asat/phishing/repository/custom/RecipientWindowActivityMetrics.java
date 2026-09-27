package com.aspire.asat.phishing.repository.custom;

/**
 * Per-recipient email activity counters aggregated over a time window.
 */
public record RecipientWindowActivityMetrics(
        String recipientEmail,
        int delivered,
        int opened,
        int clicked,
        int submits,
        int reported) {
}
