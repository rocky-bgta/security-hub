package com.aspire.asat.common.util;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves registration / UI timezone labels into a usable {@link ZoneId}.
 * <p>
 * Supports:
 * <ul>
 *   <li>IANA ids — {@code Asia/Dhaka}, {@code Africa/Lagos}</li>
 *   <li>Offset labels — {@code WAT (UTC+01:00)}, {@code UTC+08:00}, {@code GMT-05:30}</li>
 *   <li>{@code UTC} / {@code GMT}</li>
 * </ul>
 * Resolution preference when multiple fields are supplied:
 * {@code timezoneId} (label) → {@code displayName} → UTC.
 */
public final class TimezoneResolver {

    private static final Pattern OFFSET_IN_TIME_ZONE_LABEL = Pattern.compile(
            "(?:UTC|GMT)\\s*([+-])\\s*(\\d{1,2})(?::(\\d{2}))?",
            Pattern.CASE_INSENSITIVE);

    private TimezoneResolver() {
    }

    /**
     * Resolves a zone from registration {@code timezoneId} label and optional display name.
     * Falls back to UTC when nothing can be parsed.
     */
    public static ZoneId resolve(String timezoneId, String displayName) {
        ZoneId zone = tryResolve(timezoneId);
        if (zone != null) {
            return zone;
        }
        zone = tryResolve(displayName);
        if (zone != null) {
            return zone;
        }
        return ZoneOffset.UTC;
    }

    /**
     * Resolves from a single registration {@code timezoneId} or offset / IANA label.
     */
    public static ZoneId resolve(String timezoneIdOrLabel) {
        return resolve(timezoneIdOrLabel, null);
    }

    /**
     * Attempts to resolve a single value; returns {@code null} when unrecognised.
     */
    public static ZoneId tryResolve(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String ref = value.trim();
        if ("UTC".equalsIgnoreCase(ref) || "GMT".equalsIgnoreCase(ref)) {
            return ZoneOffset.UTC;
        }

        ZoneOffset offsetFromLabel = parseOffsetFromDisplayLabel(ref);
        if (offsetFromLabel != null) {
            return offsetFromLabel;
        }

        if (looksLikeIanaZoneId(ref)) {
            try {
                return ZoneId.of(ref);
            } catch (DateTimeException ex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Extracts a fixed offset from strings like {@code WAT (UTC+01:00)}, {@code UTC+03:00}, {@code GMT-5:30}.
     */
    public static ZoneOffset parseOffsetFromDisplayLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        Matcher matcher = OFFSET_IN_TIME_ZONE_LABEL.matcher(label);
        if (!matcher.find()) {
            return null;
        }
        String sign = matcher.group(1);
        int hours = Integer.parseInt(matcher.group(2));
        String minutesGroup = matcher.group(3);
        int minutes = minutesGroup != null ? Integer.parseInt(minutesGroup) : 0;
        if (hours > 18 || minutes > 59) {
            return null;
        }
        int totalSeconds = hours * 3600 + minutes * 60;
        if ("-".equals(sign)) {
            totalSeconds = -totalSeconds;
        }
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    private static boolean looksLikeIanaZoneId(String ref) {
        return ref.contains("/") && !ref.contains(" ");
    }
}
