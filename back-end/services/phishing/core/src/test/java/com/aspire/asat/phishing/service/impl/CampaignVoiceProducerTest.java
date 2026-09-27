package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.CampaignVoiceData;
import com.aspire.asat.phishing.dto.CampaignVoiceScenarioRef;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignVoiceProducerTest {

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String CLIENT_ID = "client-1";

    @Mock
    private SqsClient sqsClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private VoiceServerConfigurationService voiceServerConfigurationService;
    @Spy
    private TemplatePersonalizationService templatePersonalizationService = new TemplatePersonalizationService();

    @InjectMocks
    private CampaignVoiceProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        setField(producer, "queueUrl", "https://sqs.test/voice");
        setField(producer, "publishMaxAttempts", 2);
        setField(producer, "publishInitialBackoffMs", 1L);
    }

    @Test
    void publishCampaignVoice_personalizesAllRecipientFieldsAndLegacyAliases() throws Exception {
        String script = "Hello {{FIRST_NAME}} {{LAST_NAME}}. "
                + "Email {{EMAIL_ADDRESS}} or {{EMAIL}}, dept {{DEPARTMENT}}, "
                + "org {{organizationName}}, phone {{PHONE_NUMBER}} or {{PHONE}}, "
                + "location {{LOCATION}}.";

        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .campaignName("Vishing Campaign")
                .channel(CampaignChannel.VOICE)
                .voiceScenario(CampaignVoiceScenarioRef.builder()
                        .scriptBody(script)
                        .language("en")
                        .build())
                .voiceData(CampaignVoiceData.builder()
                        .externalVoiceId("voice-1")
                        .cloningEngine(VoiceCloneProvider.ELEVENLABS)
                        .callerId("+15550001111")
                        .build())
                .build();

        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("recipient-1")
                .campaignId(CAMPAIGN_ID)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .department("Engineering")
                .organizationName("Aspire")
                .phoneNumber("+15551234567")
                .countryName("United States")
                .trackingId("trk-1")
                .status(RecipientStatus.PENDING)
                .build();

        VoiceServerConfiguration voiceServer = VoiceServerConfiguration.builder()
                .id("vs-1")
                .callerId("+15550001111")
                .build();

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(voiceServerConfigurationService.resolveForCampaign(CLIENT_ID, null)).thenReturn(voiceServer);
        when(recipientRepository.findByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING))
                .thenReturn(List.of(recipient));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        producer.publishCampaignVoice(CAMPAIGN_ID);

        ArgumentCaptor<CampaignVoiceMessage> messageCaptor = ArgumentCaptor.forClass(CampaignVoiceMessage.class);
        verify(objectMapper).writeValueAsString(messageCaptor.capture());

        CampaignVoiceMessage message = messageCaptor.getValue();
        String expected = "Hello Ada Lovelace. "
                + "Email ada@example.com or ada@example.com, dept Engineering, "
                + "org Aspire, phone +15551234567 or +15551234567, "
                + "location United States.";
        assertEquals(expected, message.getRenderedScript());
        assertEquals("trk-1", message.getTrackingId());
        assertEquals("+15551234567", message.getToPhone());
        assertFalse(message.getRenderedScript().contains("{{"));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
