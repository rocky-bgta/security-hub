package com.aspire.asat.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DatetimeUtils {
    private DatetimeUtils() {
    }

    /** Email / notification body: {@code 23 August 2026 at 10:26 PM} */
    public static final String EMAIL_DATETIME = "dd MMMM yyyy 'at' hh:mm a";

    /** API / logs: {@code 2026-08-23 22:26:09} */
    public static final String API_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /** Date only: {@code 2026-08-23} */
    public static final String DATE_ONLY = "yyyy-MM-dd";

    private static final DateTimeFormatter EMAIL_FORMATTER =
            DateTimeFormatter.ofPattern(EMAIL_DATETIME, Locale.ENGLISH);
    private static final DateTimeFormatter API_FORMATTER =
            DateTimeFormatter.ofPattern(API_DATETIME, Locale.ENGLISH);
    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_ONLY, Locale.ENGLISH);

    /**
     * Formats an instant in the given zone with a custom pattern (English locale).
     */
    public static String formatInstant(Instant instant, ZoneId zone, String pattern) {
        if (instant == null) {
            return null;
        }
        ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                .withZone(effectiveZone)
                .format(instant);
    }

    /**
     * Formats for email / in-app notification timestamps.
     */
    public static String formatForEmail(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
        return EMAIL_FORMATTER.withZone(effectiveZone).format(instant);
    }

    /**
     * Formats for API / log style timestamps.
     */
    public static String formatForApi(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
        return API_FORMATTER.withZone(effectiveZone).format(instant);
    }

    /**
     * Date-only in the given zone. Prefer this over {@link #getDateOnlyFormat(Instant)}.
     */
    public static String formatDateOnly(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
        return DATE_ONLY_FORMATTER.withZone(effectiveZone).format(instant);
    }

    /**
     * @deprecated Prefer {@link #formatDateOnly(Instant, ZoneId)} with an explicit org/user zone.
     * Uses JVM default zone which is usually UTC on servers and mismatches user-facing times.
     */
    @Deprecated
    public static String getDateOnlyFormat(Instant instant) {
        return formatDateOnly(instant, ZoneId.systemDefault());
    }
}
