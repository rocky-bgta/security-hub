package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.AssetInventoryCountsDto;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplAssetInventoryCountsTest {

    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private AdminDashboardAggregateRepository adminDashboardAggregateRepository;
    @Mock
    private DashboardStatsRepository dashboardStatsRepository;
    @Mock
    private DashboardAggregationService dashboardAggregationService;
    @Mock
    private DashboardCampaignRollupService campaignRollupService;
    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getAssetInventoryCounts_returnsTotalCountsFromEachCollection() {
        when(campaignRepository.count()).thenReturn(190L);
        when(emailTemplateRepository.count()).thenReturn(150L);
        when(senderProfileRepository.count()).thenReturn(100L);
        when(landingPageRepository.count()).thenReturn(53L);

        AssetInventoryCountsDto result = dashboardService.getAssetInventoryCounts();

        assertEquals(190L, result.getCampaignPresets());
        assertEquals(150L, result.getEmailTemplates());
        assertEquals(100L, result.getSendingProfiles());
        assertEquals(53L, result.getLandingPages());
        verify(campaignRepository).count();
        verify(emailTemplateRepository).count();
        verify(senderProfileRepository).count();
        verify(landingPageRepository).count();
    }
}
