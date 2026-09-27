package com.aspire.asat.phishing.voice;

public record VoiceCallResult(boolean success, String callId, String errorMessage) {

    public static VoiceCallResult ok(String callId) {
        return new VoiceCallResult(true, callId, null);
    }

    public static VoiceCallResult fail(String errorMessage) {
        return new VoiceCallResult(false, null, errorMessage);
    }
}
