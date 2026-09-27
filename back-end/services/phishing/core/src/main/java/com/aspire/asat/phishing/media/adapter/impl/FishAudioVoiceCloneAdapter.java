package com.aspire.asat.phishing.media.adapter.impl;

import com.aspire.asat.phishing.client.FishAudioClient;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class FishAudioVoiceCloneAdapter implements VoiceCloneAdapter {

    private final FishAudioClient fishAudioClient;

    @Override
    public VoiceCloneProvider getProvider() {
        return VoiceCloneProvider.FISH_AUDIO;
    }

    @Override
    public String createClone(File sampleFile, String name, String language, ResolvedProviderCredentials credentials) {
        return fishAudioClient.createModel(sampleFile, name, credentials);
    }

    @Override
    public byte[] synthesize(String externalVoiceId, String text, String language, ResolvedProviderCredentials credentials) {
        return fishAudioClient.textToSpeech(externalVoiceId, text, credentials);
    }

    @Override
    public void deleteClone(String externalVoiceId, ResolvedProviderCredentials credentials) {
        fishAudioClient.deleteModel(externalVoiceId, credentials);
    }
}
