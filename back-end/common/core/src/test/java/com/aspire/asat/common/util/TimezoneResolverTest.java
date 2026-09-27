package com.aspire.asat.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TimezoneResolverTest {

    @ParameterizedTest
    @CsvSource({
            "WAT (UTC+01:00), +01:00",
            "EAT (UTC+03:00), +03:00",
            "UTC+03:00, +03:00",
            "GMT-05:30, -05:30",
            "Pacific (UTC+08:00), +08:00",
            "UTC+3:00, +03:00",
            "UTC+03, +03:00",
            "SGT (UTC+08:00), +08:00"
    })
    void parseOffsetFromDisplayLabelSupportsUiFormats(String label, String expectedOffset) {
        assertEquals(ZoneOffset.of(expectedOffset), TimezoneResolver.parseOffsetFromDisplayLabel(label));
    }

    @Test
    void resolveUsesOffsetLabel() {
        ZoneId zone = TimezoneResolver.resolve("WAT (UTC+01:00)", "West Africa Time");
        assertEquals(ZoneOffset.of("+01:00"), zone);
    }

    @Test
    void resolveFallsBackToUtcWhenUnresolved() {
        assertEquals(ZoneOffset.UTC, TimezoneResolver.resolve("Eastern Africa Time", null));
        assertEquals(ZoneOffset.UTC, TimezoneResolver.resolve((String) null));
    }

    @Test
    void tryResolveReturnsNullForBlankOrUnknown() {
        assertNull(TimezoneResolver.tryResolve(null));
        assertNull(TimezoneResolver.tryResolve(""));
        assertNull(TimezoneResolver.tryResolve("Eastern Africa Time"));
    }

    @Test
    void resolveIanaDirectly() {
        assertEquals(ZoneId.of("Asia/Dhaka"), TimezoneResolver.resolve("Asia/Dhaka"));
    }

    @Test
    void formatEmailUsesResolvedOffset() {
        Instant instant = Instant.parse("2026-08-23T14:26:09Z");
        ZoneId zone = TimezoneResolver.resolve("SGT (UTC+08:00)");
        assertEquals("23 August 2026 at 10:26 PM", DatetimeUtils.formatForEmail(instant, zone));
    }
}
