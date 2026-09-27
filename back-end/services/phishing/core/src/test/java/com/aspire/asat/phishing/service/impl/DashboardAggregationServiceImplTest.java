package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.AdminDashboardAggregate;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.DashboardAggregationAudit;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.DashboardAggregationAuditRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.DashboardStatsWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DashboardAggregationServiceImplTest {

    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private AdminDashboardAggregateRepository adminDashboardAggregateRepository;
    @Mock
    private DashboardAggregationAuditRepository dashboardAggregationAuditRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private DashboardCampaignRollupService campaignRollupService;
    @Mock
    private DashboardStatsWriter dashboardStatsWriter;

    @InjectMocks
    private DashboardAggregationServiceImpl service;

    @Test
    void aggregateForClientShouldComputeAndPersistCohortMetrics() {
        setField(service, "rollingWindowDays", 90);
        String clientId = "client-1";

        UserRiskProfile p1 = UserRiskProfile.builder()
                .clientId(clientId)
                .userId("u1")
                .email("u1@asat.com")
                .phishingRiskScore(60.0)
                .riskLevel(RiskLevel.HIGH)
                .emailsReceived(10)
                .linksClicked(6)
                .dataSubmissions(3)
                .emailsReported(2)
                .build();
        UserRiskProfile p2 = UserRiskProfile.builder()
                .clientId(clientId)
                .userId("u2")
                .email("u2@asat.com")
                .phishingRiskScore(40.0)
                .riskLevel(RiskLevel.MEDIUM)
                .emailsReceived(10)
                .linksClicked(4)
                .dataSubmissions(0)
                .emailsReported(1)
                .build();

        DashboardAggregationAudit previous = DashboardAggregationAudit.builder()
                .clientId(clientId)
                .currentAvgHrs(55.0)
                .currentReportRatePercent(20.0)
                .build();

        when(userRiskProfileRepository.findByClientId(clientId)).thenReturn(List.of(p1, p2));
        when(dashboardAggregationAuditRepository.findTopByClientIdOrderByRunAtDesc(clientId))
                .thenReturn(Optional.of(previous));
        when(adminDashboardAggregateRepository.findByClientIdAndPeriodAndDate(eq(clientId), eq(StatsPeriod.DAILY), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        CampaignEmailCounters counters = new CampaignEmailCounters(10, 8, 7, 0, 4, 2, 0, 1, 1);
        EmailMetrics emailMetrics = EmailMetrics.builder().emailsOpened(4).linksClicked(2).dataSubmitted(1).build();
        emailMetrics.calculateRates();
        when(campaignRollupService.rollupEmailCounters(eq(clientId), any(CampaignChannel.class))).thenReturn(counters);
        when(campaignRollupService.toEmailMetrics(counters)).thenReturn(emailMetrics);
        when(campaignRollupService.buildCampaignMetrics(eq(clientId), any(CampaignChannel.class)))
                .thenReturn(CampaignMetrics.builder().totalCampaigns(1).build());

        service.aggregateForClient(clientId);

        ArgumentCaptor<AdminDashboardAggregate> statsCaptor = ArgumentCaptor.forClass(AdminDashboardAggregate.class);
        verify(adminDashboardAggregateRepository).save(statsCaptor.capture());
        AdminDashboardAggregate savedStats = statsCaptor.getValue();

        assertEquals(2, savedStats.getCohortMetrics().getTotalUsers());
        assertEquals(50.0, savedStats.getCohortMetrics().getAveragePhishingRiskScore());
        assertEquals(RiskLevel.MEDIUM, savedStats.getCohortMetrics().getCohortRiskLevel());
        assertEquals(1, savedStats.getCohortMetrics().getPhishProneCriticalCount());
        assertEquals(0, savedStats.getCohortMetrics().getPhishProneHighCount());
        assertEquals(3, savedStats.getCohortMetrics().getInformationSubmitCount());
        assertEquals(15.0, savedStats.getCohortMetrics().getReportRatePercent());
        assertEquals("DOWN", savedStats.getCohortMetrics().getReportRateDirection());
        assertEquals("Improving", savedStats.getCohortMetrics().getRiskTrendStatus());
        assertEquals("UP", savedStats.getCohortMetrics().getRiskTrendDirection());
        assertEquals(2, savedStats.getTopRiskUsers().size());
        assertEquals("u1", savedStats.getTopRiskUsers().get(0).getUserId());

        ArgumentCaptor<DashboardAggregationAudit> auditCaptor = ArgumentCaptor.forClass(DashboardAggregationAudit.class);
        verify(dashboardAggregationAuditRepository).save(auditCaptor.capture());
        assertEquals(50.0, auditCaptor.getValue().getCurrentAvgHrs());
        assertEquals(55.0, auditCaptor.getValue().getPreviousAvgHrs());
        assertNotNull(auditCaptor.getValue().getRunAt());

        verify(dashboardStatsWriter, times(3)).upsertDailyStats(
                eq(clientId),
                any(LocalDate.class),
                any(CampaignChannel.class),
                eq(emailMetrics),
                any(CampaignMetrics.class),
                any());
    }

    @Test
    void aggregateForClientShouldHandleZeroDivisionCases() {
        setField(service, "rollingWindowDays", 90);
        String clientId = "client-2";
        UserRiskProfile profile = UserRiskProfile.builder()
                .clientId(clientId)
                .userId("u1")
                .phishingRiskScore(0.0)
                .riskLevel(RiskLevel.LOW)
                .emailsReceived(0)
                .linksClicked(0)
                .dataSubmissions(0)
                .emailsReported(0)
                .build();

        when(userRiskProfileRepository.findByClientId(clientId)).thenReturn(List.of(profile));
        when(dashboardAggregationAuditRepository.findTopByClientIdOrderByRunAtDesc(clientId))
                .thenReturn(Optional.empty());
        when(adminDashboardAggregateRepository.findByClientIdAndPeriodAndDate(eq(clientId), eq(StatsPeriod.DAILY), any(LocalDate.class)))
                .thenReturn(Optional.of(AdminDashboardAggregate.builder().clientId(clientId).period(StatsPeriod.DAILY).date(LocalDate.now()).build()));

        CampaignEmailCounters counters = CampaignEmailCounters.empty();
        EmailMetrics emailMetrics = mock(EmailMetrics.class);
        when(campaignRollupService.rollupEmailCounters(eq(clientId), any(CampaignChannel.class))).thenReturn(counters);
        when(campaignRollupService.toEmailMetrics(counters)).thenReturn(emailMetrics);
        when(campaignRollupService.buildCampaignMetrics(eq(clientId), any(CampaignChannel.class)))
                .thenReturn(CampaignMetrics.builder().build());

        service.aggregateForClient(clientId);

        ArgumentCaptor<AdminDashboardAggregate> statsCaptor = ArgumentCaptor.forClass(AdminDashboardAggregate.class);
        verify(adminDashboardAggregateRepository).save(statsCaptor.capture());
        AdminDashboardAggregate savedStats = statsCaptor.getValue();
        assertEquals(0.0, savedStats.getCohortMetrics().getReportRatePercent());
        assertEquals("FLAT", savedStats.getCohortMetrics().getReportRateDirection());
        assertEquals("Stable", savedStats.getCohortMetrics().getRiskTrendStatus());
        assertEquals("FLAT", savedStats.getCohortMetrics().getRiskTrendDirection());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
