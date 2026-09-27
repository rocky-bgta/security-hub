package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.response.ClientDashboardAssetCountsDto;
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
class DashboardServiceImplAssetCountsTest {

    private static final String CLIENT_ID = "client-1";

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
    void getAssetCountsShouldReturnCountsForCurrentClient() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(emailTemplateRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(5L);
        when(landingPageRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(3L);
        when(senderProfileRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(2L);

        ClientDashboardAssetCountsDto result = dashboardService.getAssetCounts();

        assertEquals(5, result.getNumberOfEmailTemplates());
        assertEquals(3, result.getNumberOfLandingPages());
        assertEquals(2, result.getNumberOfSenderProfiles());
        verify(emailTemplateRepository).countByClientIdOrIsGlobal(CLIENT_ID);
        verify(landingPageRepository).countByClientIdOrIsGlobal(CLIENT_ID);
        verify(senderProfileRepository).countByClientIdOrIsGlobal(CLIENT_ID);
    }
}
