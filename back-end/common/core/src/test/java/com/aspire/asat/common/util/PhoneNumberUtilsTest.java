package com.aspire.asat.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhoneNumberUtilsTest {

    @Test
    void normalize_AlreadyE164_Unchanged() {
        assertEquals("+8801773126589", PhoneNumberUtils.normalize("+8801773126589"));
    }

    @Test
    void normalize_BangladeshTrunkZeroAfterCountryCode_StripsExtraZero() {
        assertEquals("+8801773126589", PhoneNumberUtils.normalize("+88001773126589"));
        assertEquals("+8801773126589", PhoneNumberUtils.normalize("88001773126589"));
    }

    @Test
    void normalize_LocalBangladeshNumberStartingWithZero_AddsCountryCode() {
        assertEquals("+8801773126589", PhoneNumberUtils.normalize("01773126589"));
    }

    @Test
    void normalize_NullOrBlank_ReturnsNull() {
        assertNull(PhoneNumberUtils.normalize(null));
        assertNull(PhoneNumberUtils.normalize("   "));
    }
}
