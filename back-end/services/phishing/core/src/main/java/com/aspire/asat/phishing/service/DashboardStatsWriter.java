package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.model.UserRiskMetrics;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Persists pre-computed daily rows into {@code dashboard_stats}.
 * One document per {@code (clientId, period, date)}; channel series are nested so the
 * existing unique index is preserved.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardStatsWriter {

    private final DashboardStatsRepository dashboardStatsRepository;

    public void upsertDailyStats(
            String clientId,
            LocalDate date,
            EmailMetrics emailMetrics,
            CampaignMetrics campaignMetrics,
            UserRiskMetrics userRiskMetrics) {
        upsertDailyStats(clientId, date, CampaignChannel.EMAIL, emailMetrics, campaignMetrics, userRiskMetrics);
    }

    public void upsertDailyStats(
            String clientId,
            LocalDate date,
            CampaignChannel channel,
            EmailMetrics emailMetrics,
            CampaignMetrics campaignMetrics,
            UserRiskMetrics userRiskMetrics) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        DashboardStats stats = dashboardStatsRepository
                .findByClientIdAndPeriodAndDate(clientId, StatsPeriod.DAILY, date)
                .orElse(DashboardStats.builder()
                        .clientId(clientId)
                        .period(StatsPeriod.DAILY)
                        .date(date)
                        .channel(CampaignChannel.EMAIL)
                        .build());

        switch (effectiveChannel) {
            case SMS -> {
                stats.setSmsMetrics(emailMetrics);
                stats.setSmsCampaignMetrics(campaignMetrics);
            }
            case VOICE -> {
                stats.setVoiceMetrics(emailMetrics);
                stats.setVoiceCampaignMetrics(campaignMetrics);
            }
            default -> {
                stats.setChannel(CampaignChannel.EMAIL);
                stats.setEmailMetrics(emailMetrics);
                stats.setCampaignMetrics(campaignMetrics);
            }
        }
        stats.setUserRiskMetrics(userRiskMetrics);
        stats.setUpdatedAt(Instant.now());

        dashboardStatsRepository.save(stats);

        log.info(
                "Dashboard stats upserted: clientId={}, date={}, channel={}, emailsOpened={}, linksClicked={}, dataSubmitted={}, "
                        + "openRate={}, clickRate={}, submissionRate={}",
                clientId,
                date,
                effectiveChannel,
                emailMetrics.getEmailsOpened(),
                emailMetrics.getLinksClicked(),
                emailMetrics.getDataSubmitted(),
                emailMetrics.getOpenRate(),
                emailMetrics.getClickRate(),
                emailMetrics.getSubmissionRate());
    }
}
