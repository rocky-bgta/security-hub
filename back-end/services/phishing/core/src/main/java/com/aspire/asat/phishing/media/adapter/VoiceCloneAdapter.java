package com.aspire.asat.phishing.media.adapter;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;

import java.io.File;

/**
 * Provider-agnostic contract for voice cloning + speech synthesis.
 */
public interface VoiceCloneAdapter {

    VoiceCloneProvider getProvider();

    /**
     * Creates a one-shot voice clone from a sample recording.
     *
     * @param credentials resolved provider credentials for this call
     * @return provider-side voice identifier used for later synthesis
     */
    String createClone(File sampleFile, String name, String language, ResolvedProviderCredentials credentials);

    /**
     * Synthesizes speech for the given text using a previously cloned voice.
     *
     * @param credentials resolved provider credentials for this call
     * @return audio bytes (typically MP3)
     */
    byte[] synthesize(String externalVoiceId, String text, String language, ResolvedProviderCredentials credentials);

    /**
     * Deletes a previously cloned voice on the provider side.
     *
     * @param credentials resolved provider credentials for this call
     */
    void deleteClone(String externalVoiceId, ResolvedProviderCredentials credentials);
}
