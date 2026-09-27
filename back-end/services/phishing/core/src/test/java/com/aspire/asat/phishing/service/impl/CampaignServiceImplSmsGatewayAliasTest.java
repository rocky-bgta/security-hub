package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.util.CampaignScheduleDateTimeParser;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplSmsGatewayAliasTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String SMS_CONFIG_ID = "sms-config-1";
    private static final String SMS_CONFIG_NAME = "Twilio Dev Gateway";
    private static final String SENDER_PROFILE_ID = "sender-1";
    private static final String SENDER_PROFILE_NAME = "Corporate SMTP";

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private RegistrationServiceClient registrationClient;
    @Mock
    private CmsSubPackageClient cmsSubPackageClient;
    @Mock
    private CmsTopicClient cmsTopicClient;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private SmsServerConfigurationRepository smsServerConfigurationRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private UserRiskProfileService userRiskProfileService;
    @Mock
    private CampaignUserRiskProfileAsyncUpdater campaignUserRiskProfileAsyncUpdater;
    @Mock
    private CampaignScheduleDateTimeParser scheduleDateTimeParser;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @BeforeEach
    void stubClientContext() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    @Test
    void getCampaignById_aliasesSmsGatewayIntoSenderProfileFields() {
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.SMS)
                .smsServerConfigurationId(SMS_CONFIG_ID)
                .build();
        CampaignDto dto = CampaignDto.builder()
                .channel(CampaignChannel.SMS)
                .smsServerConfigurationId(SMS_CONFIG_ID)
                .build();
        SmsServerConfiguration smsConfig = SmsServerConfiguration.builder()
                .id(SMS_CONFIG_ID)
                .clientId(CLIENT_ID)
                .name(SMS_CONFIG_NAME)
                .build();

        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID)).thenReturn(Optional.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(dto);
        when(smsServerConfigurationRepository.findByIdAndClientId(SMS_CONFIG_ID, CLIENT_ID))
                .thenReturn(Optional.of(smsConfig));

        CampaignDto result = campaignService.getCampaignById(CAMPAIGN_ID);

        assertEquals(SMS_CONFIG_ID, result.getSenderProfileId());
        assertEquals(SMS_CONFIG_NAME, result.getSenderProfileName());
        assertEquals(SMS_CONFIG_ID, result.getSmsServerConfigurationId());
        assertEquals(SMS_CONFIG_NAME, result.getSmsServerConfigurationName());
        verify(senderProfileRepository, never()).findByIdAndClientIdOrGlobal(any(), any());
    }

    @Test
    void getCampaignById_usesSenderProfileForEmailCampaign() {
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.EMAIL)
                .senderProfileId(SENDER_PROFILE_ID)
                .build();
        CampaignDto dto = CampaignDto.builder()
                .channel(CampaignChannel.EMAIL)
                .senderProfileId(SENDER_PROFILE_ID)
                .build();
        SenderProfile senderProfile = SenderProfile.builder()
                .id(SENDER_PROFILE_ID)
                .profileName(SENDER_PROFILE_NAME)
                .build();

        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID)).thenReturn(Optional.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(dto);
        when(senderProfileRepository.findByIdAndClientIdOrGlobal(SENDER_PROFILE_ID, CLIENT_ID))
                .thenReturn(Optional.of(senderProfile));

        CampaignDto result = campaignService.getCampaignById(CAMPAIGN_ID);

        assertEquals(SENDER_PROFILE_ID, result.getSenderProfileId());
        assertEquals(SENDER_PROFILE_NAME, result.getSenderProfileName());
        assertNull(result.getSmsServerConfigurationId());
        assertNull(result.getSmsServerConfigurationName());
        verify(smsServerConfigurationRepository, never()).findByIdAndClientId(any(), any());
    }

    @Test
    void getCampaigns_batchAliasesSmsGatewayIntoSenderProfileFields() {
        Campaign smsCampaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.SMS)
                .smsServerConfigurationId(SMS_CONFIG_ID)
                .build();
        CampaignDto dto = CampaignDto.builder()
                .channel(CampaignChannel.SMS)
                .smsServerConfigurationId(SMS_CONFIG_ID)
                .build();
        SmsServerConfiguration smsConfig = SmsServerConfiguration.builder()
                .id(SMS_CONFIG_ID)
                .clientId(CLIENT_ID)
                .name(SMS_CONFIG_NAME)
                .build();
        Page<Campaign> page = new PageImpl<>(List.of(smsCampaign));

        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), eq(null), eq(null), eq(null),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(page);
        when(campaignMapper.toDto(smsCampaign)).thenReturn(dto);
        when(smsServerConfigurationRepository.findAllById(Set.of(SMS_CONFIG_ID))).thenReturn(List.of(smsConfig));

        List<CampaignDto> results = campaignService.getCampaigns(0, 10, null, null, null, null, null);

        assertEquals(1, results.size());
        CampaignDto result = results.get(0);
        assertEquals(SMS_CONFIG_ID, result.getSenderProfileId());
        assertEquals(SMS_CONFIG_NAME, result.getSenderProfileName());
        assertEquals(SMS_CONFIG_ID, result.getSmsServerConfigurationId());
        assertEquals(SMS_CONFIG_NAME, result.getSmsServerConfigurationName());
    }
}
