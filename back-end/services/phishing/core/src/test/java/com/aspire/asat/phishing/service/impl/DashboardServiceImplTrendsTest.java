package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.DashboardTrendsDto;
import com.aspire.asat.phishing.dto.response.TrendDataPointDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTrendsTest {

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
    @Mock
    private VishingScenarioRepository vishingScenarioRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardTrendsShouldDefaultToThirtyDayWindow() {
        stubClientContext();
        LocalDate statDate = LocalDate.of(2026, 6, 1);
        EmailMetrics metrics = EmailMetrics.builder()
                .openRate(40.0)
                .clickRate(25.0)
                .submissionRate(12.5)
                .reportRate(5.5)
                .build();
        when(dashboardStatsRepository.findDailyStatsForLastNDays(
                eq(CLIENT_ID), eq(LocalDate.now().minusDays(30)), eq(CampaignChannel.EMAIL)))
                .thenReturn(List.of(DashboardStats.builder()
                        .clientId(CLIENT_ID)
                        .date(statDate)
                        .channel(CampaignChannel.EMAIL)
                        .emailMetrics(metrics)
                        .build()));

        DashboardTrendsDto result = dashboardService.getDashboardTrends(30);

        assertEquals(1, result.getOpenRateTrend().size());
        assertEquals(1, result.getClickRateTrend().size());
        assertEquals(1, result.getSubmissionRateTrend().size());
        assertEquals(1, result.getReportRateTrend().size());
        assertEquals(40.0, result.getOpenRateTrend().get(0).getValue());
        assertEquals(25.0, result.getClickRateTrend().get(0).getValue());
        assertEquals(12.5, result.getSubmissionRateTrend().get(0).getValue());
        assertEquals(5.5, result.getReportRateTrend().get(0).getValue());
        assertEquals(statDate, result.getOpenRateTrend().get(0).getDate());
        verify(dashboardStatsRepository).findDailyStatsForLastNDays(
                CLIENT_ID, LocalDate.now().minusDays(30), CampaignChannel.EMAIL);
    }

    @Test
    void getDashboardTrendsShouldReadStatsForRequestedChannel() {
        stubClientContext();
        LocalDate statDate = LocalDate.of(2026, 6, 2);
        EmailMetrics emailMetrics = EmailMetrics.builder()
                .openRate(99.0)
                .clickRate(99.0)
                .submissionRate(99.0)
                .reportRate(99.0)
                .build();
        EmailMetrics smsMetrics = EmailMetrics.builder()
                .openRate(0.0)
                .clickRate(18.0)
                .submissionRate(9.0)
                .reportRate(0.0)
                .build();
        when(dashboardStatsRepository.findDailyStatsForLastNDays(
                eq(CLIENT_ID), eq(LocalDate.now().minusDays(30)), eq(CampaignChannel.SMS)))
                .thenReturn(List.of(DashboardStats.builder()
                        .clientId(CLIENT_ID)
                        .date(statDate)
                        .emailMetrics(emailMetrics)
                        .smsMetrics(smsMetrics)
                        .build()));

        DashboardTrendsDto result = dashboardService.getDashboardTrends(30, CampaignChannel.SMS);

        assertEquals(0.0, result.getOpenRateTrend().get(0).getValue());
        assertEquals(18.0, result.getClickRateTrend().get(0).getValue());
        verify(dashboardStatsRepository).findDailyStatsForLastNDays(
                CLIENT_ID, LocalDate.now().minusDays(30), CampaignChannel.SMS);
        verify(dashboardStatsRepository, never()).findDailyStatsForLastNDays(
                CLIENT_ID, LocalDate.now().minusDays(30), CampaignChannel.EMAIL);
    }

    @Test
    void getDashboardTrendsShouldUseSevenDayWindowWhenRequested() {
        stubClientContext();
        when(dashboardStatsRepository.findDailyStatsForLastNDays(
                eq(CLIENT_ID), eq(LocalDate.now().minusDays(7)), eq(CampaignChannel.EMAIL)))
                .thenReturn(List.of());

        dashboardService.getDashboardTrends(7);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(dashboardStatsRepository).findDailyStatsForLastNDays(
                eq(CLIENT_ID), startCaptor.capture(), eq(CampaignChannel.EMAIL));
        assertEquals(LocalDate.now().minusDays(7), startCaptor.getValue());
    }

    @Test
    void getDashboardTrendsShouldRejectInvalidDays() {
        assertThrows(PhishingValidationException.class, () -> dashboardService.getDashboardTrends(15));
    }

    @Test
    void getTrendsShouldReturnSingleSeriesFromBundledBuilder() {
        stubClientContext();
        LocalDate statDate = LocalDate.of(2026, 6, 1);
        EmailMetrics metrics = EmailMetrics.builder()
                .openRate(39.9)
                .clickRate(25.0)
                .submissionRate(10.7)
                .reportRate(4.2)
                .build();
        when(dashboardStatsRepository.findDailyStatsForLastNDays(
                eq(CLIENT_ID), eq(LocalDate.now().minusDays(30)), eq(CampaignChannel.EMAIL)))
                .thenReturn(List.of(DashboardStats.builder()
                        .clientId(CLIENT_ID)
                        .date(statDate)
                        .channel(CampaignChannel.EMAIL)
                        .emailMetrics(metrics)
                        .build()));

        List<TrendDataPointDto> openTrend = dashboardService.getTrends("openRate", 30);
        DashboardTrendsDto bundled = dashboardService.getDashboardTrends(30);

        assertEquals(bundled.getOpenRateTrend(), openTrend);
        assertEquals(39.9, openTrend.get(0).getValue());
    }

    private void stubClientContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }
}
