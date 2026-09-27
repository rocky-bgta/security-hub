package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.VishingCallLog;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.service.VishingCallAudioService;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.voice.VoiceCallRequest;
import com.aspire.asat.phishing.voice.VoiceCallResult;
import com.aspire.asat.phishing.voice.VoiceProvider;
import com.aspire.asat.phishing.voice.VoiceProviderFactory;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampaignVoiceConsumerTest {

    private static final String RECIPIENT_ID = "rec-1";
    private static final String TRACKING_ID = "trk-1";
    private static final String CONFIG_ID = "cfg-1";
    private static final String BASE_URL = "https://dev.example.com/gateway/phishing";

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private VoiceServerConfigurationRepository voiceServerConfigurationRepository;
    @Mock
    private VishingCallLogRepository callLogRepository;
    @Mock
    private VoiceProviderFactory voiceProviderFactory;
    @Mock
    private VishingCallAudioService vishingCallAudioService;
    @Mock
    private RecipientRiskScoringService recipientRiskScoringService;
    @Mock
    private CampaignCompletionEvaluator completionEvaluator;
    @Mock
    private VoiceProvider voiceProvider;

    @InjectMocks
    private CampaignVoiceConsumer consumer;

    private CampaignVoiceMessage message;

    @BeforeEach
    void setUp() throws Exception {
        Field field = CampaignVoiceConsumer.class.getDeclaredField("trackingBaseUrl");
        field.setAccessible(true);
        field.set(consumer, BASE_URL);
        message = CampaignVoiceMessage.builder()
                .campaignId("camp-1")
                .recipientId(RECIPIENT_ID)
                .clientId("client-1")
                .trackingId(TRACKING_ID)
                .toPhone("+15551234567")
                .renderedScript("Hello there")
                .voiceServerConfigurationId(CONFIG_ID)
                .externalVoiceId("voice-abc")
                .voiceCloneProvider(VoiceCloneProvider.ELEVENLABS)
                .language("en")
                .callerId("+15557654321")
                .campaignName("Q3 Vishing")
                .build();
    }

    @Test
    void eligibleRecipient_synthesizesUpsertsLogAndInitiatesCallWithCallbacks() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id(RECIPIENT_ID)
                .trackingId(TRACKING_ID)
                .email("jane.doe@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .status(RecipientStatus.PENDING)
                .build();
        when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(voiceServerConfigurationRepository.findById(CONFIG_ID))
                .thenReturn(Optional.of(new VoiceServerConfiguration()));
        when(voiceProviderFactory.create(any())).thenReturn(voiceProvider);
        when(vishingCallAudioService.synthesizeForCall(eq("client-1"), eq(VoiceCloneProvider.ELEVENLABS),
                eq("voice-abc"), eq("Hello there"), eq("en"))).thenReturn("vishing/audio/x.mp3");
        when(callLogRepository.findByTrackingId(TRACKING_ID)).thenReturn(Optional.empty());
        when(voiceProvider.initiateCall(any())).thenReturn(VoiceCallResult.ok("CA123"));

        consumer.processVoiceMessage(message);

        ArgumentCaptor<VishingCallLog> logCaptor = ArgumentCaptor.forClass(VishingCallLog.class);
        verify(callLogRepository).save(logCaptor.capture());
        assertEquals("vishing/audio/x.mp3", logCaptor.getValue().getAudioS3Key());
        assertEquals("Hello there", logCaptor.getValue().getRenderedScript());

        ArgumentCaptor<VoiceCallRequest> reqCaptor = ArgumentCaptor.forClass(VoiceCallRequest.class);
        verify(voiceProvider).initiateCall(reqCaptor.capture());
        VoiceCallRequest req = reqCaptor.getValue();
        assertEquals(BASE_URL + "/v/calls/" + TRACKING_ID + "/twiml", req.twimlUrl());
        assertEquals(BASE_URL + "/v/calls/" + TRACKING_ID + "/twilio-status", req.statusCallbackUrl());

        ArgumentCaptor<EmailActivity> activityCaptor = ArgumentCaptor.forClass(EmailActivity.class);
        verify(emailActivityRepository).save(activityCaptor.capture());
        EmailActivity activity = activityCaptor.getValue();
        assertEquals("jane.doe@example.com", activity.getRecipientEmail());
        assertEquals(ActivityType.VOICE_INITIATED, activity.getActivityType());
        assertEquals("+15551234567", activity.getMetadata().get("phoneNumber"));
    }

    @Test
    void ineligibleRecipient_skipsCallEntirely() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id(RECIPIENT_ID)
                .trackingId(TRACKING_ID)
                .status(RecipientStatus.ANSWERED)
                .build();
        when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));

        consumer.processVoiceMessage(message);

        verify(vishingCallAudioService, never()).synthesizeForCall(anyString(), any(), anyString(), anyString(), anyString());
        verify(voiceProviderFactory, never()).create(any());
        verify(callLogRepository, never()).save(any());
    }
}
