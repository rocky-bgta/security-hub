package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.response.PhishingPerformanceDto;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.custom.UserRiskProfileEmailTotals;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplPhishingPerformanceTest {

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
    void getPhishingPerformance_whenNoMspScope_usesGlobalTotals() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .userId("admin-1")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());
        when(userRiskProfileRepository.aggregateEmailTotals())
                .thenReturn(new UserRiskProfileEmailTotals(1000, 600, 120, 80));

        PhishingPerformanceDto result = dashboardService.getPhishingPerformance(null);

        assertEquals(1000L, result.getEmailsReceived());
        assertEquals(120L, result.getEmailsReported());
        assertEquals(400L, result.getEmailsIgnored());
        assertEquals(80L, result.getLinksClicked());
        assertEquals(12.0, result.getReportedPercentage());
        assertEquals(40.0, result.getIgnoredOrNotOpenedPercentage());
        assertEquals(8.0, result.getClickedPercentage());
        verify(userRiskProfileRepository).aggregateEmailTotals();
        verify(registrationServiceClient, never()).getClientAdminIdsByMspId(any());
    }

    @Test
    void getPhishingPerformance_whenMspUserAndNullMspId_scopesToContextUserClients() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .userId("msp-1")
                        .userType(UserType.MSP.name())
                        .build());
        when(registrationServiceClient.getClientAdminIdsByMspId("msp-1"))
                .thenReturn(List.of("ca-1", "ca-2"));
        when(userRiskProfileRepository.aggregateEmailTotalsForClientIds(List.of("ca-1", "ca-2")))
                .thenReturn(new UserRiskProfileEmailTotals(200, 100, 20, 10));

        PhishingPerformanceDto result = dashboardService.getPhishingPerformance(null);

        assertEquals(200L, result.getEmailsReceived());
        assertEquals(20L, result.getEmailsReported());
        assertEquals(100L, result.getEmailsIgnored());
        assertEquals(10L, result.getLinksClicked());
        assertEquals(10.0, result.getReportedPercentage());
        assertEquals(50.0, result.getIgnoredOrNotOpenedPercentage());
        assertEquals(5.0, result.getClickedPercentage());
        verify(userRiskProfileRepository, never()).aggregateEmailTotals();
        verify(registrationServiceClient).getClientAdminIdsByMspId("msp-1");
    }

    @Test
    void getPhishingPerformance_whenMspIdProvided_scopesToThatMspClients() {
        when(registrationServiceClient.getClientAdminIdsByMspId("msp-42"))
                .thenReturn(List.of("ca-9"));
        when(userRiskProfileRepository.aggregateEmailTotalsForClientIds(List.of("ca-9")))
                .thenReturn(new UserRiskProfileEmailTotals(50, 50, 5, 0));

        PhishingPerformanceDto result = dashboardService.getPhishingPerformance("msp-42");

        assertEquals(50L, result.getEmailsReceived());
        assertEquals(5L, result.getEmailsReported());
        assertEquals(0L, result.getEmailsIgnored());
        assertEquals(10.0, result.getReportedPercentage());
        assertEquals(0.0, result.getIgnoredOrNotOpenedPercentage());
        assertEquals(0.0, result.getClickedPercentage());
        verify(registrationServiceClient).getClientAdminIdsByMspId("msp-42");
        verify(userCurrentContextService, never()).getCurrentUserContext();
    }

    @Test
    void getPhishingPerformance_whenNoEmailsReceived_returnsZeroPercentages() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .userId("admin-1")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());
        when(userRiskProfileRepository.aggregateEmailTotals())
                .thenReturn(UserRiskProfileEmailTotals.empty());

        PhishingPerformanceDto result = dashboardService.getPhishingPerformance(null);

        assertEquals(0.0, result.getReportedPercentage());
        assertEquals(0.0, result.getIgnoredOrNotOpenedPercentage());
        assertEquals(0.0, result.getClickedPercentage());
        assertEquals(0L, result.getEmailsIgnored());
    }
}
