package com.aspire.asat.registration.data.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregate license counts that power the summary cards in the Subscription
 * Summary Report. All counts are computed over the filtered subscription set
 * (context scope + date range + search), independent of the optional status
 * filter which only narrows the detail list / CSV export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSummaryTotalsDTO {

    private long totalProducts;
    private long totalPackages;
    private long totalLicenses;
    private long activeLicenses;
    private long unusedLicenses;
    private long pendingLicenses;
    private long expiredLicenses;
}
