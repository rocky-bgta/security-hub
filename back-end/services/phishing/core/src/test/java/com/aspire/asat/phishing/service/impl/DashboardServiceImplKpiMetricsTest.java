package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.DashboardKpiDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.repository.custom.ClientWindowActivityTotals;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplKpiMetricsTest {

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
    void getKpiMetricsShouldReturnWindowedActivityCountsAndRates() {
        stubClientContext();
        stubEmailKpiDependencies(List.of("camp-email"), new ClientWindowActivityTotals(100, 10, 5), 3L, 12L, 8L);

        DashboardKpiDto result = dashboardService.getKpiMetrics(30);

        assertEquals(100, result.getAttacks());
        assertEquals(10, result.getHacks());
        assertEquals(5, result.getReports());
        assertEquals(3, result.getCampaigns());
        assertEquals(12, result.getTemplates());
        assertEquals(8, result.getLandingPages());
        assertEquals(10.0, result.getCompromiseRate());
        assertEquals(5.0, result.getReportRate());
        verify(campaignRepository).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
        verify(campaignRepository).countLaunchedBetweenByChannel(
                eq(CLIENT_ID), any(), any(), eq(CampaignChannel.EMAIL));
        verify(emailTemplateRepository).countEmailTemplatesByClientIdOrIsGlobal(CLIENT_ID);
        verify(emailTemplateRepository, never()).countSmsTemplatesByClientIdOrIsGlobal(CLIENT_ID);
        verify(vishingScenarioRepository, never()).countByClientIdOrIsGlobal(CLIENT_ID);
    }

    @Test
    void getKpiMetricsShouldDefaultNullChannelToEmail() {
        stubClientContext();
        stubEmailKpiDependencies(List.of("camp-email"), new ClientWindowActivityTotals(20, 2, 1), 1L, 4L, 3L);

        dashboardService.getKpiMetrics(30, null);

        verify(campaignRepository).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
        ArgumentCaptor<DashboardChannelScope.ActivityMapping> mappingCaptor =
                ArgumentCaptor.forClass(DashboardChannelScope.ActivityMapping.class);
        verify(emailActivityRepository).aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-email")), mappingCaptor.capture());
        assertEquals(ActivityType.EMAIL_SENT, mappingCaptor.getValue().sent());
        assertEquals(ActivityType.DATA_SUBMITTED, mappingCaptor.getValue().hack());
        assertEquals(ActivityType.EMAIL_REPORTED, mappingCaptor.getValue().reported());
    }

    @Test
    void getKpiMetricsShouldUseSmsActivityTypesAndSmsTemplates() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms"));
        when(emailActivityRepository.aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms")), any()))
                .thenReturn(new ClientWindowActivityTotals(40, 4, 0));
        when(campaignRepository.countLaunchedBetweenByChannel(
                eq(CLIENT_ID), any(), any(), eq(CampaignChannel.SMS)))
                .thenReturn(2L);
        when(emailTemplateRepository.countSmsTemplatesByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(6L);
        when(landingPageRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(8L);

        DashboardKpiDto result = dashboardService.getKpiMetrics(30, CampaignChannel.SMS);

        assertEquals(40, result.getAttacks());
        assertEquals(4, result.getHacks());
        assertEquals(0, result.getReports());
        assertEquals(2, result.getCampaigns());
        assertEquals(6, result.getTemplates());
        assertEquals(8, result.getLandingPages());
        ArgumentCaptor<DashboardChannelScope.ActivityMapping> mappingCaptor =
                ArgumentCaptor.forClass(DashboardChannelScope.ActivityMapping.class);
        verify(emailActivityRepository).aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms")), mappingCaptor.capture());
        assertEquals(ActivityType.SMS_SENT, mappingCaptor.getValue().sent());
        assertEquals(ActivityType.DATA_SUBMITTED, mappingCaptor.getValue().hack());
        verify(campaignRepository, never()).findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL);
        verify(emailTemplateRepository, never()).countEmailTemplatesByClientIdOrIsGlobal(CLIENT_ID);
        verify(vishingScenarioRepository, never()).countByClientIdOrIsGlobal(CLIENT_ID);
    }

    @Test
    void getKpiMetricsShouldUseVoiceActivityTypesAndScenarioCounts() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.VOICE))
                .thenReturn(List.of("camp-voice"));
        when(emailActivityRepository.aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-voice")), any()))
                .thenReturn(new ClientWindowActivityTotals(15, 3, 1));
        when(campaignRepository.countLaunchedBetweenByChannel(
                eq(CLIENT_ID), any(), any(), eq(CampaignChannel.VOICE)))
                .thenReturn(4L);
        when(vishingScenarioRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(9L);

        DashboardKpiDto result = dashboardService.getKpiMetrics(30, CampaignChannel.VOICE);

        assertEquals(15, result.getAttacks());
        assertEquals(3, result.getHacks());
        assertEquals(1, result.getReports());
        assertEquals(4, result.getCampaigns());
        assertEquals(9, result.getTemplates());
        assertEquals(0, result.getLandingPages());
        ArgumentCaptor<DashboardChannelScope.ActivityMapping> mappingCaptor =
                ArgumentCaptor.forClass(DashboardChannelScope.ActivityMapping.class);
        verify(emailActivityRepository).aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-voice")), mappingCaptor.capture());
        assertEquals(ActivityType.VOICE_INITIATED, mappingCaptor.getValue().sent());
        assertEquals(ActivityType.VOICE_COMPROMISED, mappingCaptor.getValue().hack());
        assertEquals(ActivityType.VOICE_REPORTED, mappingCaptor.getValue().reported());
        verify(landingPageRepository, never()).countByClientIdOrIsGlobal(CLIENT_ID);
        verify(emailTemplateRepository, never()).countEmailTemplatesByClientIdOrIsGlobal(CLIENT_ID);
    }

    @Test
    void getKpiMetricsShouldUseSevenDayWindowWhenRequested() {
        stubClientContext();
        stubEmailKpiDependencies(List.of("camp-email"), ClientWindowActivityTotals.empty(), 0L, 0L, 0L);

        dashboardService.getKpiMetrics(7);

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(emailActivityRepository).aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), startCaptor.capture(), any(), eq(List.of("camp-email")), any());
        long daysBetween = ChronoUnit.DAYS.between(startCaptor.getValue(), Instant.now());
        assertEquals(7, daysBetween, 1);
    }

    @Test
    void getKpiMetricsShouldRejectInvalidDays() {
        assertThrows(PhishingValidationException.class, () -> dashboardService.getKpiMetrics(15));
    }

    @Test
    void getKpiMetricsShouldReturnZerosWhenNoCampaignsForChannel() {
        stubClientContext();
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of());
        when(emailActivityRepository.aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of()), any()))
                .thenReturn(ClientWindowActivityTotals.empty());
        when(campaignRepository.countLaunchedBetweenByChannel(
                eq(CLIENT_ID), any(), any(), eq(CampaignChannel.EMAIL)))
                .thenReturn(0L);
        when(emailTemplateRepository.countEmailTemplatesByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(5L);
        when(landingPageRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(4L);

        DashboardKpiDto result = dashboardService.getKpiMetrics(30);

        assertEquals(0, result.getAttacks());
        assertEquals(0, result.getHacks());
        assertEquals(0, result.getReports());
        assertEquals(0.0, result.getCompromiseRate());
        assertEquals(0.0, result.getReportRate());
        assertEquals(5, result.getTemplates());
        assertEquals(4, result.getLandingPages());
    }

    private void stubEmailKpiDependencies(
            List<String> campaignIds,
            ClientWindowActivityTotals totals,
            long launchedCount,
            long templates,
            long landingPages) {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(campaignIds);
        when(emailActivityRepository.aggregateClientActivityTotalsForWindow(
                eq(CLIENT_ID), any(), any(), eq(campaignIds), any()))
                .thenReturn(totals);
        when(campaignRepository.countLaunchedBetweenByChannel(
                eq(CLIENT_ID), any(), any(), eq(CampaignChannel.EMAIL)))
                .thenReturn(launchedCount);
        when(emailTemplateRepository.countEmailTemplatesByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(templates);
        when(landingPageRepository.countByClientIdOrIsGlobal(CLIENT_ID)).thenReturn(landingPages);
    }

    private void stubClientContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }
}
