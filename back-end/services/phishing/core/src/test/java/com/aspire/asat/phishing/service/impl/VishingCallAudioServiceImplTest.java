package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.VishingAudioCache;
import com.aspire.asat.phishing.repository.VishingAudioCacheRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VishingCallAudioServiceImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String VOICE_ID = "voice-abc";
    private static final String SCRIPT = "Hello {{FIRST_NAME}}, this is IT support.";
    private static final String S3_KEY = "vishing/audio/generated.mp3";

    @Mock
    private VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    @Mock
    private ProviderCredentialResolver providerCredentialResolver;
    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private VishingAudioCacheRepository audioCacheRepository;
    @Mock
    private VoiceCloneAdapter voiceCloneAdapter;

    @InjectMocks
    private VishingCallAudioServiceImpl service;

    @Test
    void synthesize_cacheMiss_synthesizesUploadsAndCaches() {
        when(audioCacheRepository.findByCacheKey(anyString())).thenReturn(Optional.empty());
        when(providerCredentialResolver.resolve(CLIENT_ID, VoiceCloneProvider.ELEVENLABS.name()))
                .thenReturn(new ResolvedProviderCredentials());
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.synthesize(eq(VOICE_ID), eq(SCRIPT), eq("en"), any()))
                .thenReturn(new byte[]{1, 2, 3});
        when(deepfakeS3Service.uploadBytes(any(), eq("vishing/audio"), eq("mp3"), eq("audio/mpeg")))
                .thenReturn(S3_KEY);

        String result = service.synthesizeForCall(CLIENT_ID, VoiceCloneProvider.ELEVENLABS, VOICE_ID, SCRIPT, "en");

        assertEquals(S3_KEY, result);
        verify(deepfakeS3Service).uploadBytes(any(), eq("vishing/audio"), eq("mp3"), eq("audio/mpeg"));
        verify(audioCacheRepository).save(any(VishingAudioCache.class));
    }

    @Test
    void synthesize_cacheHit_returnsCachedKeyWithoutSynthesis() {
        when(audioCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.of(VishingAudioCache.builder().s3Key(S3_KEY).build()));

        String result = service.synthesizeForCall(CLIENT_ID, VoiceCloneProvider.ELEVENLABS, VOICE_ID, SCRIPT, "en");

        assertEquals(S3_KEY, result);
        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
        verify(deepfakeS3Service, never()).uploadBytes(any(), anyString(), anyString(), anyString());
        verify(audioCacheRepository, never()).save(any());
    }

    @Test
    void synthesize_missingVoiceId_throwsBadRequest() {
        assertThrows(ServiceException.class, () ->
                service.synthesizeForCall(CLIENT_ID, VoiceCloneProvider.ELEVENLABS, "  ", SCRIPT, "en"));
        verify(audioCacheRepository, never()).findByCacheKey(anyString());
    }

    @Test
    void synthesize_emptyAudio_throwsBadGateway() {
        when(audioCacheRepository.findByCacheKey(anyString())).thenReturn(Optional.empty());
        when(providerCredentialResolver.resolve(anyString(), anyString()))
                .thenReturn(new ResolvedProviderCredentials());
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.synthesize(anyString(), anyString(), anyString(), any()))
                .thenReturn(new byte[0]);

        assertThrows(ServiceException.class, () ->
                service.synthesizeForCall(CLIENT_ID, VoiceCloneProvider.ELEVENLABS, VOICE_ID, SCRIPT, "en"));
        verify(deepfakeS3Service, never()).uploadBytes(any(), anyString(), anyString(), anyString());
    }
}
