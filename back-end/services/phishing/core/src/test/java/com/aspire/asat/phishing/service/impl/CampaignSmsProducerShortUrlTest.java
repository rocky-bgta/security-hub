package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.service.support.SmsTemplateValidator;
import com.aspire.asat.phishing.service.support.TemplatePersonalizationService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignSmsProducerShortUrlTest {

    private static final String CAMPAIGN_ID = "camp-1";
    private static final String CLIENT_ID = "client-1";
    private static final String SHORT_URL = "https://online-banking.tech/01k7Qm2Nxp";
    private static final String ORIGIN = "https://online-banking.tech";
    private static final String BASE_URL = "https://online-banking.tech/dev/gateway/phishing";
    private static final String QUEUE_URL =
            "https://sqs.us-east-1.amazonaws.com/123/phishing-campaign-sms-dev-queue";

    @Mock private SqsClient sqsClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private CampaignRepository campaignRepository;
    @Mock private EmailTemplateRepository emailTemplateRepository;
    @Mock private CampaignRecipientRepository recipientRepository;
    @Mock private LandingPageRepository landingPageRepository;
    @Mock private TrackingBaseUrlResolver trackingBaseUrlResolver;
    @Mock private SmsServerConfigurationService smsServerConfigurationService;
    @Mock private UrlShortenerService urlShortenerService;

    private CampaignSmsProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        producer = new CampaignSmsProducer(
                sqsClient,
                objectMapper,
                campaignRepository,
                emailTemplateRepository,
                recipientRepository,
                landingPageRepository,
                trackingBaseUrlResolver,
                smsServerConfigurationService,
                new SmsTemplateValidator(new TemplatePersonalizationService()),
                new TemplatePersonalizationService(),
                urlShortenerService);
        setField("queueUrl", QUEUE_URL);
        setField("publishMaxAttempts", 1);
        setField("publishInitialBackoffMs", 1L);
    }

    @Test
    void publishCampaignSms_embedsShortUrlNotLandingPath() throws Exception {
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .emailTemplateId("tpl-1")
                .smsServerConfigurationId("sms-1")
                .trackingDomainId("domain-1")
                .landingPageId("lp-1")
                .campaignName("Bank alert")
                .build();
        EmailTemplate template = EmailTemplate.builder()
                .id("tpl-1")
                .templateType(TemplateType.SMS)
                .smsBody("Verify now: {{tracking_link}}")
                .build();
        CampaignRecipient recipient = CampaignRecipient.builder()
                .id("rec-1")
                .campaignId(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .trackingId("225cb258f8404dde9541e51c3af83662")
                .phoneNumber("+15551234567")
                .status(RecipientStatus.PENDING)
                .build();
        LandingPage landingPage = LandingPage.builder().id("lp-1").trackingDomainId("domain-1").build();

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(emailTemplateRepository.findById("tpl-1")).thenReturn(Optional.of(template));
        when(smsServerConfigurationService.resolveForCampaign(CLIENT_ID, "sms-1"))
                .thenReturn(SmsServerConfiguration.builder().id("sms-1").build());
        when(recipientRepository.findByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING))
                .thenReturn(List.of(recipient));
        when(landingPageRepository.findById("lp-1")).thenReturn(Optional.of(landingPage));
        when(trackingBaseUrlResolver.resolve(CLIENT_ID, "domain-1", "domain-1")).thenReturn(BASE_URL);
        when(trackingBaseUrlResolver.resolveShortLinkOrigin(CLIENT_ID, "domain-1", "domain-1"))
                .thenReturn(Optional.of(ORIGIN));
        when(urlShortenerService.shorten(eq(BASE_URL + "/t/phish/" + recipient.getTrackingId()),
                eq(ORIGIN), eq(recipient))).thenReturn(SHORT_URL);
        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
            CampaignSmsMessage message = invocation.getArgument(0);
            return "{\"messageBody\":\"" + message.getMessageBody() + "\"}";
        });
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("sqs-1").build());

        producer.publishCampaignSms(CAMPAIGN_ID);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        String body = captor.getValue().messageBody();
        assertTrue(body.contains(SHORT_URL));
        assertFalse(body.contains("/t/phish/"));
        assertFalse(body.contains("/gateway/phishing"));
    }

    @Test
    void publishCampaignSms_withoutVerifiedDomain_doesNotEnqueue() {
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .emailTemplateId("tpl-1")
                .smsServerConfigurationId("sms-1")
                .landingPageId("lp-1")
                .build();
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(emailTemplateRepository.findById("tpl-1")).thenReturn(Optional.of(EmailTemplate.builder()
                .id("tpl-1")
                .templateType(TemplateType.SMS)
                .smsBody("Verify now: {{tracking_link}}")
                .build()));
        when(smsServerConfigurationService.resolveForCampaign(CLIENT_ID, "sms-1"))
                .thenReturn(SmsServerConfiguration.builder().id("sms-1").build());
        when(recipientRepository.findByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING))
                .thenReturn(List.of(CampaignRecipient.builder()
                        .id("rec-1")
                        .trackingId("trk-1")
                        .phoneNumber("+15551234567")
                        .status(RecipientStatus.PENDING)
                        .build()));
        when(landingPageRepository.findById("lp-1"))
                .thenReturn(Optional.of(LandingPage.builder().id("lp-1").build()));
        when(trackingBaseUrlResolver.resolve(CLIENT_ID, null, null)).thenReturn(BASE_URL);
        when(trackingBaseUrlResolver.resolveShortLinkOrigin(CLIENT_ID, null, null))
                .thenReturn(Optional.empty());

        producer.publishCampaignSms(CAMPAIGN_ID);

        verify(urlShortenerService, never()).shorten(any(), any(), any());
        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = CampaignSmsProducer.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(producer, value);
    }
}
