package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplChannelFilterTest {

    private static final String CLIENT_ID = "client-1";

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
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    @Test
    void getCampaigns_passesChannelToRepositoryFilter() {
        Campaign campaign = Campaign.builder().id("c1").channel(CampaignChannel.VOICE).build();
        CampaignDto dto = CampaignDto.builder().channel(CampaignChannel.VOICE).build();
        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), eq(CampaignChannel.VOICE),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));
        when(campaignMapper.toDto(campaign)).thenReturn(dto);

        List<CampaignDto> result = campaignService.getCampaigns(
                0, 12, null, null, CampaignChannel.VOICE, "createdAt", "desc");

        assertEquals(1, result.size());
        assertEquals(CampaignChannel.VOICE, result.get(0).getChannel());
        verify(campaignRepository).findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), eq(CampaignChannel.VOICE),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    @Test
    void getCampaigns_passesCombinedFiltersToRepository() {
        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), eq("phish"), eq(CampaignStatus.RUNNING), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        campaignService.getCampaigns(1, 12, "phish", CampaignStatus.RUNNING, CampaignChannel.SMS, "createdAt", "asc");

        verify(campaignRepository).findWithFilters(
                eq(CLIENT_ID), eq("phish"), eq(CampaignStatus.RUNNING), eq(CampaignChannel.SMS),
                isNull(), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    @Test
    void countCampaigns_passesChannelToRepositoryFilter() {
        when(campaignRepository.countWithFilters(
                CLIENT_ID, null, null, CampaignChannel.VOICE, null, null, null, false))
                .thenReturn(7L);

        long total = campaignService.countCampaigns(null, null, CampaignChannel.VOICE);

        assertEquals(7L, total);
        verify(campaignRepository).countWithFilters(
                CLIENT_ID, null, null, CampaignChannel.VOICE, null, null, null, false);
    }

    @Test
    void countCampaigns_combinesStatusSearchAndChannel() {
        when(campaignRepository.countWithFilters(
                CLIENT_ID, "phish", CampaignStatus.RUNNING, CampaignChannel.SMS, null, null, null, false))
                .thenReturn(3L);

        long total = campaignService.countCampaigns("phish", CampaignStatus.RUNNING, CampaignChannel.SMS);

        assertEquals(3L, total);
        verify(campaignRepository).countWithFilters(
                CLIENT_ID, "phish", CampaignStatus.RUNNING, CampaignChannel.SMS, null, null, null, false);
    }

    @Test
    void countCampaigns_nullFilters_countsAllForClient() {
        when(campaignRepository.countWithFilters(CLIENT_ID, null, null, null, null, null, null, false))
                .thenReturn(116L);

        assertEquals(116L, campaignService.countCampaigns(null, null, null));
        verify(campaignRepository).countWithFilters(CLIENT_ID, null, null, null, null, null, null, false);
    }
}
