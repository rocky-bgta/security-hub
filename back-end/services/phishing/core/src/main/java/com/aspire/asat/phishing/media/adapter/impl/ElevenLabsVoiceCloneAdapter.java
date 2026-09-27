package com.aspire.asat.phishing.media.adapter.impl;

import com.aspire.asat.phishing.client.ElevenLabsClient;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsVoiceCloneAdapter implements VoiceCloneAdapter {

    private final ElevenLabsClient elevenLabsClient;

    @Override
    public VoiceCloneProvider getProvider() {
        return VoiceCloneProvider.ELEVENLABS;
    }

    @Override
    public String createClone(File sampleFile, String name, String language, ResolvedProviderCredentials credentials) {
        return elevenLabsClient.addVoice(sampleFile, name, credentials);
    }

    @Override
    public byte[] synthesize(String externalVoiceId, String text, String language, ResolvedProviderCredentials credentials) {
        return elevenLabsClient.textToSpeech(externalVoiceId, text, language, credentials);
    }

    @Override
    public void deleteClone(String externalVoiceId, ResolvedProviderCredentials credentials) {
        elevenLabsClient.deleteVoice(externalVoiceId, credentials);
    }
}
