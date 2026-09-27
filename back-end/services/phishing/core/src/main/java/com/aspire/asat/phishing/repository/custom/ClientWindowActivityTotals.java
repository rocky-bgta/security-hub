package com.aspire.asat.phishing.repository.custom;

/**
 * Client-level email activity counters aggregated over a time window (KPI metrics).
 */
public record ClientWindowActivityTotals(
        int emailsSent,
        int dataSubmitted,
        int emailsReported) {

    public static ClientWindowActivityTotals empty() {
        return new ClientWindowActivityTotals(0, 0, 0);
    }
}
