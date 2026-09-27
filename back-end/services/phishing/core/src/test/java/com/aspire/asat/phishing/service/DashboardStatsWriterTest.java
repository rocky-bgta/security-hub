package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.model.UserRiskMetrics;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStatsWriterTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private DashboardStatsRepository dashboardStatsRepository;

    @InjectMocks
    private DashboardStatsWriter dashboardStatsWriter;

    @Test
    void upsertDailyStatsShouldCreateNewDocumentWhenMissing() {
        LocalDate date = LocalDate.of(2026, 5, 18);
        EmailMetrics emailMetrics = EmailMetrics.builder().emailsOpened(10).linksClicked(3).build();
        emailMetrics.calculateRates();
        CampaignMetrics campaignMetrics = CampaignMetrics.builder().totalCampaigns(2).build();
        UserRiskMetrics userRiskMetrics = UserRiskMetrics.builder().totalUsers(5).build();

        when(dashboardStatsRepository.findByClientIdAndPeriodAndDate(CLIENT_ID, StatsPeriod.DAILY, date))
                .thenReturn(Optional.empty());

        dashboardStatsWriter.upsertDailyStats(CLIENT_ID, date, emailMetrics, campaignMetrics, userRiskMetrics);

        ArgumentCaptor<DashboardStats> captor = ArgumentCaptor.forClass(DashboardStats.class);
        verify(dashboardStatsRepository).save(captor.capture());
        DashboardStats saved = captor.getValue();

        assertEquals(CLIENT_ID, saved.getClientId());
        assertEquals(StatsPeriod.DAILY, saved.getPeriod());
        assertEquals(date, saved.getDate());
        assertEquals(CampaignChannel.EMAIL, saved.getChannel());
        assertEquals(emailMetrics, saved.getEmailMetrics());
        assertEquals(campaignMetrics, saved.getCampaignMetrics());
        assertEquals(userRiskMetrics, saved.getUserRiskMetrics());
        assertNotNull(saved.getUpdatedAt());
        verify(dashboardStatsRepository).findByClientIdAndPeriodAndDate(eq(CLIENT_ID), eq(StatsPeriod.DAILY), eq(date));
    }

    @Test
    void upsertDailyStatsShouldUpdateExistingEmailDocument() {
        LocalDate date = LocalDate.of(2026, 5, 18);
        DashboardStats existing = DashboardStats.builder()
                .id("existing-id")
                .clientId(CLIENT_ID)
                .period(StatsPeriod.DAILY)
                .date(date)
                .build();

        EmailMetrics emailMetrics = EmailMetrics.builder().emailsOpened(20).build();
        emailMetrics.calculateRates();

        when(dashboardStatsRepository.findByClientIdAndPeriodAndDate(CLIENT_ID, StatsPeriod.DAILY, date))
                .thenReturn(Optional.of(existing));

        dashboardStatsWriter.upsertDailyStats(
                CLIENT_ID,
                date,
                emailMetrics,
                CampaignMetrics.builder().totalCampaigns(3).build(),
                UserRiskMetrics.builder().totalUsers(8).build());

        ArgumentCaptor<DashboardStats> captor = ArgumentCaptor.forClass(DashboardStats.class);
        verify(dashboardStatsRepository).save(captor.capture());

        assertEquals("existing-id", captor.getValue().getId());
        assertEquals(20, captor.getValue().getEmailMetrics().getEmailsOpened());
        verify(dashboardStatsRepository).findByClientIdAndPeriodAndDate(eq(CLIENT_ID), eq(StatsPeriod.DAILY), eq(date));
    }

    @Test
    void upsertDailyStatsShouldStoreSmsSeriesWithoutReplacingEmailMetrics() {
        LocalDate date = LocalDate.of(2026, 5, 18);
        EmailMetrics existingEmail = EmailMetrics.builder().emailsOpened(7).build();
        DashboardStats existing = DashboardStats.builder()
                .id("legacy-id")
                .clientId(CLIENT_ID)
                .period(StatsPeriod.DAILY)
                .date(date)
                .emailMetrics(existingEmail)
                .build();
        EmailMetrics smsMetrics = EmailMetrics.builder().linksClicked(4).dataSubmitted(1).build();
        smsMetrics.calculateRates();

        when(dashboardStatsRepository.findByClientIdAndPeriodAndDate(CLIENT_ID, StatsPeriod.DAILY, date))
                .thenReturn(Optional.of(existing));

        dashboardStatsWriter.upsertDailyStats(
                CLIENT_ID, date, CampaignChannel.SMS, smsMetrics,
                CampaignMetrics.builder().totalCampaigns(2).build(),
                UserRiskMetrics.builder().build());

        ArgumentCaptor<DashboardStats> captor = ArgumentCaptor.forClass(DashboardStats.class);
        verify(dashboardStatsRepository).save(captor.capture());
        DashboardStats saved = captor.getValue();
        assertEquals("legacy-id", saved.getId());
        assertEquals(7, saved.getEmailMetrics().getEmailsOpened());
        assertEquals(smsMetrics, saved.getSmsMetrics());
        assertEquals(2, saved.getSmsCampaignMetrics().getTotalCampaigns());
        assertNull(saved.getVoiceMetrics());
    }
}
