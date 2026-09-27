package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;

import java.util.List;
import java.util.Map;

/**
 * Result of interpreting a Twilio {@code <Gather>} response into a vishing outcome.
 */
public record VishingOutcomeResolution(
        VishingCallOutcome outcome,
        Map<String, Object> sensitiveDataCaptured,
        List<String> detectedKeywords
) {
}
