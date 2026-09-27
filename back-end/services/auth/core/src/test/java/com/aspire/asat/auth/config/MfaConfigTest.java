package com.aspire.asat.auth.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MfaConfigTest {

    @Test
    void cooldownSecondsForCompletedResends_UsesProgressiveLadder() {
        MfaConfig config = new MfaConfig();

        assertEquals(60, config.cooldownSecondsForCompletedResends(0));
        assertEquals(120, config.cooldownSecondsForCompletedResends(1));
        assertEquals(300, config.cooldownSecondsForCompletedResends(2));
        assertEquals(300, config.cooldownSecondsForCompletedResends(5));
    }

    @Test
    void cooldownSecondsForCompletedResends_EmptyList_DefaultsTo60() {
        MfaConfig config = new MfaConfig();
        config.setResendCooldownSeconds(new ArrayList<>());

        assertEquals(60, config.cooldownSecondsForCompletedResends(0));
    }

    @Test
    void cooldownSecondsForCompletedResends_NullList_DefaultsTo60() {
        MfaConfig config = new MfaConfig();
        config.setResendCooldownSeconds(null);

        assertEquals(60, config.cooldownSecondsForCompletedResends(3));
    }
}
