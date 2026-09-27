package com.aspire.asat.phishing.media.adapter.impl;

import com.aspire.asat.phishing.client.FishAudioClient;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FishAudioVoiceCloneAdapterTest {

    @Mock
    private FishAudioClient fishAudioClient;

    @InjectMocks
    private FishAudioVoiceCloneAdapter adapter;

    @Test
    void deleteClone_delegatesToFishAudioClient() {
        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder()
                .apiKey("key")
                .build();

        adapter.deleteClone("model-abc", credentials);

        verify(fishAudioClient).deleteModel("model-abc", credentials);
    }
}
