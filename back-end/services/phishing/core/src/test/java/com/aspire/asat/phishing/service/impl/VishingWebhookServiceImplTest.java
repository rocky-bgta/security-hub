package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.service.VoiceIngestionService;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.voice.TwilioSignatureValidator;
import com.aspire.asat.phishing.voice.VishingOutcomeResolution;
import com.aspire.asat.phishing.voice.VishingOutcomeResolver;
import com.aspire.asat.phishing.voice.VishingTwimlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VishingWebhookServiceImplTest {

    private static final String TRACKING_ID = "trk-1";
    private static final String CAMPAIGN_ID = "camp-1";

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private VishingCallLogRepository callLogRepository;
    @Mock
    private VoiceServerConfigurationService voiceServerConfigurationService;
    @Mock
    private CredentialEncryptionService credentialEncryptionService;
    @Mock
    private DeepfakeS3Service deepfakeS3Service;
    @Mock
    private VishingTwimlBuilder twimlBuilder;
    @Mock
    private VishingOutcomeResolver outcomeResolver;
    @Mock
    private TwilioSignatureValidator signatureValidator;
    @Mock
    private VoiceIngestionService voiceIngestionService;

    @InjectMocks
    private VishingWebhookServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        setField("validateSignature", false);
        setField("trackingBaseUrl", "https://dev.example.com/gateway/phishing");

        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("rec-1").campaignId(CAMPAIGN_ID).trackingId(TRACKING_ID).build();
        Campaign campaign = Campaign.builder().id(CAMPAIGN_ID).clientId("client-1").build();
        when(recipientRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.of(recipient));
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(twimlBuilder.buildClosingTwiml(any())).thenReturn("<Response/>");
    }

    @Test
    void handleGather_dtmf_storesDigitsAsTranscript() {
        Map<String, String> params = new HashMap<>();
        params.put("Digits", "55555");
        when(outcomeResolver.resolve(eq("55555"), any(), any(), anyList()))
                .thenReturn(new VishingOutcomeResolution(
                        VishingCallOutcome.COMPROMISED, new HashMap<>(), List.of()));

        service.handleGather(TRACKING_ID, params, "sig");

        ArgumentCaptor<VoiceCallResultRequest> captor = ArgumentCaptor.forClass(VoiceCallResultRequest.class);
        verify(voiceIngestionService).processCallResult(eq(TRACKING_ID), captor.capture());
        assertEquals("DTMF digits entered: 55555", captor.getValue().getTranscript());
    }

    @Test
    void handleGather_speech_storesSpeechAsTranscript() {
        Map<String, String> params = new HashMap<>();
        params.put("SpeechResult", "my code is 1234");
        when(outcomeResolver.resolve(any(), eq("my code is 1234"), any(), anyList()))
                .thenReturn(new VishingOutcomeResolution(
                        VishingCallOutcome.ENGAGED, new HashMap<>(), List.of()));

        service.handleGather(TRACKING_ID, params, "sig");

        ArgumentCaptor<VoiceCallResultRequest> captor = ArgumentCaptor.forClass(VoiceCallResultRequest.class);
        verify(voiceIngestionService).processCallResult(eq(TRACKING_ID), captor.capture());
        assertEquals("my code is 1234", captor.getValue().getTranscript());
    }

    @Test
    void handleStatusCallback_completed_persistsDurationAndTimestamps() {
        VishingCallLog logEntry = VishingCallLog.builder().trackingId(TRACKING_ID).build();
        when(callLogRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.of(logEntry));

        Map<String, String> params = new HashMap<>();
        params.put("CallStatus", "completed");
        params.put("CallDuration", "29");

        service.handleStatusCallback(TRACKING_ID, params, "sig");

        ArgumentCaptor<VishingCallLog> captor = ArgumentCaptor.forClass(VishingCallLog.class);
        verify(callLogRepository).save(captor.capture());
        assertEquals(29, captor.getValue().getDurationSeconds());
        assertNotNull(captor.getValue().getEndedAt());
        assertNotNull(captor.getValue().getStartedAt());
    }

    @Test
    void handleStatusCallback_inProgress_setsStartedAt() {
        VishingCallLog logEntry = VishingCallLog.builder().trackingId(TRACKING_ID).build();
        when(callLogRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.of(logEntry));

        Map<String, String> params = new HashMap<>();
        params.put("CallStatus", "in-progress");

        service.handleStatusCallback(TRACKING_ID, params, "sig");

        ArgumentCaptor<VishingCallLog> captor = ArgumentCaptor.forClass(VishingCallLog.class);
        verify(callLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getStartedAt());
        assertEquals(0, captor.getValue().getDurationSeconds());
    }

    private void setField(String name, Object value) throws Exception {
        Field field = VishingWebhookServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }
}
