package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.request.DeepfakeStep5Request;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeVideoServiceImplDeleteTest {

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

    private DeepfakeRenderJob job(UUID renderId, DeepfakeJobStatus status) {
        return DeepfakeRenderJob.builder()
                .renderId(renderId)
                .clientId(CLIENT_ID)
                .status(status)
                .build();
    }

    @Test
    void deleteVideo_completed_softDeletesJob() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId, DeepfakeJobStatus.COMPLETED);
        when(renderJobRepository.findByRenderIdAndClientId(renderId, CLIENT_ID)).thenReturn(Optional.of(job));

        service.deleteVideo(renderId);

        ArgumentCaptor<DeepfakeRenderJob> captor = ArgumentCaptor.forClass(DeepfakeRenderJob.class);
        verify(renderJobRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
        verify(renderJobRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteVideo_notFound_throwsResourceNotFound() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        when(renderJobRepository.findByRenderIdAndClientId(renderId, CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteVideo(renderId));
        verify(renderJobRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteVideo_alreadyDeleted_throwsResourceNotFound() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId, DeepfakeJobStatus.COMPLETED);
        job.setDeleted(true);
        when(renderJobRepository.findByRenderIdAndClientId(renderId, CLIENT_ID)).thenReturn(Optional.of(job));

        assertThrows(ResourceNotFoundException.class, () -> service.deleteVideo(renderId));
        verify(renderJobRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteVideo_processing_throwsConflict() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId, DeepfakeJobStatus.PROCESSING);
        when(renderJobRepository.findByRenderIdAndClientId(renderId, CLIENT_ID)).thenReturn(Optional.of(job));

        assertThrows(ServiceException.class, () -> service.deleteVideo(renderId));
        verify(renderJobRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateStep5_softDeletedVoice_throwsResourceNotFoundBeforeSynthesis() {
        stubClient();
        UUID renderId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeRenderJob job = job(renderId, DeepfakeJobStatus.DRAFT);
        job.setFaceConfirmed(true);
        job.setVoiceCloneId(voiceCloneId.toString());
        DeepfakeVoiceClone deletedClone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .status(DeepfakeJobStatus.COMPLETED)
                .externalVoiceId("deleted-external-id")
                .isDeleted(true)
                .build();
        when(renderJobRepository.findByRenderIdAndClientId(renderId, CLIENT_ID)).thenReturn(Optional.of(job));
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(deletedClone));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStep5(renderId, DeepfakeStep5Request.builder().script("hello").build()));

        verify(voiceCloneAdapterFactory, never()).getAdapter(org.mockito.ArgumentMatchers.any());
    }
}
