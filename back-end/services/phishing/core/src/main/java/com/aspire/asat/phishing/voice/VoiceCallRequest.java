package com.aspire.asat.phishing.voice;

import lombok.Builder;

@Builder
public record VoiceCallRequest(
        String toPhone,
        String scriptBody,
        String callerId,
        String externalVoiceId,
        String trackingId,
        String twimlUrl,
        String statusCallbackUrl
) {
}
