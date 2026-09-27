package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;

/**
 * Synthesizes the rendered vishing script into cloned-voice audio and stores it
 * in S3 for playback during a Twilio call.
 */
public interface VishingCallAudioService {

    /**
     * Synthesizes {@code renderedScript} using the campaign's cloned voice, uploads
     * the resulting audio to S3, and returns the S3 key. Identical requests
     * (same voice, language, and text) reuse a previously synthesized asset.
     *
     * @param clientId        owning client (for provider credential resolution)
     * @param provider        voice cloning engine (e.g. ELEVENLABS / FISH_AUDIO)
     * @param externalVoiceId provider-side cloned voice id
     * @param renderedScript  placeholder-substituted script text
     * @param language        BCP-47/provider language hint (defaults to {@code en})
     * @return the S3 key of the synthesized audio
     */
    String synthesizeForCall(String clientId,
                             VoiceCloneProvider provider,
                             String externalVoiceId,
                             String renderedScript,
                             String language);
}
