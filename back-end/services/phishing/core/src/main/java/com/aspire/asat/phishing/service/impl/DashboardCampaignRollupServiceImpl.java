package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardCampaignRollupServiceImpl implements DashboardCampaignRollupService {

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignEmailCounters rollupEmailCounters(String clientId, CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        List<Campaign> campaigns = campaignRepository.findWithFilters(
                clientId,
                null,
                null,
                effectiveChannel,
                PageRequest.of(0, MAX_CAMPAIGNS_PER_ROLLUP)).getContent();

        int totalRecipients = 0;
        int totalSent = 0;
        int delivered = 0;
        int bounced = 0;
        int opened = 0;
        int clicked = 0;
        int attachments = 0;
        int submitted = 0;
        int reported = 0;

        for (Campaign campaign : campaigns) {
            CampaignStats stats = campaign.getStats();
            if (stats == null) {
                continue;
            }
            totalRecipients += stats.getTotalRecipients();
            switch (DashboardChannelScope.effective(campaign.getChannel())) {
                case SMS -> {
                    totalSent += stats.getSmsSent();
                    delivered += stats.getSmsDelivered();
                    bounced += stats.getSmsFailed();
                    clicked += stats.getLinksClicked();
                    submitted += stats.getDataSubmitted();
                }
                case VOICE -> {
                    totalSent += stats.getCallsTotal();
                    delivered += stats.getCallsAnswered();
                    opened += stats.getCallsAnswered();
                    bounced += stats.getCallsFailed();
                    clicked += stats.getCallsEngaged();
                    submitted += stats.getCallsCompromised();
                    reported += stats.getCallsReported();
                }
                default -> {
                    totalSent += stats.getEmailsSent();
                    delivered += stats.getEmailsDelivered();
                    bounced += stats.getEmailsBounced();
                    opened += stats.getEmailsOpened();
                    clicked += stats.getLinksClicked();
                    attachments += stats.getAttachmentsOpened();
                    submitted += stats.getDataSubmitted();
                    reported += stats.getEmailsReported();
                }
            }
        }

        return new CampaignEmailCounters(
                totalRecipients,
                totalSent,
                delivered,
                bounced,
                opened,
                clicked,
                attachments,
                submitted,
                reported);
    }

    @Override
    public EmailMetrics toEmailMetrics(CampaignEmailCounters counters) {
        EmailMetrics metrics = EmailMetrics.builder()
                .totalRecipients(counters.totalRecipients())
                .totalEmailsSent(counters.totalEmailsSent())
                .emailsDelivered(counters.emailsDelivered())
                .emailsBounced(counters.emailsBounced())
                .emailsOpened(counters.emailsOpened())
                .linksClicked(counters.linksClicked())
                .attachmentsOpened(counters.attachmentsOpened())
                .dataSubmitted(counters.dataSubmitted())
                .emailsReported(counters.emailsReported())
                .build();
        metrics.calculateRates();
        return metrics;
    }

    @Override
    public CampaignMetrics buildCampaignMetrics(String clientId, CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        int total = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, null);
        int active = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.RUNNING);
        int completed = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.COMPLETED);
        int draft = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.DRAFT);
        int scheduled = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.SCHEDULED);
        int cancelled = (int) campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.CANCELLED);

        return CampaignMetrics.builder()
                .totalCampaigns(total)
                .activeCampaigns(active)
                .completedCampaigns(completed)
                .draftCampaigns(draft)
                .scheduledCampaigns(scheduled)
                .cancelledCampaigns(cancelled)
                .build();
    }
}
