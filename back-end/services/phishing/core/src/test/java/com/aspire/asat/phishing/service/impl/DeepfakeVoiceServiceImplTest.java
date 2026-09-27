package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.VishingVoiceFilter;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.DeepfakeRenderJobRepository;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.DeepfakeVideoService;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeVoiceServiceImplTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    @Mock
    private DeepfakeVoiceCloneRepository voiceCloneRepository;
    @Mock
    private DeepfakeRenderJobRepository renderJobRepository;
    @Mock
    private DeepfakeVideoService deepfakeVideoService;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ProviderCredentialResolver providerCredentialResolver;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DeepfakeVoiceServiceImpl service;

    private void stubClient() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    private DeepfakeRenderJob confirmedJob() {
        return DeepfakeRenderJob.builder()
                .renderId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .faceConfirmed(true)
                .currentStep(3)
                .status(DeepfakeJobStatus.DRAFT)
                .build();
    }

    @Test
    void updateStep4_rejectsWhenBothFileAndVoiceCloneIdProvided() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStep4(UUID.randomUUID(), file, "ELEVENLABS", "en", UUID.randomUUID(), null, null));
        assertTrue(ex.getMessage().contains("either an audio sample or an existing voiceCloneId"));
        verify(deepfakeVideoService, never()).getForEdit(any());
    }

    @Test
    void updateStep4_rejectsWhenNeitherProvided() {
        assertThrows(ServiceException.class,
                () -> service.updateStep4(UUID.randomUUID(), null, "ELEVENLABS", "en", null, null, null));
    }


    @Test
    void updateStep4_allowsBlankVoiceNameWhenUploadingSample() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(new java.io.File("unused"));
        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        when(adapter.createClone(any(), any(), any(), any()))
                .thenThrow(new ServiceException("stop before provider", org.springframework.http.HttpStatus.BAD_GATEWAY));

        assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, "ELEVENLABS", "en", null, "  ", null));

        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(null, captor.getAllValues().get(0).getVoiceName());
    }

    @Test
    void updateStep4_collapsesDuplicatedCommaJoinedVoiceName() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(new java.io.File("unused"));
        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        when(adapter.createClone(any(), any(), any(), any()))
                .thenThrow(new ServiceException("stop before provider", org.springframework.http.HttpStatus.BAD_GATEWAY));

        assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, "ELEVENLABS", "en", null, "tesyting,tesyting", null));

        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertEquals("tesyting", captor.getAllValues().get(0).getVoiceName());
    }

    @Test
    void updateStep4_preservesVoiceNameWithDistinctCommaParts() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(new java.io.File("unused"));
        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        when(adapter.createClone(any(), any(), any(), any()))
                .thenThrow(new ServiceException("stop before provider", org.springframework.http.HttpStatus.BAD_GATEWAY));

        assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, "ELEVENLABS", "en", null, "Smith, John", null));

        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertEquals("Smith, John", captor.getAllValues().get(0).getVoiceName());
    }

    @Test
    void updateStep4_reusesExistingCompletedVoice() {
        UUID videoId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeRenderJob job = confirmedJob();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        stubClient();

        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-123")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));

        DeepfakeVideoStepResponse response = service.updateStep4(videoId, null, null, null, voiceCloneId, null, null);

        assertEquals(voiceCloneId.toString(), job.getVoiceCloneId());
        assertTrue(job.getCurrentStep() >= 4);
        assertEquals(4, response.getCurrentStep());
        verify(deepfakeS3Service, never()).uploadMultipart(any(), any());
        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
    }

    @Test
    void updateStep4_reuseRejectsVoiceOwnedByAnotherClient() {
        UUID videoId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();

        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId("another-client")
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-123")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStep4(videoId, null, null, null, voiceCloneId, null, null));
    }

    @Test
    void updateStep4_reuseRejectsNotReadyVoice() {
        UUID videoId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();

        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .status(DeepfakeJobStatus.PROCESSING)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, null, null, null, voiceCloneId, null, null));
    }

    @Test
    void listClonedVoices_mapsResults() {
        stubClient();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.FISH_AUDIO)
                .voiceName("My Clone")
                .sampleFileName("my-voice.mp3")
                .sampleS3Key("deepfake/voice-sample/abc.mp3")
                .language("en")
                .externalVoiceId("ext-1")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of(clone));
        when(deepfakeS3Service.presignGetUrl("deepfake/voice-sample/abc.mp3")).thenReturn("https://signed-url");

        Page<ClonedVoiceDto> page = service.listClonedVoices(null, null, null, 0, 10);

        assertEquals(1, page.getTotalElements());
        ClonedVoiceDto dto = page.getContent().get(0);
        assertEquals("My Clone", dto.getVoiceName());
        assertEquals("my-voice.mp3", dto.getFileName());
        assertEquals("https://signed-url", dto.getSampleUrl());
        assertEquals(VoiceCloneProvider.FISH_AUDIO, dto.getProvider());
        assertEquals(DeepfakeJobStatus.COMPLETED, dto.getStatus());
    }

    @Test
    void listVishingVoices_nonAdmin_scopesToOwnClientId() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(DeepfakeVoiceClone.class));
        String queryText = captor.getValue().toString();
        assertTrue(queryText.contains("clientId"));
        assertTrue(queryText.contains(CLIENT_ID));
        assertTrue(queryText.contains("voiceCloneSource") || queryText.contains("sampleS3Key"));
    }

    @Test
    void listVishingVoices_platformAdmin_withoutClientId_omitsTenantFilter() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId("admin-tenant")
                        .userType(UserType.SUPER_ADMIN.name())
                        .build());
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(DeepfakeVoiceClone.class));
        String queryText = captor.getValue().toString();
        assertFalse(queryText.contains("clientId"));
        assertTrue(queryText.contains("VISHING") || queryText.contains("vishing/voice-sample"));
    }

    @Test
    void listVishingVoices_platformAdmin_withClientId_narrowsTenant() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId("admin-tenant")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listVishingVoices(VishingVoiceFilter.builder().clientId("tenant-x").build(), 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(DeepfakeVoiceClone.class));
        String queryText = captor.getValue().toString();
        assertTrue(queryText.contains("tenant-x"));
    }

    @Test
    void listVishingVoices_appliesProviderLanguageStatusAndSearch() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listVishingVoices(VishingVoiceFilter.builder()
                .provider(VoiceCloneProvider.ELEVENLABS)
                .language("EN")
                .status(DeepfakeJobStatus.COMPLETED)
                .search("sample")
                .build(), 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(DeepfakeVoiceClone.class));
        String queryText = captor.getValue().toString();
        assertTrue(queryText.contains("ELEVENLABS"));
        assertTrue(queryText.contains("en"));
        assertTrue(queryText.contains("COMPLETED"));
        assertTrue(queryText.contains("voiceName")
                || queryText.contains("sampleFileName")
                || queryText.contains("externalVoiceId"));
    }

    @Test
    void listVishingVoices_mapsStatusOntoDto() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .sampleFileName("vish.wav")
                .sampleS3Key("vishing/voice-sample/x.wav")
                .language("en")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of(clone));
        when(deepfakeS3Service.presignGetUrl(any())).thenReturn("https://url");

        Page<ClonedVoiceDto> page = service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        assertEquals(DeepfakeJobStatus.COMPLETED, page.getContent().get(0).getStatus());
    }

    @Test
    void listVishingVoices_derivesFileNameFromS3KeyWhenSampleFileNameNull() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .sampleFileName(null)
                .sampleS3Key("vishing/voice-sample/52754851-481a-4092-85e1-66f50de2d704-voice-sample.webm")
                .language("en")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of(clone));
        when(deepfakeS3Service.presignGetUrl(any())).thenReturn("https://url");

        Page<ClonedVoiceDto> page = service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        assertEquals("voice-sample.webm", page.getContent().get(0).getFileName());
    }

    @Test
    void listVishingVoices_prefersSampleFileNameWhenPresent() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userType(UserType.CLIENT_ADMIN.name())
                        .build());
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .sampleFileName("original-name.wav")
                .sampleS3Key("vishing/voice-sample/52754851-481a-4092-85e1-66f50de2d704-ignored.wav")
                .language("en")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of(clone));
        when(deepfakeS3Service.presignGetUrl(any())).thenReturn("https://url");

        Page<ClonedVoiceDto> page = service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        assertEquals("original-name.wav", page.getContent().get(0).getFileName());
    }

    @Test
    void deleteClonedVoice_softDeletesAndCallsProviderWhenUnique() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-123")
                .status(DeepfakeJobStatus.COMPLETED)
                .isDeleted(false)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(clone));
        when(voiceCloneRepository.countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
                "ext-123", VoiceCloneProvider.ELEVENLABS, voiceCloneId)).thenReturn(0L);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder().apiKey("key").build();
        when(providerCredentialResolver.resolve(CLIENT_ID, "ELEVENLABS")).thenReturn(credentials);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteClonedVoice(voiceCloneId);

        verify(adapter).deleteClone("ext-123", credentials);
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
    }

    @Test
    void deleteClonedVoice_skipsProviderDeleteWhenShared() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("shared-ext")
                .status(DeepfakeJobStatus.COMPLETED)
                .usedFallbackVoice(true)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(clone));
        when(voiceCloneRepository.countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
                "shared-ext", VoiceCloneProvider.ELEVENLABS, voiceCloneId)).thenReturn(1L);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteClonedVoice(voiceCloneId);

        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
        verify(voiceCloneRepository).save(any(DeepfakeVoiceClone.class));
        assertTrue(clone.isDeleted());
    }

    @Test
    void deleteClonedVoice_notFoundWhenMissingOrAlreadyDeleted() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteClonedVoice(voiceCloneId));

        DeepfakeVoiceClone deleted = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .isDeleted(true)
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(deleted));

        assertThrows(ResourceNotFoundException.class, () -> service.deleteClonedVoice(voiceCloneId));
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void deleteClonedVoice_rejectsProcessingStatus() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-1")
                .status(DeepfakeJobStatus.PROCESSING)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(clone));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteClonedVoice(voiceCloneId));
        assertTrue(ex.getMessage().contains("cloning is in progress"));
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void deleteClonedVoice_doesNotSoftDeleteWhenProviderFails() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-fail")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(clone));
        when(voiceCloneRepository.countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
                "ext-fail", VoiceCloneProvider.ELEVENLABS, voiceCloneId)).thenReturn(0L);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder().apiKey("key").build();
        when(providerCredentialResolver.resolve(CLIENT_ID, "ELEVENLABS")).thenReturn(credentials);
        doThrow(new ServiceException("ElevenLabs voice delete failed", org.springframework.http.HttpStatus.BAD_GATEWAY))
                .when(adapter).deleteClone("ext-fail", credentials);

        assertThrows(ServiceException.class, () -> service.deleteClonedVoice(voiceCloneId));
        verify(voiceCloneRepository, never()).save(any());
        assertFalse(clone.isDeleted());
    }

    @Test
    void updateStep4_reuseRejectsSoftDeletedVoice() {
        UUID videoId = UUID.randomUUID();
        UUID voiceCloneId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();

        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("ext-123")
                .status(DeepfakeJobStatus.COMPLETED)
                .isDeleted(true)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStep4(videoId, null, null, null, voiceCloneId, null, null));
    }

    @Test
    void listClonedVoices_excludesDeletedInQueryCriteria() {
        stubClient();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listClonedVoices(null, null, null, 0, 10);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(DeepfakeVoiceClone.class));
        assertTrue(queryCaptor.getValue().getQueryObject().containsKey("isDeleted"));
    }

    @Test
    void listVishingVoices_excludesDeletedInQueryCriteria() {
        stubClient();
        when(mongoTemplate.count(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(DeepfakeVoiceClone.class))).thenReturn(List.of());

        service.listVishingVoices(VishingVoiceFilter.builder().build(), 0, 10);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(DeepfakeVoiceClone.class));
        assertTrue(queryCaptor.getValue().getQueryObject().toString().contains("isDeleted"));
    }

    @Test
    void updateStep4_withProviderId_usesResolveByIdAndStoresCredentialId() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = confirmedJob();
        job.setRenderId(videoId);
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));
        java.io.File temp = java.io.File.createTempFile("voice", ".wav");
        temp.deleteOnExit();
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(temp);

        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder()
                .providerName("FISH_AUDIO")
                .category(ProviderCategory.VOICE_CLONING)
                .modelName("s2-pro")
                .apiKey("fish-key")
                .baseUrl("https://api.fish.audio")
                .build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-voice-1")).thenReturn(credentials);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(adapter.getProvider()).thenReturn(VoiceCloneProvider.FISH_AUDIO);
        when(voiceCloneAdapterFactory.getAdapterForBaseUrl("https://api.fish.audio")).thenReturn(adapter);
        when(adapter.createClone(eq(temp), any(), eq("en"), eq(credentials))).thenReturn("ext-fish");

        DeepfakeVideoStepResponse response = service.updateStep4(
                videoId, file, "ELEVENLAB", "en", null, "My Voice", "pc-voice-1");

        assertEquals(videoId.toString(), response.getVideoId());
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        DeepfakeVoiceClone saved = captor.getAllValues().stream()
                .filter(c -> c.getStatus() == DeepfakeJobStatus.COMPLETED)
                .findFirst()
                .orElseThrow();
        assertEquals(VoiceCloneProvider.FISH_AUDIO, saved.getProvider());
        assertEquals("pc-voice-1", saved.getProviderCredentialId());
        assertEquals("ext-fish", saved.getExternalVoiceId());
        verify(providerCredentialResolver).resolveById(CLIENT_ID, "pc-voice-1");
        verify(providerCredentialResolver, never()).resolve(any(), any());
    }

    @Test
    void updateStep4_withoutProviderId_acceptsSpacedFishAudioName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(new java.io.File("unused"));
        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.FISH_AUDIO)).thenReturn(adapter);
        when(adapter.createClone(any(), any(), any(), any()))
                .thenThrow(new ServiceException("stop before provider", org.springframework.http.HttpStatus.BAD_GATEWAY));

        assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, "FISH AUDIO", "en", null, "fish self voice", null));

        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(VoiceCloneProvider.FISH_AUDIO, captor.getAllValues().get(0).getProvider());
        verify(providerCredentialResolver).resolve(CLIENT_ID, "FISH_AUDIO");
        verify(voiceCloneAdapterFactory).getAdapter(VoiceCloneProvider.FISH_AUDIO);
    }

    @Test
    void updateStep4_withProviderId_acceptsSpacedCredentialProviderName() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = confirmedJob();
        job.setRenderId(videoId);
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));
        java.io.File temp = java.io.File.createTempFile("voice", ".wav");
        temp.deleteOnExit();
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(temp);

        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder()
                .providerName("FISH AUDIO")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("fish-key")
                .baseUrl("https://fish.audio")
                .build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-voice-1")).thenReturn(credentials);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(adapter.getProvider()).thenReturn(VoiceCloneProvider.FISH_AUDIO);
        when(voiceCloneAdapterFactory.getAdapterForBaseUrl("https://fish.audio")).thenReturn(adapter);
        when(adapter.createClone(eq(temp), any(), eq("en"), eq(credentials))).thenReturn("ext-fish");

        DeepfakeVideoStepResponse response = service.updateStep4(
                videoId, file, "FISH AUDIO", "en", null, "fish self voice", "pc-voice-1");

        assertEquals(videoId.toString(), response.getVideoId());
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        DeepfakeVoiceClone saved = captor.getAllValues().stream()
                .filter(c -> c.getStatus() == DeepfakeJobStatus.COMPLETED)
                .findFirst()
                .orElseThrow();
        assertEquals(VoiceCloneProvider.FISH_AUDIO, saved.getProvider());
        verify(voiceCloneAdapterFactory).getAdapterForBaseUrl("https://fish.audio");
        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
        verify(providerCredentialResolver, never()).resolve(any(), any());
    }

    @Test
    void updateStep4_withProviderId_customNameAndElevenLabsUrl_doesNotParseName() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        DeepfakeRenderJob job = confirmedJob();
        job.setRenderId(videoId);
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(job);
        stubClient();
        when(deepfakeS3Service.uploadMultipart(file, "deepfake/voice-sample")).thenReturn("deepfake/voice-sample/a.wav");
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(renderJobRepository.save(any(DeepfakeRenderJob.class))).thenAnswer(inv -> inv.getArgument(0));
        java.io.File temp = java.io.File.createTempFile("voice", ".wav");
        temp.deleteOnExit();
        when(deepfakeS3Service.downloadToTemp(any())).thenReturn(temp);

        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder()
                .providerName("My TTS")
                .category(ProviderCategory.VOICE_CLONING)
                .apiKey("el-key")
                .baseUrl("https://api.elevenlabs.io")
                .build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-custom")).thenReturn(credentials);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(adapter.getProvider()).thenReturn(VoiceCloneProvider.ELEVENLABS);
        when(voiceCloneAdapterFactory.getAdapterForBaseUrl("https://api.elevenlabs.io")).thenReturn(adapter);
        when(adapter.createClone(eq(temp), any(), eq("en"), eq(credentials))).thenReturn("ext-el");

        DeepfakeVideoStepResponse response = service.updateStep4(
                videoId, file, "My TTS", "en", null, "custom voice", "pc-custom");

        assertEquals(videoId.toString(), response.getVideoId());
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        DeepfakeVoiceClone saved = captor.getAllValues().stream()
                .filter(c -> c.getStatus() == DeepfakeJobStatus.COMPLETED)
                .findFirst()
                .orElseThrow();
        assertEquals(VoiceCloneProvider.ELEVENLABS, saved.getProvider());
        assertEquals("pc-custom", saved.getProviderCredentialId());
        verify(voiceCloneAdapterFactory).getAdapterForBaseUrl("https://api.elevenlabs.io");
        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
    }

    @Test
    void updateStep4_withoutProviderId_rejectsInvalidProviderName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, "ELEVENLAB", "en", null, "My Voice", null));
        assertTrue(ex.getMessage().contains("Unsupported voice cloning provider"));
        verify(providerCredentialResolver, never()).resolveById(any(), any());
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void updateStep4_withProviderId_rejectsNonVoiceCategory() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(128L);
        when(file.getOriginalFilename()).thenReturn("sample.wav");
        UUID videoId = UUID.randomUUID();
        when(deepfakeVideoService.getForEdit(videoId)).thenReturn(confirmedJob());
        stubClient();

        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-video"))
                .thenReturn(ResolvedProviderCredentials.builder()
                        .providerName("HEYGEN")
                        .category(ProviderCategory.VIDEO_RENDERING)
                        .apiKey("k")
                        .build());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStep4(videoId, file, null, "en", null, null, "pc-video"));
        assertTrue(ex.getMessage().contains("voice cloning"));
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void deleteClonedVoice_usesResolveByIdWhenCredentialIdPresent() {
        stubClient();
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .providerCredentialId("pc-voice-1")
                .externalVoiceId("ext-123")
                .status(DeepfakeJobStatus.COMPLETED)
                .isDeleted(false)
                .build();
        when(voiceCloneRepository.findByVoiceCloneIdAndClientId(voiceCloneId, CLIENT_ID))
                .thenReturn(Optional.of(clone));
        when(voiceCloneRepository.countByExternalVoiceIdAndProviderAndIsDeletedFalseAndVoiceCloneIdNot(
                "ext-123", VoiceCloneProvider.ELEVENLABS, voiceCloneId)).thenReturn(0L);

        VoiceCloneAdapter adapter = mock(VoiceCloneAdapter.class);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(adapter);
        ResolvedProviderCredentials credentials = ResolvedProviderCredentials.builder().apiKey("key").build();
        when(providerCredentialResolver.resolveById(CLIENT_ID, "pc-voice-1")).thenReturn(credentials);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteClonedVoice(voiceCloneId);

        verify(providerCredentialResolver).resolveById(CLIENT_ID, "pc-voice-1");
        verify(providerCredentialResolver, never()).resolve(any(), any());
        verify(adapter).deleteClone("ext-123", credentials);
    }
}
