package com.aspire.asat.registration.utils;

import com.aspire.asat.registration.data.reports.QuickRange;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Resolves an optional {@link QuickRange} into a concrete {@code [from, toExclusive)}
 * instant window. A supplied {@link QuickRange} always takes precedence over the
 * explicit {@code fromDate}/{@code toDate} passed by the caller; when it is
 * {@code null} the explicit dates are returned unchanged.
 */
public final class QuickRangeResolver {

    private QuickRangeResolver() {
    }

    /**
     * @param quickRange        optional relative window; when non-null it overrides the explicit dates
     * @param fromDate          explicit inclusive lower bound (nullable)
     * @param toDateExclusive   explicit exclusive upper bound (nullable)
     * @return resolved {@link DateRange}; either the quick-range window anchored to "now" or the explicit dates
     */
    public static DateRange resolve(QuickRange quickRange, Instant fromDate, Instant toDateExclusive) {
        if (quickRange == null) {
            return new DateRange(fromDate, toDateExclusive);
        }

        Instant now = Instant.now();
        Instant from = switch (quickRange) {
            case LAST_7_DAYS -> now.minus(7, ChronoUnit.DAYS);
            case LAST_30_DAYS -> now.minus(30, ChronoUnit.DAYS);
            case LAST_3_MONTHS -> now.minus(90, ChronoUnit.DAYS);
            case LAST_YEAR -> now.minus(365, ChronoUnit.DAYS);
        };
        return new DateRange(from, now);
    }

    /**
     * Immutable inclusive-from / exclusive-to instant window. Either bound may be
     * {@code null}, meaning "unbounded" on that side.
     */
    public record DateRange(Instant from, Instant toExclusive) {
    }
}
