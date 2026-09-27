package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsSubPackageClient;
import com.aspire.asat.phishing.client.CmsTopicClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplRecipientsPaginationTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "campaign-1";

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
    void getRecipients_usesPageIndexOffset_notDocumentSkip() {
        Campaign campaign = Campaign.builder().id(CAMPAIGN_ID).clientId(CLIENT_ID).build();
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(CampaignDto.builder().campaignId(CAMPAIGN_ID).build());
        when(recipientRepository.findByCampaignId(eq(CAMPAIGN_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.<CampaignRecipient>of()));

        campaignService.getRecipients(CAMPAIGN_ID, 1, 10, null, "email", "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipientRepository).findByCampaignId(eq(CAMPAIGN_ID), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "email")), pageable);
    }
}
