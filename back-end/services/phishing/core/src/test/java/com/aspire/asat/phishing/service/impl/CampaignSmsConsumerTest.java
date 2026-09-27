package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.CampaignSmsDeliveryRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsProviderFactory;
import com.aspire.asat.phishing.sms.SmsSendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignSmsConsumerTest {

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String RECIPIENT_ID = "recipient-1";
    private static final String TRACKING_ID = "tracking-1";
    private static final String CONFIG_ID = "config-1";

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private CampaignSmsDeliveryRepository campaignSmsDeliveryRepository;
    @Mock
    private SmsServerConfigurationRepository smsServerConfigurationRepository;
    @Mock
    private SmsProviderFactory smsProviderFactory;
    @Mock
    private TrackingServiceImpl trackingService;
    @Mock
    private CampaignCompletionEvaluator completionEvaluator;
    @Mock
    private SmsProvider smsProvider;
    @Mock
    private SmsServerConfiguration smsServerConfiguration;

    @InjectMocks
    private CampaignSmsConsumer consumer;

    private CampaignSmsMessage message;

    @BeforeEach
    void setUp() {
        message = CampaignSmsMessage.builder()
                .campaignId(CAMPAIGN_ID)
                .recipientId(RECIPIENT_ID)
                .trackingId(TRACKING_ID)
                .clientId("client-1")
                .toPhone("+10000000000")
                .messageBody("hello")
                .smsServerConfigurationId(CONFIG_ID)
                .campaignName("c")
                .build();
    }

    private Campaign runningCampaign() {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId("client-1")
                .campaignName("c")
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .stats(new CampaignStats())
                .build();
    }

    @Test
    void processSmsMessage_SuccessfulSend_DelegatesCompletionToEvaluator() {
        Campaign campaign = runningCampaign();
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(smsServerConfigurationRepository.findById(CONFIG_ID))
                .thenReturn(Optional.of(smsServerConfiguration));
        when(smsProviderFactory.create(smsServerConfiguration)).thenReturn(smsProvider);
        when(smsProvider.send(anyString(), anyString())).thenReturn(SmsSendResult.ok("msg-1"));
        when(recipientRepository.findById(RECIPIENT_ID))
                .thenReturn(Optional.of(CampaignRecipient.builder().id(RECIPIENT_ID).build()));
        when(campaignSmsDeliveryRepository.findByCampaignIdAndRecipientId(CAMPAIGN_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        consumer.processSmsMessage(message);

        verify(recipientRepository).markSentByIdOrTrackingId(eq(RECIPIENT_ID), eq(TRACKING_ID), any(Instant.class));
        verify(completionEvaluator).evaluateAndApply(campaign);
        // The consumer must NOT hand-roll completion; status is left to the evaluator (mocked = no change).
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
        assertEquals(1, campaign.getStats().getSmsSent());
    }

    @Test
    void processSmsMessage_FailedSend_MarksBouncedAndDelegatesCompletion() {
        Campaign campaign = runningCampaign();
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(smsServerConfigurationRepository.findById(CONFIG_ID))
                .thenReturn(Optional.of(smsServerConfiguration));
        when(smsProviderFactory.create(smsServerConfiguration)).thenReturn(smsProvider);
        when(smsProvider.send(anyString(), anyString()))
                .thenReturn(SmsSendResult.fail("SMS_ERROR", "provider rejected"));
        when(campaignSmsDeliveryRepository.findByCampaignIdAndRecipientId(CAMPAIGN_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        consumer.processSmsMessage(message);

        verify(recipientRepository).markBouncedByIdOrTrackingId(eq(RECIPIENT_ID), eq(TRACKING_ID), any(Instant.class));
        verify(completionEvaluator).evaluateAndApply(campaign);
        assertEquals(1, campaign.getStats().getSmsFailed());
    }

    @Test
    void processSmsMessage_CompletedCampaign_SkipsSending() {
        Campaign campaign = runningCampaign();
        campaign.setStatus(CampaignStatus.COMPLETED);
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));

        consumer.processSmsMessage(message);

        verifyNoInteractions(smsServerConfigurationRepository);
        verifyNoInteractions(smsProviderFactory);
        verifyNoInteractions(completionEvaluator);
        verify(recipientRepository, never())
                .markSentByIdOrTrackingId(anyString(), anyString(), any(Instant.class));
    }

    @Test
    void processSmsMessage_ExpiredCampaign_SkipsSending() {
        Campaign campaign = runningCampaign();
        campaign.setExpiresAt(Instant.now().minusSeconds(60));
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));

        consumer.processSmsMessage(message);

        verifyNoInteractions(smsProviderFactory);
        verifyNoInteractions(completionEvaluator);
        verify(smsProvider, never()).send(anyString(), anyString());
    }

    @Test
    void processSmsMessage_SuccessfulSend_UpdatesRiskProfile() {
        Campaign campaign = runningCampaign();
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id(RECIPIENT_ID)
                .status(RecipientStatus.PENDING)
                .build();
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(smsServerConfigurationRepository.findById(CONFIG_ID))
                .thenReturn(Optional.of(smsServerConfiguration));
        when(smsProviderFactory.create(smsServerConfiguration)).thenReturn(smsProvider);
        when(smsProvider.send(anyString(), anyString())).thenReturn(SmsSendResult.ok("msg-1"));
        when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(campaignSmsDeliveryRepository.findByCampaignIdAndRecipientId(CAMPAIGN_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        consumer.processSmsMessage(message);

        verify(trackingService, times(1))
                .updateUserRiskProfilePhishingScore(eq(recipient), any());
    }

    @Test
    void processSmsMessage_SuccessfulSend_StoresRecipientEmailOnActivityNotPhone() {
        Campaign campaign = runningCampaign();
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id(RECIPIENT_ID)
                .email("xtrail-dev-portal-u2@yopmail.com")
                .phoneNumber("+10000000000")
                .status(RecipientStatus.PENDING)
                .build();
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(smsServerConfigurationRepository.findById(CONFIG_ID))
                .thenReturn(Optional.of(smsServerConfiguration));
        when(smsProviderFactory.create(smsServerConfiguration)).thenReturn(smsProvider);
        when(smsProvider.send(anyString(), anyString())).thenReturn(SmsSendResult.ok("msg-1"));
        when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(campaignSmsDeliveryRepository.findByCampaignIdAndRecipientId(CAMPAIGN_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        consumer.processSmsMessage(message);

        ArgumentCaptor<EmailActivity> activityCaptor = ArgumentCaptor.forClass(EmailActivity.class);
        verify(emailActivityRepository).save(activityCaptor.capture());
        EmailActivity activity = activityCaptor.getValue();
        assertEquals("xtrail-dev-portal-u2@yopmail.com", activity.getRecipientEmail());
        assertEquals(ActivityType.SMS_SENT, activity.getActivityType());
        assertEquals("+10000000000", activity.getMetadata().get("phoneNumber"));
    }
}
