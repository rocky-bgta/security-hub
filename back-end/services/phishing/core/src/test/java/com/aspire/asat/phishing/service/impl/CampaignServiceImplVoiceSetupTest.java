package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.CampaignVoiceData;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.CampaignVoiceSetupRequest;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.service.CampaignVoiceCloneService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampaignServiceImplVoiceSetupTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "campaign-1";

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignVoiceCloneService campaignVoiceCloneService;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private SmsServerConfigurationRepository smsServerConfigurationRepository;
    @Mock
    private VoiceServerConfigurationRepository voiceServerConfigurationRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private MultipartFile audioSample;
    private CampaignVoiceSetupRequest request;

    @BeforeEach
    void setUp() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        audioSample = mock(MultipartFile.class);
        when(audioSample.isEmpty()).thenReturn(false);
        request = CampaignVoiceSetupRequest.builder()
                .consentConfirmed(true)
                .consentText("I confirm consent")
                .callerId("+15551234567")
                .build();
    }

    @Test
    void updateVoiceSetup_rejectsNonVoiceCampaign() {
        Campaign emailCampaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.EMAIL)
                .status(CampaignStatus.DRAFT)
                .currentStep(1)
                .build();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(emailCampaign));

        PhishingValidationException ex = assertThrows(PhishingValidationException.class, () ->
                campaignService.updateVoiceSetup(
                        CAMPAIGN_ID, request, audioSample, null, VoiceCloneProvider.ELEVENLABS, "en", "CEO Voice"));

        assertEquals("Voice setup is only for voice campaigns", ex.getMessage());
        verify(campaignVoiceCloneService, never()).createClone(any(), any(), any(), any(), any());
        verify(campaignVoiceCloneService, never()).getExistingClone(any(), any());
    }

    @Test
    void updateVoiceSetup_rejectsMissingConsent() {
        Campaign voiceCampaign = draftVoiceCampaign();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(voiceCampaign));
        request.setConsentConfirmed(false);

        PhishingValidationException ex = assertThrows(PhishingValidationException.class, () ->
                campaignService.updateVoiceSetup(
                        CAMPAIGN_ID, request, audioSample, null, VoiceCloneProvider.ELEVENLABS, "en", "CEO Voice"));

        assertEquals("Voice clone consent must be confirmed", ex.getMessage());
        verify(campaignVoiceCloneService, never()).createClone(any(), any(), any(), any(), any());
    }

    @Test
    void updateVoiceSetup_rejectsBothFileAndVoiceCloneId() {
        Campaign voiceCampaign = draftVoiceCampaign();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(voiceCampaign));

        PhishingValidationException ex = assertThrows(PhishingValidationException.class, () ->
                campaignService.updateVoiceSetup(
                        CAMPAIGN_ID, request, audioSample, UUID.randomUUID(),
                        VoiceCloneProvider.ELEVENLABS, "en", "CEO Voice"));

        assertTrue(ex.getMessage().contains("not both"));
        verify(campaignVoiceCloneService, never()).createClone(any(), any(), any(), any(), any());
        verify(campaignVoiceCloneService, never()).getExistingClone(any(), any());
    }

    @Test
    void updateVoiceSetup_rejectsNeitherFileNorVoiceCloneId() {
        Campaign voiceCampaign = draftVoiceCampaign();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(voiceCampaign));

        PhishingValidationException ex = assertThrows(PhishingValidationException.class, () ->
                campaignService.updateVoiceSetup(
                        CAMPAIGN_ID, request, null, null, VoiceCloneProvider.ELEVENLABS, "en", null));

        assertTrue(ex.getMessage().contains("required"));
        verify(campaignVoiceCloneService, never()).createClone(any(), any(), any(), any(), any());
        verify(campaignVoiceCloneService, never()).getExistingClone(any(), any());
    }

    @Test
    void updateVoiceSetup_happyPath_createsCloneAndStoresVoiceData() {
        Campaign voiceCampaign = draftVoiceCampaign();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(voiceCampaign));

        UUID cloneUuid = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .id("clone-mongo-id")
                .voiceCloneId(cloneUuid)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("el-voice-xyz")
                .voiceName("CEO Voice")
                .language("en")
                .status(DeepfakeJobStatus.COMPLETED)
                .build();
        when(campaignVoiceCloneService.createClone(
                eq(audioSample), eq(VoiceCloneProvider.ELEVENLABS), eq("English (US)"), eq(CLIENT_ID), eq("CEO Voice")))
                .thenReturn(clone);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignDto.builder()
                    .campaignId(c.getId())
                    .channel(c.getChannel())
                    .currentStep(c.getCurrentStep())
                    .voiceData(c.getVoiceData())
                    .build();
        });

        CampaignDto result = campaignService.updateVoiceSetup(
                CAMPAIGN_ID, request, audioSample, null, VoiceCloneProvider.ELEVENLABS, "English (US)", "CEO Voice");

        assertNotNull(result.getVoiceData());
        CampaignVoiceData voiceData = result.getVoiceData();
        assertTrue(voiceData.isConsentConfirmed());
        assertEquals("I confirm consent", voiceData.getConsentText());
        assertEquals(cloneUuid.toString(), voiceData.getVoiceCloneId());
        assertEquals("el-voice-xyz", voiceData.getExternalVoiceId());
        assertEquals("CEO Voice", voiceData.getVoiceDisplayName());
        assertEquals(VoiceCloneProvider.ELEVENLABS, voiceData.getCloningEngine());
        assertEquals("+15551234567", voiceData.getCallerId());
        assertEquals(3, result.getCurrentStep());

        ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(campaignCaptor.capture());
        assertEquals(3, campaignCaptor.getValue().getCurrentStep());
        assertNotNull(campaignCaptor.getValue().getVoiceData().getConsentConfirmedAt());
        verify(campaignVoiceCloneService, never()).getExistingClone(any(), any());
    }

    @Test
    void updateVoiceSetup_reusesExistingCloneWithoutCreating() {
        Campaign voiceCampaign = draftVoiceCampaign();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(voiceCampaign));

        UUID existingUuid = UUID.randomUUID();
        DeepfakeVoiceClone clone = DeepfakeVoiceClone.builder()
                .id("mongo-id")
                .voiceCloneId(existingUuid)
                .clientId(CLIENT_ID)
                .provider(VoiceCloneProvider.ELEVENLABS)
                .externalVoiceId("el-reused")
                .language("en")
                .status(DeepfakeJobStatus.COMPLETED)
                .usedFallbackVoice(false)
                .build();
        when(campaignVoiceCloneService.getExistingClone(existingUuid, CLIENT_ID)).thenReturn(clone);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignDto.builder()
                    .campaignId(c.getId())
                    .channel(c.getChannel())
                    .currentStep(c.getCurrentStep())
                    .voiceData(c.getVoiceData())
                    .build();
        });

        CampaignDto result = campaignService.updateVoiceSetup(
                CAMPAIGN_ID, request, null, existingUuid, null, null, null);

        assertEquals(existingUuid.toString(), result.getVoiceData().getVoiceCloneId());
        assertEquals("el-reused", result.getVoiceData().getExternalVoiceId());
        assertEquals(VoiceCloneProvider.ELEVENLABS, result.getVoiceData().getCloningEngine());
        verify(campaignVoiceCloneService).getExistingClone(existingUuid, CLIENT_ID);
        verify(campaignVoiceCloneService, never()).createClone(any(), any(), any(), any(), any());
    }

    private Campaign draftVoiceCampaign() {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.VOICE)
                .status(CampaignStatus.DRAFT)
                .currentStep(1)
                .build();
    }
}
