package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.enums.VoiceCloneSource;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.exception.VoiceCloneLimitReachedException;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapter;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.DeepfakeVoiceCloneRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampaignVoiceCloneServiceImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String SAMPLE_KEY = "vishing/voice-sample/abc.wav";
    private static final String EXTERNAL_VOICE_ID = "el-voice-123";
    private static final String VOICE_NAME = "CEO Voice";

    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private VoiceCloneAdapterFactory voiceCloneAdapterFactory;
    @Mock
    private DeepfakeVoiceCloneRepository voiceCloneRepository;
    @Mock
    private VoiceCloneAdapter voiceCloneAdapter;
    @Mock
    private ProviderCredentialResolver providerCredentialResolver;

    @InjectMocks
    private CampaignVoiceCloneServiceImpl campaignVoiceCloneService;

    private MultipartFile audioSample;
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        audioSample = mockAudio("sample.wav", 128);
        tempFile = Files.createTempFile("vishing-voice-", ".wav").toFile();
        tempFile.deleteOnExit();
    }

    @Test
    void createClone_success_persistsCompletedClone() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class)))
                .thenAnswer(inv -> {
                    DeepfakeVoiceClone clone = inv.getArgument(0);
                    if (clone.getId() == null) {
                        clone.setId("mongo-id-1");
                    }
                    return clone;
                });
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), eq("english (us)"),
                any())).thenReturn(EXTERNAL_VOICE_ID);

        DeepfakeVoiceClone result = campaignVoiceCloneService.createClone(
                audioSample, VoiceCloneProvider.ELEVENLABS, "English (US)", CLIENT_ID, VOICE_NAME);

        assertEquals("mongo-id-1", result.getId());
        assertEquals(CLIENT_ID, result.getClientId());
        assertEquals(VoiceCloneProvider.ELEVENLABS, result.getProvider());
        assertEquals(EXTERNAL_VOICE_ID, result.getExternalVoiceId());
        assertEquals(DeepfakeJobStatus.COMPLETED, result.getStatus());
        assertEquals("english (us)", result.getLanguage());
        assertEquals(SAMPLE_KEY, result.getSampleS3Key());
        assertEquals(VoiceCloneSource.VISHING, result.getVoiceCloneSource());
        assertEquals("sample.wav", result.getSampleFileName());
        assertEquals(VOICE_NAME, result.getVoiceName());
        verify(voiceCloneRepository, times(2)).save(any(DeepfakeVoiceClone.class));
    }

    @Test
    void createClone_defaultsProviderToElevenLabs() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), eq("en"),
                any())).thenReturn(EXTERNAL_VOICE_ID);

        DeepfakeVoiceClone result = campaignVoiceCloneService.createClone(audioSample, null, null, CLIENT_ID, "CEO Voice");

        assertEquals(VoiceCloneProvider.ELEVENLABS, result.getProvider());
        assertEquals("en", result.getLanguage());
        verify(voiceCloneAdapterFactory).getAdapter(VoiceCloneProvider.ELEVENLABS);
    }

    @Test
    void createClone_reusesPreviousVoiceWhenLimitReached() throws Exception {
        setReuseOnLimitReached(true);
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any()))
                .thenThrow(new VoiceCloneLimitReachedException("quota", null));
        when(voiceCloneRepository.findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(
                VoiceCloneProvider.ELEVENLABS, DeepfakeJobStatus.COMPLETED))
                .thenReturn(Optional.of(DeepfakeVoiceClone.builder()
                        .externalVoiceId("reused-voice")
                        .build()));

        DeepfakeVoiceClone result = campaignVoiceCloneService.createClone(
                audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME);

        assertEquals("reused-voice", result.getExternalVoiceId());
        assertEquals(DeepfakeJobStatus.COMPLETED, result.getStatus());
        assertTrue(result.isUsedFallbackVoice());
    }

    @Test
    void createClone_limitReachedWithNoReuse_throwsServiceUnavailable() throws Exception {
        setReuseOnLimitReached(true);
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any()))
                .thenThrow(new VoiceCloneLimitReachedException("quota", null));
        when(voiceCloneRepository.findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(
                VoiceCloneProvider.ELEVENLABS, DeepfakeJobStatus.COMPLETED))
                .thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(
                        audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, times(2)).save(captor.capture());
        assertEquals(DeepfakeJobStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    void createClone_limitReached_failsLoudlyWhenReuseDisabled() {
        // reuseOnLimitReached defaults to false
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any()))
                .thenThrow(new VoiceCloneLimitReachedException("quota", null));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(
                        audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        assertTrue(ex.getMessage().contains("Voice cloning limit reached"));
        verify(voiceCloneRepository, never())
                .findFirstByProviderAndStatusAndExternalVoiceIdIsNotNullOrderByCreatedAtDesc(any(), any());
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, times(2)).save(captor.capture());
        assertEquals(DeepfakeJobStatus.FAILED, captor.getValue().getStatus());
        assertFalse(captor.getValue().isUsedFallbackVoice());
    }

    @Test
    void createClone_adapterFailure_marksCloneFailed() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any()))
                .thenThrow(new RuntimeException("provider down"));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(
                        audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        assertTrue(ex.getMessage().contains("Voice cloning failed"));
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, times(2)).save(captor.capture());
        assertEquals(DeepfakeJobStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    void createClone_rejectsEmptyAudio() {
        MultipartFile empty = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(empty, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        assertEquals("Audio sample is required", ex.getMessage());
        verify(deepfakeS3Service, never()).uploadMultipart(any(), anyString());
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void createClone_rejectsInvalidExtension() {
        MultipartFile bad = mock(MultipartFile.class);
        when(bad.isEmpty()).thenReturn(false);
        when(bad.getSize()).thenReturn(64L);
        when(bad.getOriginalFilename()).thenReturn("sample.txt");

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(bad, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        assertTrue(ex.getMessage().contains("Invalid audio format"));
        verify(voiceCloneRepository, never()).save(any());
    }

    @Test
    void createClone_blankExternalVoiceId_marksFailed() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any())).thenReturn("  ");

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.createClone(
                        audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, VOICE_NAME));

        assertTrue(ex.getMessage().contains("did not return a voice id"));
        ArgumentCaptor<DeepfakeVoiceClone> captor = ArgumentCaptor.forClass(DeepfakeVoiceClone.class);
        verify(voiceCloneRepository, times(2)).save(captor.capture());
        assertEquals(DeepfakeJobStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    void createClone_usesVishingCloneNamePrefix() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> {
            DeepfakeVoiceClone clone = inv.getArgument(0);
            if (clone.getVoiceCloneId() == null) {
                clone.setVoiceCloneId(UUID.randomUUID());
            }
            return clone;
        });
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any())).thenReturn(EXTERNAL_VOICE_ID);

        campaignVoiceCloneService.createClone(audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, "CEO Voice");

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(voiceCloneAdapter).createClone(eq(tempFile), nameCaptor.capture(), eq("en"),
                any());
        assertTrue(nameCaptor.getValue().startsWith("vishing-"));
    }


    @Test
    void createClone_allowsBlankVoiceName() {
        when(deepfakeS3Service.uploadMultipart(audioSample, "vishing/voice-sample")).thenReturn(SAMPLE_KEY);
        when(voiceCloneRepository.save(any(DeepfakeVoiceClone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deepfakeS3Service.downloadToTemp(SAMPLE_KEY)).thenReturn(tempFile);
        when(voiceCloneAdapterFactory.getAdapter(VoiceCloneProvider.ELEVENLABS)).thenReturn(voiceCloneAdapter);
        when(voiceCloneAdapter.createClone(eq(tempFile), anyString(), anyString(),
                any())).thenReturn(EXTERNAL_VOICE_ID);

        DeepfakeVoiceClone result = campaignVoiceCloneService.createClone(
                audioSample, VoiceCloneProvider.ELEVENLABS, "en", CLIENT_ID, "  ");

        assertEquals(null, result.getVoiceName());
        assertEquals(EXTERNAL_VOICE_ID, result.getExternalVoiceId());
    }

    @Test
    void getExistingClone_returnsCompletedOwnedClone() {
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId(EXTERNAL_VOICE_ID)
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        DeepfakeVoiceClone result = campaignVoiceCloneService.getExistingClone(voiceCloneId, CLIENT_ID);

        assertEquals(EXTERNAL_VOICE_ID, result.getExternalVoiceId());
        verify(deepfakeS3Service, never()).uploadMultipart(any(), anyString());
        verify(voiceCloneAdapterFactory, never()).getAdapter(any());
    }

    @Test
    void getExistingClone_notFound_throws404() {
        UUID voiceCloneId = UUID.randomUUID();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                campaignVoiceCloneService.getExistingClone(voiceCloneId, CLIENT_ID));
        assertTrue(ex.getMessage().contains("Cloned voice not found"));
    }

    @Test
    void getExistingClone_wrongClient_throws404() {
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId("other-client")
                .externalVoiceId(EXTERNAL_VOICE_ID)
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                campaignVoiceCloneService.getExistingClone(voiceCloneId, CLIENT_ID));
        assertTrue(ex.getMessage().contains("Cloned voice not found"));
    }

    @Test
    void getExistingClone_notReady_throws409() {
        UUID voiceCloneId = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .voiceCloneId(voiceCloneId)
                .clientId(CLIENT_ID)
                .externalVoiceId(null)
                .status(DeepfakeJobStatus.PROCESSING)
                .build();
        when(voiceCloneRepository.findByVoiceCloneId(voiceCloneId)).thenReturn(Optional.of(clone));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                campaignVoiceCloneService.getExistingClone(voiceCloneId, CLIENT_ID));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("not ready for reuse"));
    }

    private static MultipartFile mockAudio(String filename, long size) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(size);
        when(file.getOriginalFilename()).thenReturn(filename);
        return file;
    }

    private void setReuseOnLimitReached(boolean value) throws Exception {
        Field field = CampaignVoiceCloneServiceImpl.class.getDeclaredField("reuseOnLimitReached");
        field.setAccessible(true);
        field.set(campaignVoiceCloneService, value);
    }
}
