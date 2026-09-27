package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignSchedule;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.service.support.RecipientResolver;
import com.aspire.asat.phishing.service.support.RecipientResolverFactory;
import com.aspire.asat.phishing.service.support.SmsTemplateValidator;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplSmsShortUrlTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "camp-1";

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignRecipientRepository recipientRepository;
    @Mock private CampaignMapper campaignMapper;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;
    @Mock private RecipientResolverFactory recipientResolverFactory;
    @Mock private RecipientResolver recipientResolver;
    @Mock private SmsServerConfigurationService smsServerConfigurationService;
    @Mock private SmsTemplateValidator smsTemplateValidator;
    @Mock private EmailTemplateRepository emailTemplateRepository;
    @Mock private LandingPageRepository landingPageRepository;
    @Mock private TrackingBaseUrlResolver trackingBaseUrlResolver;
    @Mock private UrlShortenerService urlShortenerService;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private Campaign campaign;

    @Test
    void launchCampaign_smsWithoutTrackingDomain_throws() {
        stubLaunchableSmsCampaign();
        when(trackingBaseUrlResolver.resolveShortLinkOrigin(CLIENT_ID, null, null))
                .thenReturn(Optional.empty());

        PhishingValidationException ex = assertThrows(PhishingValidationException.class,
                () -> campaignService.launchCampaign(CAMPAIGN_ID));

        assertTrue(ex.getMessage().contains("verified tracking domain"));
        verify(campaignDeliveryOrchestrator, never()).publishCampaign(any());
        verify(urlShortenerService, never()).previewUrl(any());
    }

    @Test
    void launchCampaign_smsValidatesLengthAgainstShortUrl() {
        stubLaunchableSmsCampaign();
        when(trackingBaseUrlResolver.resolveShortLinkOrigin(CLIENT_ID, "domain-1", null))
                .thenReturn(Optional.of("https://online-banking.tech"));
        when(urlShortenerService.previewUrl("https://online-banking.tech"))
                .thenReturn("https://online-banking.tech/01XXXXXXXX");
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(campaignMapper.toDto(any(Campaign.class))).thenReturn(null);

        campaign.setTrackingDomainId("domain-1");

        campaignService.launchCampaign(CAMPAIGN_ID);

        verify(smsTemplateValidator).validateRenderedLength(
                eq("Click {{tracking_link}}"),
                eq("https://online-banking.tech/01XXXXXXXX"),
                any(CampaignRecipient.class));
        verify(urlShortenerService).previewUrl("https://online-banking.tech");
    }

    private void stubLaunchableSmsCampaign() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());

        campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .campaignName("SMS")
                .campaignType(CampaignType.SMISHING_SIMULATION)
                .channel(CampaignChannel.SMS)
                .status(CampaignStatus.DRAFT)
                .currentStep(9)
                .productPackageId("pkg-1")
                .emailTemplateId("tpl-1")
                .smsServerConfigurationId("sms-1")
                .schedule(CampaignSchedule.builder().build())
                .stats(new CampaignStats())
                .build();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID)).thenReturn(Optional.of(campaign));

        CampaignRecipient sample = CampaignRecipient.builder()
                .id("rec-1")
                .trackingId("trk-1")
                .build();
        when(recipientRepository.findByCampaignId(CAMPAIGN_ID)).thenReturn(List.of(sample));
        when(recipientResolverFactory.getResolver(CampaignChannel.SMS)).thenReturn(recipientResolver);
        when(emailTemplateRepository.findById("tpl-1")).thenReturn(Optional.of(EmailTemplate.builder()
                .id("tpl-1")
                .templateType(TemplateType.SMS)
                .smsBody("Click {{tracking_link}}")
                .build()));
        when(smsServerConfigurationService.resolveForCampaign(CLIENT_ID, "sms-1"))
                .thenReturn(SmsServerConfiguration.builder().id("sms-1").build());
    }
}
