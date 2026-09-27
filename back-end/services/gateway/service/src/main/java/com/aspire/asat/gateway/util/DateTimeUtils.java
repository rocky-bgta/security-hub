package com.aspire.asat.gateway.util;

import java.util.Calendar;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static int convertToMilli(int minute, int calenderFlag) {
        if (Calendar.HOUR == calenderFlag) return 1000 * 60 * 60 * minute;
        if (Calendar.MINUTE == calenderFlag) return 1000 * 60 * minute;
        return 0;
    }
}
