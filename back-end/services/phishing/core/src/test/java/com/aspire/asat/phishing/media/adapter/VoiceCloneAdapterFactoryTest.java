package com.aspire.asat.phishing.media.adapter;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceCloneAdapterFactoryTest {

    @Mock
    private VoiceCloneAdapter elevenLabsAdapter;
    @Mock
    private VoiceCloneAdapter fishAudioAdapter;

    private VoiceCloneAdapterFactory factory;

    @BeforeEach
    void setUp() {
        lenient().when(elevenLabsAdapter.getProvider()).thenReturn(VoiceCloneProvider.ELEVENLABS);
        lenient().when(fishAudioAdapter.getProvider()).thenReturn(VoiceCloneProvider.FISH_AUDIO);
        factory = new VoiceCloneAdapterFactory(List.of(elevenLabsAdapter, fishAudioAdapter));
    }

    @Test
    void getAdapterForBaseUrl_ElevenLabsApiHost_ReturnsElevenLabsAdapter() {
        assertEquals(elevenLabsAdapter, factory.getAdapterForBaseUrl("https://api.elevenlabs.io"));
        assertEquals(VoiceCloneProvider.ELEVENLABS,
                VoiceCloneAdapterFactory.resolveProviderFromBaseUrl("https://api.elevenlabs.io"));
    }

    @Test
    void getAdapterForBaseUrl_FishAudioHosts_ReturnsFishAudioAdapter() {
        assertEquals(fishAudioAdapter, factory.getAdapterForBaseUrl("https://fish.audio"));
        assertEquals(fishAudioAdapter, factory.getAdapterForBaseUrl("https://api.fish.audio"));
        assertEquals(VoiceCloneProvider.FISH_AUDIO,
                VoiceCloneAdapterFactory.resolveProviderFromBaseUrl("https://api.fish.audio"));
    }

    @Test
    void resolveProviderFromBaseUrl_BlankUrl_Throws() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> VoiceCloneAdapterFactory.resolveProviderFromBaseUrl("  "));
        assertTrue(ex.getMessage().contains("Base URL is required"));
    }

    @Test
    void resolveProviderFromBaseUrl_UnknownHost_Throws() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> VoiceCloneAdapterFactory.resolveProviderFromBaseUrl("https://example.com/v1"));
        assertTrue(ex.getMessage().contains("ElevenLabs or Fish Audio"));
    }
}
