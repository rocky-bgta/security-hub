package com.aspire.asat.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatetimeUtilsTest {

    private static final Instant SAMPLE = Instant.parse("2026-08-23T14:26:09Z");

    @Test
    void formatForEmailInUtc() {
        assertEquals("23 August 2026 at 02:26 PM",
                DatetimeUtils.formatForEmail(SAMPLE, ZoneOffset.UTC));
    }

    @Test
    void formatForEmailInPlusEight() {
        assertEquals("23 August 2026 at 10:26 PM",
                DatetimeUtils.formatForEmail(SAMPLE, ZoneOffset.of("+08:00")));
    }

    @Test
    void formatForApi() {
        assertEquals("2026-08-23 15:26:09",
                DatetimeUtils.formatForApi(SAMPLE, ZoneOffset.of("+01:00")));
    }

    @Test
    void formatDateOnly() {
        assertEquals("2026-08-23", DatetimeUtils.formatDateOnly(SAMPLE, ZoneOffset.UTC));
    }

    @Test
    void nullInstantReturnsNull() {
        assertNull(DatetimeUtils.formatForEmail(null, ZoneOffset.UTC));
        assertNull(DatetimeUtils.formatForApi(null, ZoneOffset.UTC));
        assertNull(DatetimeUtils.formatDateOnly(null, ZoneOffset.UTC));
    }

    @Test
    void nullZoneDefaultsToUtc() {
        assertEquals("23 August 2026 at 02:26 PM", DatetimeUtils.formatForEmail(SAMPLE, null));
    }
}
