package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.enums.QuickRange;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Resolves {@link QuickRange} values into start/end date strings (yyyy-MM-dd).
 * When quickRange is null, explicit dates are returned unchanged.
 */
public final class QuickRangeResolver {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private QuickRangeResolver() {
    }

    /**
     * @return String[2] where [0] = startDate, [1] = endDate
     */
    public static String[] resolve(QuickRange quickRange, String startDate, String endDate) {
        if (quickRange == null) {
            return new String[]{startDate, endDate};
        }

        LocalDate today = LocalDate.now();
        LocalDate start = switch (quickRange) {
            case LAST_7_DAYS -> today.minusDays(7);
            case LAST_30_DAYS -> today.minusDays(30);
            case LAST_3_MONTHS -> today.minusMonths(3);
            case LAST_YEAR -> today.minusYears(1);
        };

        return new String[]{start.format(DATE_FORMATTER), today.format(DATE_FORMATTER)};
    }
}
