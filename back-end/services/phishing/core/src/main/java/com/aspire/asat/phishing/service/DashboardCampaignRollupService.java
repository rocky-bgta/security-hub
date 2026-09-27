package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.EmailMetrics;

/**
 * Rolls up campaign-level statistics for dashboard persistence and API responses.
 * <p>
 * Counter sums match {@code DashboardServiceImpl#getEmailStats()}. Rate formulas differ by consumer:
 * {@link EmailMetrics#calculateRates()} for {@code dashboard_stats} trends vs DTO mapping in dashboard service.
 */
public interface DashboardCampaignRollupService {

    /** Maximum campaigns loaded per rollup (same cap as legacy dashboard email stats). */
    int MAX_CAMPAIGNS_PER_ROLLUP = 1000;

    default CampaignEmailCounters rollupEmailCounters(String clientId) {
        return rollupEmailCounters(clientId, CampaignChannel.EMAIL);
    }

    CampaignEmailCounters rollupEmailCounters(String clientId, CampaignChannel channel);

    EmailMetrics toEmailMetrics(CampaignEmailCounters counters);

    default CampaignMetrics buildCampaignMetrics(String clientId) {
        return buildCampaignMetrics(clientId, CampaignChannel.EMAIL);
    }

    CampaignMetrics buildCampaignMetrics(String clientId, CampaignChannel channel);
}
