package com.aspire.asat.phishing.util;

import com.aspire.asat.common.util.TimezoneResolver;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses campaign schedule date/time strings from the UI into UTC {@link Instant}s.
 * <p>
 * The front end sends {@code timeZoneId}; registration {@code displayName} is resolved in the service layer
 * (e.g. {@code EAT (UTC+03:00)}) and used here for parsing via {@link TimezoneResolver}.
 */
@Component
public class CampaignScheduleDateTimeParser {

    private static final DateTimeFormatter ISO_LOCAL_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter ISO_LOCAL_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter SPACE_SEPARATED_MINUTE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter SPACE_SEPARATED_SECOND =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parses start/end using a resolved {@link ZoneId} (from registration IANA id or display label).
     */
    public ScheduleDateTimeParseResult parseSchedule(String startRaw, String endRaw, ZoneId zone) {
        if (zone == null) {
            throw new PhishingValidationException("Time zone is required for scheduled campaigns");
        }
        return ScheduleDateTimeParseResult.builder()
                .startDateTime(parseWithZone(startRaw, zone))
                .endDateTime(parseWithZone(endRaw, zone))
                .build();
    }

    /**
     * Parses start/end using {@code timeZoneDisplay} and optional registration IANA {@code timezoneId}.
     */
    public ScheduleDateTimeParseResult parseSchedule(
            String startRaw, String endRaw, String timeZoneDisplay, String ianaZoneId) {
        if (timeZoneDisplay == null || timeZoneDisplay.isBlank()) {
            throw new PhishingValidationException("Time zone is required for scheduled campaigns");
        }
        return parseSchedule(startRaw, endRaw, resolveScheduleZone(timeZoneDisplay, ianaZoneId));
    }

    /**
     * Parses start/end using {@code timeZoneDisplay} only (legacy display label from the UI).
     */
    public ScheduleDateTimeParseResult parseSchedule(String startRaw, String endRaw, String timeZoneDisplay) {
        return parseSchedule(startRaw, endRaw, timeZoneDisplay, null);
    }

    /**
     * Resolves the zone to use when converting naive local schedule times to UTC.
     * <p>
     * Registration returns two fields:
     * <ul>
     *   <li>{@code displayName} — descriptive label (e.g. {@code Bangladesh Standard Time})</li>
     *   <li>{@code timezoneId} — offset label (e.g. {@code BST (UTC+06:00)}) or IANA id (e.g. {@code Asia/Dhaka})</li>
     * </ul>
     * Prefer {@code registrationTimezoneId}, then {@code timeZoneDisplay}.
     */
    public ZoneId resolveScheduleZone(String timeZoneDisplay, String registrationTimezoneId) {
        ZoneId zone = TimezoneResolver.tryResolve(registrationTimezoneId);
        if (zone != null) {
            return zone;
        }
        zone = TimezoneResolver.tryResolve(timeZoneDisplay);
        if (zone != null) {
            return zone;
        }
        throw new PhishingValidationException(
                "Invalid time zone. Expected registration timezoneId such as BST (UTC+06:00) "
                        + "or an IANA id such as Asia/Dhaka.");
    }

    /**
     * @param raw             date/time from the client (naive local, or ISO-8601 with offset/Z)
     * @param timeZoneDisplay campaign schedule display label
     * @param ianaZoneId      optional registration IANA zone id
     */
    public Instant parse(String raw, String timeZoneDisplay, String ianaZoneId) {
        if (isBlank(raw)) {
            return null;
        }
        if (timeZoneDisplay == null || timeZoneDisplay.isBlank()) {
            return parseWithZone(raw, ZoneOffset.UTC);
        }
        return parseWithZone(raw, resolveScheduleZone(timeZoneDisplay, ianaZoneId));
    }

    /**
     * @param raw      date/time from the client (naive local, or ISO-8601 with offset/Z)
     * @param timeZone campaign schedule time zone label from the UI
     */
    public Instant parse(String raw, String timeZone) {
        return parse(raw, timeZone, null);
    }

    private Instant parseWithZone(String raw, ZoneId zone) {
        if (isBlank(raw)) {
            return null;
        }
        String trimmed = raw.trim();

        // If the client already sent an instant with Z or numeric offset, honour it as-is.
        if (hasExplicitOffsetOrZ(trimmed)) {
            try {
                return Instant.parse(trimmed);
            } catch (DateTimeParseException ex) {
                throw new PhishingValidationException(
                        "Invalid date/time value: " + trimmed);
            }
        }

        LocalDateTime localDateTime = parseLocalDateTime(trimmed);
        return localDateTime.atZone(zone).toInstant();
    }

    private static boolean hasExplicitOffsetOrZ(String value) {
        return value.endsWith("Z")
                || value.matches(".*[+-]\\d{2}:\\d{2}$")
                || value.matches(".*[+-]\\d{4}$");
    }

    private LocalDateTime parseLocalDateTime(String value) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                ISO_LOCAL_MINUTE,
                ISO_LOCAL_SECOND,
                SPACE_SEPARATED_MINUTE,
                SPACE_SEPARATED_SECOND
        }) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next pattern
            }
        }
        throw new PhishingValidationException(
                "Invalid date/time format. Use yyyy-MM-ddTHH:mm (local time in the selected time zone).");
    }

    /**
     * @deprecated Prefer {@link TimezoneResolver#parseOffsetFromDisplayLabel(String)}.
     */
    @Deprecated
    static ZoneOffset parseOffsetFromDisplayLabel(String label) {
        return TimezoneResolver.parseOffsetFromDisplayLabel(label);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
