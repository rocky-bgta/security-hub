package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VishingOutcomeResolverTest {

    private final VishingOutcomeResolver resolver = new VishingOutcomeResolver();

    @Test
    void dtmfDigits_yieldCompromisedWithCapturedDigits() {
        VishingOutcomeResolution result = resolver.resolve("1234", null, VishingInteractionMode.BOTH, List.of());

        assertEquals(VishingCallOutcome.COMPROMISED, result.outcome());
        assertEquals("1234", result.sensitiveDataCaptured().get("dtmfDigits"));
    }

    @Test
    void speechMatchingKeyword_yieldsCompromisedWithDetectedKeyword() {
        VishingOutcomeResolution result = resolver.resolve(
                null, "my password is hunter2", VishingInteractionMode.SPEECH, List.of("password"));

        assertEquals(VishingCallOutcome.COMPROMISED, result.outcome());
        assertTrue(result.detectedKeywords().contains("password"));
    }

    @Test
    void speechWithoutKeyword_yieldsEngaged() {
        VishingOutcomeResolution result = resolver.resolve(
                null, "who is calling", VishingInteractionMode.SPEECH, List.of("password"));

        assertEquals(VishingCallOutcome.ENGAGED, result.outcome());
    }

    @Test
    void noInput_yieldsAnswered() {
        VishingOutcomeResolution result = resolver.resolve(null, null, VishingInteractionMode.BOTH, List.of());

        assertEquals(VishingCallOutcome.ANSWERED, result.outcome());
    }

    @Test
    void speechModeIgnoresDtmfDigits() {
        VishingOutcomeResolution result = resolver.resolve("999", null, VishingInteractionMode.SPEECH, List.of());

        assertEquals(VishingCallOutcome.ANSWERED, result.outcome());
    }

    @Test
    void dtmfModeIgnoresSpeech() {
        VishingOutcomeResolution result = resolver.resolve(
                null, "password", VishingInteractionMode.DTMF, List.of("password"));

        assertEquals(VishingCallOutcome.ANSWERED, result.outcome());
    }
}
