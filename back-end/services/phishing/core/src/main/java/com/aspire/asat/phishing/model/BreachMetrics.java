package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded model for breach detection metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachMetrics {

    @Builder.Default
    private int totalBreaches = 0;

    @Builder.Default
    private int affectedUsers = 0;

    @Builder.Default
    private int resolvedBreaches = 0;

    @Builder.Default
    private int pendingActions = 0;

    @Builder.Default
    private int newBreachesThisMonth = 0;
}
