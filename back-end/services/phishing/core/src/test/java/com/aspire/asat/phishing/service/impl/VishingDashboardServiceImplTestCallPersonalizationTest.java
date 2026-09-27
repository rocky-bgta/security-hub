package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.CampaignVoiceScenarioRef;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.request.VoiceTestCallRequest;
import com.aspire.asat.phishing.dto.sqs.CampaignVoiceMessage;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.VishingCallLogRepository;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VishingDashboardServiceImplTestCallPersonalizationTest {

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String CLIENT_ID = "client-1";

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private VishingCallLogRepository callLogRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignVoiceConsumer campaignVoiceConsumer;
    @Spy
    private TemplatePersonalizationService templatePersonalizationService = new TemplatePersonalizationService();
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VishingDashboardServiceImpl service;

    @Test
    void sendTestCall_personalizesScriptFromRecipientAudienceData() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());

        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .campaignName("Vishing Campaign")
                .channel(CampaignChannel.VOICE)
                .voiceScenario(CampaignVoiceScenarioRef.builder()
                        .scriptBody("Hi {{FIRST_NAME}} from {{organizationName}} in {{LOCATION}}. "
                                + "Call {{PHONE_NUMBER}} or email {{EMAIL_ADDRESS}}.")
                        .language("en")
                        .build())
                .build();

        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("recipient-1")
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@example.com")
                .organizationName("Navy")
                .phoneNumber("+15557654321")
                .countryName("United States")
                .trackingId("trk-grace")
                .build();

        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(campaign));
        when(recipientRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));

        service.sendTestCall(CAMPAIGN_ID, VoiceTestCallRequest.builder()
                .phoneNumber("+15557654321")
                .recipientId("recipient-1")
                .build());

        ArgumentCaptor<CampaignVoiceMessage> captor = ArgumentCaptor.forClass(CampaignVoiceMessage.class);
        verify(campaignVoiceConsumer).processVoiceMessage(captor.capture());

        assertEquals(
                "Hi Grace from Navy in United States. Call +15557654321 or email grace@example.com.",
                captor.getValue().getRenderedScript());
        assertFalse(captor.getValue().getRenderedScript().contains("{{"));
        assertEquals("trk-grace", captor.getValue().getTrackingId());
    }
}
