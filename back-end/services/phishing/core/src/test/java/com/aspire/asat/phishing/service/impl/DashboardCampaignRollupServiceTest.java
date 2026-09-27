package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardCampaignRollupServiceTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private DashboardCampaignRollupServiceImpl service;

    @Test
    void rollupEmailCountersShouldSumCampaignStats() {
        Campaign c1 = Campaign.builder()
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.EMAIL)
                .stats(CampaignStats.builder()
                        .totalRecipients(100)
                        .emailsSent(90)
                        .emailsDelivered(85)
                        .emailsOpened(40)
                        .linksClicked(10)
                        .dataSubmitted(2)
                        .emailsReported(5)
                        .build())
                .build();
        Campaign c2 = Campaign.builder()
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.EMAIL)
                .stats(CampaignStats.builder()
                        .totalRecipients(50)
                        .emailsSent(45)
                        .emailsDelivered(40)
                        .emailsOpened(20)
                        .linksClicked(5)
                        .dataSubmitted(1)
                        .emailsReported(2)
                        .build())
                .build();

        Page<Campaign> page = new PageImpl<>(List.of(c1, c2));
        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), eq(CampaignChannel.EMAIL), any(Pageable.class)))
                .thenReturn(page);

        CampaignEmailCounters counters = service.rollupEmailCounters(CLIENT_ID);

        assertEquals(150, counters.totalRecipients());
        assertEquals(135, counters.totalEmailsSent());
        assertEquals(125, counters.emailsDelivered());
        assertEquals(60, counters.emailsOpened());
        assertEquals(15, counters.linksClicked());
        assertEquals(3, counters.dataSubmitted());
        assertEquals(7, counters.emailsReported());
    }

    @Test
    void rollupEmailCountersShouldMapVoiceStatsToSharedCounters() {
        Campaign voice = Campaign.builder()
                .clientId(CLIENT_ID)
                .channel(CampaignChannel.VOICE)
                .stats(CampaignStats.builder()
                        .totalRecipients(20)
                        .callsTotal(18)
                        .callsAnswered(12)
                        .callsFailed(2)
                        .callsEngaged(6)
                        .callsCompromised(3)
                        .callsReported(1)
                        .emailsSent(99)
                        .build())
                .build();
        when(campaignRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), eq(CampaignChannel.VOICE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(voice)));

        CampaignEmailCounters counters = service.rollupEmailCounters(CLIENT_ID, CampaignChannel.VOICE);

        assertEquals(20, counters.totalRecipients());
        assertEquals(18, counters.totalEmailsSent());
        assertEquals(12, counters.emailsDelivered());
        assertEquals(12, counters.emailsOpened());
        assertEquals(2, counters.emailsBounced());
        assertEquals(6, counters.linksClicked());
        assertEquals(3, counters.dataSubmitted());
        assertEquals(1, counters.emailsReported());
        verify(campaignRepository, never()).findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), eq(CampaignChannel.EMAIL), any(Pageable.class));
    }

    @Test
    void toEmailMetricsShouldCalculateRatesForTrends() {
        CampaignEmailCounters counters = new CampaignEmailCounters(
                100, 90, 85, 5, 40, 10, 0, 2, 5);

        EmailMetrics metrics = service.toEmailMetrics(counters);

        assertEquals(40.0, metrics.getOpenRate());
        assertEquals(10.0, metrics.getClickRate());
        assertEquals(2.0, metrics.getSubmissionRate());
        assertEquals(5.0, metrics.getReportRate());
        assertEquals(11.1, metrics.getPhishPronePercentage(), 0.05);
    }

    @Test
    void buildCampaignMetricsShouldUseChannelScopedCounts() {
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, null)).thenReturn(10L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.RUNNING))
                .thenReturn(2L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.COMPLETED))
                .thenReturn(5L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.DRAFT))
                .thenReturn(1L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.SCHEDULED))
                .thenReturn(1L);
        when(campaignRepository.countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, CampaignStatus.CANCELLED))
                .thenReturn(1L);

        CampaignMetrics metrics = service.buildCampaignMetrics(CLIENT_ID);

        assertEquals(10, metrics.getTotalCampaigns());
        assertEquals(2, metrics.getActiveCampaigns());
        assertEquals(5, metrics.getCompletedCampaigns());
        assertEquals(1, metrics.getDraftCampaigns());
        verify(campaignRepository).countByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL, null);
    }
}
