package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.dto.response.BreachSummaryDto;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import com.aspire.asat.phishing.dto.response.DashboardOverviewDto;
import com.aspire.asat.phishing.dto.response.DashboardTrendsDto;
import com.aspire.asat.phishing.dto.response.EmailStatsDto;
import com.aspire.asat.phishing.model.AdminDashboardAggregate;
import com.aspire.asat.phishing.model.CohortMetrics;
import com.aspire.asat.phishing.repository.*;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplChannelReportTest {

    private static final String CLIENT_ID = "client-123";

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
    @Mock
    private VishingScenarioRepository vishingScenarioRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private void mockCurrentUserContext() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId(CLIENT_ID)
                .userId(CLIENT_ID)
                .userType("CLIENT_ADMIN")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    @Test
    void getOverview_DefaultEmail_ScopesToEmailChannel() {
        mockCurrentUserContext();
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, null)).thenReturn(10L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.RUNNING)).thenReturn(3L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.COMPLETED)).thenReturn(5L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.DRAFT)).thenReturn(2L);

        when(campaignRollupService.rollupEmailCounters(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(new CampaignEmailCounters(100, 100, 95, 5, 60, 20, 5, 8, 15));
        when(campaignRepository.findIdsByClientIdAndChannel(eq(CLIENT_ID), eq(CampaignChannel.EMAIL)))
                .thenReturn(List.of("camp-email-1"));
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(eq(CLIENT_ID), any(), any(), anyList(), any()))
                .thenReturn(List.of());
        when(adminDashboardAggregateRepository.findTopByClientIdAndPeriodOrderByDateDesc(CLIENT_ID, StatsPeriod.DAILY))
                .thenReturn(Optional.of(AdminDashboardAggregate.builder()
                        .cohortMetrics(CohortMetrics.builder().averagePhishingRiskScore(25.0).build())
                        .build()));
        when(dashboardStatsRepository.findDailyStatsForLastNDays(eq(CLIENT_ID), any(), eq(CampaignChannel.EMAIL)))
                .thenReturn(List.of());

        DashboardOverviewDto overview = dashboardService.getOverview();

        assertNotNull(overview);
        assertEquals(10, overview.getTotalCampaigns());
        assertEquals(3, overview.getActiveCampaigns());
        assertEquals(5, overview.getCompletedCampaigns());
        assertEquals(2, overview.getDraftCampaigns());
        assertEquals(100, overview.getTotalEmailsSent());
        verify(campaignRepository).countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, null);
    }

    @Test
    void getOverview_SmsChannel_ScopesToSmsChannel() {
        mockCurrentUserContext();
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS, null)).thenReturn(4L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS, CampaignStatus.RUNNING)).thenReturn(1L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS, CampaignStatus.COMPLETED)).thenReturn(2L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS, CampaignStatus.DRAFT)).thenReturn(1L);

        when(campaignRollupService.rollupEmailCounters(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(new CampaignEmailCounters(50, 50, 48, 2, 0, 10, 0, 3, 0));
        when(campaignRepository.findIdsByClientIdAndChannel(eq(CLIENT_ID), eq(CampaignChannel.SMS)))
                .thenReturn(List.of("camp-sms-1"));
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(eq(CLIENT_ID), any(), any(), anyList(), any()))
                .thenReturn(List.of());
        when(adminDashboardAggregateRepository.findTopByClientIdAndPeriodOrderByDateDesc(CLIENT_ID, StatsPeriod.DAILY))
                .thenReturn(Optional.empty());
        when(dashboardStatsRepository.findDailyStatsForLastNDays(eq(CLIENT_ID), any(), eq(CampaignChannel.SMS)))
                .thenReturn(List.of());

        DashboardOverviewDto overview = dashboardService.getOverview(CampaignChannel.SMS);

        assertNotNull(overview);
        assertEquals(4, overview.getTotalCampaigns());
        assertEquals(1, overview.getActiveCampaigns());
        assertEquals(2, overview.getCompletedCampaigns());
        assertEquals(1, overview.getDraftCampaigns());
        assertEquals(50, overview.getTotalEmailsSent());
        verify(campaignRepository).countByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS, null);
        verify(campaignRollupService).rollupEmailCounters(CLIENT_ID, CampaignChannel.SMS);
    }

    @Test
    void getEmailStats_SmsChannel_RollsUpSmsCounters() {
        mockCurrentUserContext();
        when(campaignRollupService.rollupEmailCounters(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(new CampaignEmailCounters(80, 80, 78, 2, 0, 16, 0, 4, 0));

        EmailStatsDto stats = dashboardService.getEmailStats(CampaignChannel.SMS);

        assertNotNull(stats);
        assertEquals(80, stats.getTotalRecipients());
        assertEquals(80, stats.getTotalEmailsSent());
        assertEquals(78, stats.getEmailsDelivered());
        assertEquals(16, stats.getLinksClicked());
        assertEquals(4, stats.getDataSubmitted());
        assertEquals(20.0, stats.getClickRate());
        assertEquals(25.0, stats.getCompromiseRate());
        verify(campaignRollupService).rollupEmailCounters(CLIENT_ID, CampaignChannel.SMS);
    }

    @Test
    void getBreachSummary_ReturnsStubData() {
        BreachSummaryDto breachSummary = dashboardService.getBreachSummary(CampaignChannel.VOICE);
        assertNotNull(breachSummary);
        assertEquals(0, breachSummary.getTotalBreaches());
        assertEquals(0, breachSummary.getAffectedUsers());
    }
}
