package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.UploadToContentStatus;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDto;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.MicroContentJob;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.repository.MicroContentJobRepository;
import com.aspire.asat.phishing.service.DeepfakeRenderService;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeVideoServiceImplListTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private DeepfakeRenderJobRepository renderJobRepository;
    @Mock
    private DeepfakeVoiceCloneRepository voiceCloneRepository;
    @Mock
    private DeepfakeS3Service deepfakeS3Service;
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

    private void stubClient() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    private DeepfakeRenderJob job(UUID renderId) {
        return DeepfakeRenderJob.builder()
                .renderId(renderId)
                .clientId(CLIENT_ID)
                .title("Video")
                .status(DeepfakeJobStatus.DRAFT)
                .createdAt(Instant.now())
                .build();
    }

    private MicroContentJob microContentJob(String deepfakeVideoId, DeepfakeJobStatus status) {
        return MicroContentJob.builder()
                .clientId(CLIENT_ID)
                .status(status)
                .videos(List.of(MicroContentJob.VideoItem.builder()
                        .deepfakeVideoId(deepfakeVideoId)
                        .build()))
                .build();
    }

    @Test
    void listVideos_completedMicroContent_mapsToDone() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId);
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of(job));
        when(microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(eq(CLIENT_ID), anyCollection()))
                .thenReturn(List.of(microContentJob(renderId.toString(), DeepfakeJobStatus.COMPLETED)));

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(1, result.size());
        assertEquals(UploadToContentStatus.DONE, result.get(0).getUploadToContent());
    }

    @Test
    void listVideos_ongoingMicroContent_mapsToProcessing() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId);
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of(job));
        when(microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(eq(CLIENT_ID), anyCollection()))
                .thenReturn(List.of(microContentJob(renderId.toString(), DeepfakeJobStatus.PROCESSING)));

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(UploadToContentStatus.PROCESSING, result.get(0).getUploadToContent());
    }

    @Test
    void listVideos_noMicroContent_defaultsToPending() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId);
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of(job));
        when(microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(eq(CLIENT_ID), anyCollection()))
                .thenReturn(List.of());

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(UploadToContentStatus.PENDING, result.get(0).getUploadToContent());
    }

    @Test
    void listVideos_deletedMicroContent_mapsToPending() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId);
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of(job));
        when(microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(eq(CLIENT_ID), anyCollection()))
                .thenReturn(List.of(microContentJob(renderId.toString(), DeepfakeJobStatus.DELETED)));

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(UploadToContentStatus.PENDING, result.get(0).getUploadToContent());
    }

    @Test
    void listVideos_failedMicroContent_mapsToPending() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId);
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of(job));
        when(microContentJobRepository.findByClientIdAndVideosDeepfakeVideoIdIn(eq(CLIENT_ID), anyCollection()))
                .thenReturn(List.of(microContentJob(renderId.toString(), DeepfakeJobStatus.FAILED)));

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(UploadToContentStatus.PENDING, result.get(0).getUploadToContent());
    }

    @Test
    void listVideos_emptyResult_skipsMicroContentLookup() {
        stubClient();
        when(renderJobRepository.findWithFilters(eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class))).thenReturn(List.of());

        List<DeepfakeVideoDto> result = service.listVideos(0, 10, null, null, null);

        assertEquals(0, result.size());
    }
}
