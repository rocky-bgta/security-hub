package com.aspire.asat.registration.data.reports;

/**
 * Relative date windows for report filtering. When supplied, a QuickRange takes
 * precedence over any explicit fromDate/toDate and is resolved against "now" by
 * {@code QuickRangeResolver}.
 */
public enum QuickRange {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_3_MONTHS,
    LAST_YEAR
}
