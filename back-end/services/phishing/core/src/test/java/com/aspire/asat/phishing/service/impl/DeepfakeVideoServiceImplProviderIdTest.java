package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.DeepfakeStep5Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep6Request;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.dto.response.VideoRenderProviderDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapterFactory;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.repository.MicroContentJobRepository;
import com.aspire.asat.phishing.service.DeepfakeRenderService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeVideoServiceImplProviderIdTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private DeepfakeRenderJobRepository renderJobRepository;
    @Mock
    private DeepfakeVoiceCloneRepository voiceCloneRepository;
    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private VideoRenderAdapterFactory videoRenderAdapterFactory;
    @Mock
    private VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private DeepfakeRenderService deepfakeRenderService;
    @Mock
    private ProviderCredentialResolver providerCredentialResolver;
    @Mock
    private MicroContentJobRepository microContentJobRepository;

    @InjectMocks
    private DeepfakeVideoServiceImpl service;

    private DeepfakeRenderJob readyForStep6(UUID renderId) {
        return DeepfakeRenderJob.builder()
                .renderId(renderId)
                .clientId(CLIENT_ID)
                .faceConfirmed(true)
                .script("Hello")
                .audioS3Key("deepfake/audio/a.mp3")
                .currentStep(5)
                .status(DeepfakeJobStatus.DRAFT)
                .build();
    }

    @Test
    void listVideoRenderProviders_MapsHeyGenDisplayName() {
        when(videoRenderAdapterFactory.listImplementedProviders())
                .thenReturn(List.of(VideoRenderProvider.HEYGEN));

        List<VideoRenderProviderDto> result = service.listVideoRenderProviders();

        assertEquals(1, result.size());
        assertEquals(VideoRenderProvider.HEYGEN, result.get(0).getProvider());
        assertEquals("HeyGen", result.get(0).getDisplayName());
    }

    @Test
    void updateStep6_withProviderId_setsProviderModelAndCredentialIdFromDb() {
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = readyForStep6(videoId);
        when(renderJobRepository.findByRenderIdAndClientId(eq(videoId), eq(CLIENT_ID)))
                .thenReturn(Optional.of(job));
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                com.aspire.asat.common.dto.files.CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());

        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-video-1"))
                .thenReturn(ResolvedProviderCredentials.builder()
                        .providerName("HEYGEN")
                        .category(ProviderCategory.VIDEO_RENDERING)
                        .modelName("avatar-v2")
                        .apiKey("hey-key")
                        .build());
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeRenderService.enqueueRender(any(DeepfakeRenderJob.class)))
                .thenReturn(DeepfakeVideoStepResponse.builder()
                        .videoId(videoId.toString())
                        .currentStep(6)
                        .status(DeepfakeJobStatus.PENDING)
                        .build());

        DeepfakeStep6Request request = DeepfakeStep6Request.builder()
                .providerId("pc-video-1")
                .videoProvider(VideoRenderProvider.HEYGEN)
                .model("ignored-model")
                .build();

        service.updateStep6(videoId, request);

        ArgumentCaptor<DeepfakeRenderJob> captor = ArgumentCaptor.forClass(DeepfakeRenderJob.class);
        verify(deepfakeRenderService).enqueueRender(captor.capture());
        DeepfakeRenderJob enqueued = captor.getValue();
        assertEquals(VideoRenderProvider.HEYGEN, enqueued.getVideoProvider());
        assertEquals("avatar-v2", enqueued.getModel());
        assertEquals("pc-video-1", enqueued.getVideoProviderCredentialId());
        verify(providerCredentialResolver).resolveById(CLIENT_ID, "pc-video-1");
    }

    @Test
    void updateStep6_withoutProviderId_requiresVideoProvider() {
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = readyForStep6(videoId);
        when(renderJobRepository.findByRenderIdAndClientId(eq(videoId), eq(CLIENT_ID)))
                .thenReturn(Optional.of(job));
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                com.aspire.asat.common.dto.files.CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStep6(videoId, DeepfakeStep6Request.builder().build()));
        assertTrue(ex.getMessage().contains("videoProvider is required"));
    }

    @Test
    void updateStep6_legacyPath_setsProviderAndClearsCredentialId() {
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = readyForStep6(videoId);
        job.setVideoProviderCredentialId("stale-id");
        when(renderJobRepository.findByRenderIdAndClientId(eq(videoId), eq(CLIENT_ID)))
                .thenReturn(Optional.of(job));
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                com.aspire.asat.common.dto.files.CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeRenderService.enqueueRender(any(DeepfakeRenderJob.class)))
                .thenReturn(DeepfakeVideoStepResponse.builder()
                        .videoId(videoId.toString())
                        .currentStep(6)
                        .status(DeepfakeJobStatus.PENDING)
                        .build());

        service.updateStep6(videoId, DeepfakeStep6Request.builder()
                .videoProvider(VideoRenderProvider.HEYGEN)
                .model("custom-model")
                .build());

        ArgumentCaptor<DeepfakeRenderJob> captor = ArgumentCaptor.forClass(DeepfakeRenderJob.class);
        verify(deepfakeRenderService).enqueueRender(captor.capture());
        assertEquals(VideoRenderProvider.HEYGEN, captor.getValue().getVideoProvider());
        assertEquals("custom-model", captor.getValue().getModel());
        assertNull(captor.getValue().getVideoProviderCredentialId());
        verify(providerCredentialResolver, never()).resolveById(any(), any());
    }

    @Test
    void updateStep5_usesResolveByIdWhenVoiceCloneHasCredentialId() {
        UUID videoId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeRenderJob job = DeepfakeRenderJob.builder()
                .renderId(videoId)
                .clientId(CLIENT_ID)
                .faceConfirmed(true)
                .voiceCloneId(voiceCloneId.toString())
                .language("en")
                .currentStep(4)
                .status(DeepfakeJobStatus.DRAFT)
                .build();
        when(renderJobRepository.findByRenderIdAndClientId(eq(videoId), eq(CLIENT_ID)))
                .thenReturn(Optional.of(job));
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                com.aspire.asat.common.dto.files.CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());

        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .providerCredentialId("pc-voice-1")
                .externalVoiceId("ext-1")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder().apiKey("k").build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-voice-1")).thenReturn(credentials);
        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        when(adapter.synthesize("ext-1", "Hi there", "en", credentials)).thenReturn(new byte[]{1, 2});
        when(deepfakeS3Service.uploadBytes(any(), eq("deepfake/audio"), eq("mp3"), eq("audio/mpeg")))
                .thenReturn("deepfake/audio/out.mp3");
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateStep5(videoId, DeepfakeStep5Request.builder().script("Hi there").build());

        verify(providerCredentialResolver).resolveById(CLIENT_ID, "pc-voice-1");
        verify(providerCredentialResolver, never()).resolve(any(), any());
        verify(adapter).synthesize("ext-1", "Hi there", "en", credentials);
    }
}
