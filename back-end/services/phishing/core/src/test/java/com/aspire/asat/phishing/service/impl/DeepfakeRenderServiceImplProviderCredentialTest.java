package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapter;
import com.aspire.asat.phishing.media.adapter.VideoRenderAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.media.model.VideoRenderInput;
import com.aspire.asat.phishing.media.model.VideoRenderResult;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeSqsService;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeRenderServiceImplProviderCredentialTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private DeepfakeRenderJobRepository renderJobRepository;
    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private DeepfakeSqsService deepfakeSqsService;
    @Mock
    private VideoRenderAdapterFactory videoRenderAdapterFactory;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ProviderCredentialResolver providerCredentialResolver;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DeepfakeRenderServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new DeepfakeRenderServiceImpl(
                renderJobRepository,
                deepfakeS3Service,
                deepfakeSqsService,
                videoRenderAdapterFactory,
                userCurrentContextService,
                providerCredentialResolver,
                objectMapper);
        setField(service, "maxAttempts", 1);
        setField(service, "backoffMs", 1L);
        setField(service, "staleProcessingMinutes", 30L);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void processRender_withVideoProviderCredentialId_usesResolveById() throws Exception {
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = DeepfakeRenderJob.builder()
                .renderId(renderId)
                .clientId(CLIENT_ID)
                .faceKey("deepfake/face/f.png")
                .faceConfirmed(true)
                .script("Hello")
                .audioS3Key("deepfake/audio/a.mp3")
                .backgroundType(DeepfakeBackgroundType.PRESET)
                .videoProvider(VideoRenderProvider.HEYGEN)
                .videoProviderCredentialId("pc-video-1")
                .model("avatar-v2")
                .currentStep(6)
                .status(DeepfakeJobStatus.PENDING)
                .build();
        when(renderJobRepository.findByRenderId(renderId)).thenReturn(Optional.of(job));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));

        File face = File.createTempFile("face", ".png");
        File audio = File.createTempFile("audio", ".mp3");
        face.deleteOnExit();
        audio.deleteOnExit();
        when(deepfakeS3Service.downloadToTemp("deepfake/face/f.png")).thenReturn(face);
        when(deepfakeS3Service.downloadToTemp("deepfake/audio/a.mp3")).thenReturn(audio);
        when(deepfakeS3Service.presignHeyGenCompatibleFaceUrl("deepfake/face/f.png")).thenReturn("https://face");
        when(deepfakeS3Service.presignGetUrl("deepfake/audio/a.mp3")).thenReturn("https://audio");

        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder()
                .providerName("HEYGEN")
                .apiKey("hey-key")
                .modelName("avatar-v2")
                .build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-video-1")).thenReturn(credentials);

        VideoRenderAdapter adapter = mock(VideoRenderAdapter.class);
        when(videoRenderAdapterFactory.getAdapter(VideoRenderProvider.HEYGEN)).thenReturn(adapter);
        when(adapter.render(any(VideoRenderInput.class))).thenReturn(VideoRenderResult.builder()
                .videoBytes(new byte[]{1, 2, 3})
                .build());
        when(deepfakeS3Service.uploadBytes(any(), any(), any(), any())).thenReturn("deepfake/video/out.mp4");

        String body = "{\"renderId\":\"" + renderId + "\",\"bucket\":\"bucket\"}";
        Message message = Message.builder().body(body).build();

        assertTrue(service.processRender(message));

        verify(providerCredentialResolver).resolveById(CLIENT_ID, "pc-video-1");
        verify(providerCredentialResolver, never()).resolve(any(), any());
        ArgumentCaptor<VideoRenderInput> inputCaptor = ArgumentCaptor.forClass(VideoRenderInput.class);
        verify(adapter).render(inputCaptor.capture());
        assertEquals(credentials, inputCaptor.getValue().getCredentials());
        assertEquals("avatar-v2", inputCaptor.getValue().getModel());
    }
}
