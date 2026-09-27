package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignStats;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Maps embedded {@link CampaignStats} onto the dashboard/report performance DTO per channel.
 */
public final class CampaignPerformanceSupport {

    private CampaignPerformanceSupport() {
    }

    public static CampaignPerformanceDto toPerformanceDto(Campaign campaign) {
        CampaignStats stats = campaign.getStats() != null ? campaign.getStats() : new CampaignStats();
        CampaignChannel channel = DashboardChannelScope.effective(campaign.getChannel());

        int totalRecipients = stats.getTotalRecipients();
        int sent;
        int delivered;
        int opened;
        int reported;
        int bounced;
        int clicked;
        int submitted;

        switch (channel) {
            case SMS -> {
                sent = stats.getSmsSent();
                delivered = stats.getSmsDelivered();
                opened = 0;
                reported = 0;
                bounced = stats.getSmsFailed();
                clicked = stats.getLinksClicked();
                submitted = stats.getDataSubmitted();
            }
            case VOICE -> {
                sent = stats.getCallsTotal();
                delivered = stats.getCallsAnswered();
                opened = stats.getCallsAnswered();
                reported = stats.getCallsReported();
                bounced = stats.getCallsFailed();
                clicked = stats.getCallsEngaged();
                submitted = stats.getCallsCompromised();
            }
            default -> {
                sent = stats.getEmailsSent();
                delivered = stats.getEmailsDelivered();
                opened = stats.getEmailsOpened();
                reported = stats.getEmailsReported();
                bounced = stats.getEmailsBounced();
                clicked = stats.getLinksClicked();
                submitted = stats.getDataSubmitted();
            }
        }

        double deliveryRate = sent > 0 ? (double) delivered / sent * 100 : 0;
        double openRate;
        double clickRate = totalRecipients > 0 ? (double) clicked / totalRecipients * 100 : 0;
        double compromiseRate;
        double reportRate;
        switch (channel) {
            case SMS -> {
                openRate = 0;
                reportRate = 0;
                compromiseRate = clicked > 0 && totalRecipients > 0
                        ? (double) submitted / totalRecipients * 100 : 0;
            }
            case VOICE -> {
                openRate = sent > 0 ? (double) opened / sent * 100 : 0;
                reportRate = sent > 0 ? (double) reported / sent * 100 : 0;
                compromiseRate = totalRecipients > 0 ? (double) submitted / totalRecipients * 100 : 0;
            }
            default -> {
                openRate = totalRecipients > 0 ? (double) opened / totalRecipients * 100 : 0;
                reportRate = totalRecipients > 0 ? (double) reported / totalRecipients * 100 : 0;
                compromiseRate = clicked > 0 && totalRecipients > 0
                        ? (double) submitted / totalRecipients * 100 : 0;
            }
        }

        long durationDays = 0;
        if (campaign.getLaunchedAt() != null) {
            Instant endTime = campaign.getCompletedAt() != null ? campaign.getCompletedAt() : Instant.now();
            durationDays = ChronoUnit.DAYS.between(campaign.getLaunchedAt(), endTime);
        }

        return CampaignPerformanceDto.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getCampaignName())
                .status(campaign.getStatus())
                .channel(channel)
                .totalRecipients(totalRecipients)
                .emailsSent(sent)
                .emailsDelivered(delivered)
                .opened(opened)
                .clicked(clicked)
                .dataSubmitted(submitted)
                .reported(reported)
                .bounced(bounced)
                .deliveryRate(Math.round(deliveryRate * 10) / 10.0)
                .openRate(Math.round(openRate * 10) / 10.0)
                .clickRate(Math.round(clickRate * 10) / 10.0)
                .compromiseRate(Math.round(compromiseRate * 10) / 10.0)
                .reportRate(Math.round(reportRate * 10) / 10.0)
                .startDate(campaign.getLaunchedAt())
                .endDate(campaign.getCompletedAt())
                .durationDays(durationDays)
                .build();
    }
}
