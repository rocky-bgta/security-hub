package com.aspire.asat.phishing.exception;

/**
 * Raised when the upstream voice-cloning provider rejects a new clone because
 * the account's custom-voice quota is exhausted (e.g. ElevenLabs
 * {@code voice_limit_reached}). Signals the service layer to reuse a previously
 * cloned voice instead of failing the wizard step.
 */
public class VoiceCloneLimitReachedException extends RuntimeException {

    public VoiceCloneLimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
