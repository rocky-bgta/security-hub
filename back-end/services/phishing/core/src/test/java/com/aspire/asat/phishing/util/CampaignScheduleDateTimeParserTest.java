package com.aspire.asat.phishing.util;

import com.aspire.asat.phishing.exception.PhishingValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CampaignScheduleDateTimeParserTest {

    private static final String EAT_LABEL = "EAT (UTC+03:00)";

    private final CampaignScheduleDateTimeParser parser = new CampaignScheduleDateTimeParser();

    @Test
    void parseScheduleMatchesFrontEndPayload() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15T21:25",
                "",
                EAT_LABEL);

        assertEquals(Instant.parse("2026-05-15T18:25:00Z"), result.getStartDateTime());
        assertNull(result.getEndDateTime());
    }

    @ParameterizedTest
    @CsvSource({
            "EAT (UTC+03:00), +03:00",
            "UTC+03:00, +03:00",
            "GMT-05:30, -05:30",
            "Pacific (UTC+08:00), +08:00",
            "UTC+3:00, +03:00",
            "UTC+03, +03:00"
    })
    void parseOffsetFromDisplayLabelSupportsUiFormats(String label, String expectedOffset) {
        assertEquals(ZoneOffset.of(expectedOffset),
                CampaignScheduleDateTimeParser.parseOffsetFromDisplayLabel(label));
    }

    @Test
    void parseScheduleTreatsLocalTimeInSelectedZone() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15T12:00",
                null,
                "UTC-05:00");

        assertEquals(Instant.parse("2026-05-15T17:00:00Z"), result.getStartDateTime());
    }

    @Test
    void parseScheduleAcceptsSpaceSeparatedLocalDateTime() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15 21:25",
                null,
                EAT_LABEL);

        assertEquals(Instant.parse("2026-05-15T18:25:00Z"), result.getStartDateTime());
    }

    @Test
    void parseScheduleAcceptsLocalDateTimeWithSeconds() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15T21:25:00",
                null,
                EAT_LABEL);

        assertEquals(Instant.parse("2026-05-15T18:25:00Z"), result.getStartDateTime());
    }

    @Test
    void parseHonoursIsoInstantWhenClientSendsOffset() {
        Instant explicit = parser.parse("2026-05-15T21:25:00+03:00", EAT_LABEL);
        assertEquals(Instant.parse("2026-05-15T18:25:00Z"), explicit);
    }

    @Test
    void parseScheduleRequiresTimeZone() {
        assertThrows(PhishingValidationException.class,
                () -> parser.parseSchedule("2026-05-15T21:25", null, (String) null, null));
    }

    @Test
    void parseScheduleUsesOffsetFromRegistrationTimezoneIdWhenDisplayNameHasNoOffset() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15T12:00",
                null,
                "Bangladesh Standard Time",
                "BST (UTC+06:00)");

        assertEquals(Instant.parse("2026-05-15T06:00:00Z"), result.getStartDateTime());
    }

    @Test
    void parseScheduleUsesIanaZoneWhenDisplayNameHasNoOffset() {
        ScheduleDateTimeParseResult result = parser.parseSchedule(
                "2026-05-15T21:25",
                null,
                "East Africa Time",
                "Africa/Nairobi");

        assertEquals(Instant.parse("2026-05-15T18:25:00Z"), result.getStartDateTime());
    }

    @Test
    void parseScheduleRejectsLabelWithoutParsableOffset() {
        assertThrows(PhishingValidationException.class,
                () -> parser.parseSchedule("2026-05-15T21:25", null, "Eastern Africa Time"));
    }

    @Test
    void parseScheduleRejectsInvalidLocalDateTime() {
        assertThrows(PhishingValidationException.class,
                () -> parser.parseSchedule("15/05/2026 21:25", null, EAT_LABEL));
    }
}
