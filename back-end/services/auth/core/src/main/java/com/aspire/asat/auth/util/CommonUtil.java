package com.aspire.asat.auth.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CommonUtil {

    /**
     * Converts page number and page size to offset.
     */
    public static int getOffset(int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and pageSize must be > 0");
        }
        return page * pageSize;
    }

    /**
     * Converts ISO datetime string to formatted date (e.g., "dd-MM-yyyy HH:mm").
     */
    public static String formatDate(String isoDateStr, String outputFormat) {
        try {
            Instant instant = Instant.parse(isoDateStr);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern(outputFormat).format(zonedDateTime);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get current timestamp as ISO string.
     */
    public static String getCurrentIsoTimestamp() {
        return Instant.now().toString();
    }

    /**
     * Converts epoch milliseconds to readable date.
     */
    public static String epochToDate(long epochMillis, String format) {
        try {
            return Instant.ofEpochMilli(epochMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates a UUID string.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if a string is null or blank.
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Safely parses string to integer with fallback.
     */
    public static int safeParseInt(String str, int fallback) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Safely parses string to boolean.
     */
    public static boolean safeParseBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }
}
