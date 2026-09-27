package com.aspire.asat.registration.utils;

/**
 * Shared helpers for building UTF-8 CSV payloads that render correctly in Excel.
 * Centralises the BOM prefix and RFC-4180 field escaping that were previously
 * duplicated across report services.
 */
public final class CsvUtil {

    /** UTF-8 byte order mark; prepended so Excel detects the encoding. */
    private static final char BOM = '\uFEFF';

    private CsvUtil() {
    }

    /**
     * @return a new {@link StringBuilder} pre-seeded with the UTF-8 BOM, ready for header + rows
     */
    public static StringBuilder newCsv() {
        return new StringBuilder().append(BOM);
    }

    /**
     * Escape a single CSV field: wraps in double quotes and doubles any embedded
     * quotes when the value contains a comma, quote or newline.
     *
     * @param value raw field value (nullable)
     * @return escaped field, or an empty string when {@code value} is null
     */
    public static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
