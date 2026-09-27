package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.DashboardStats;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregations for {@code dashboard_stats} that are not expressible as derived query methods.
 */
public interface DashboardStatsRepositoryCustom {

    /**
     * Sums per-client daily {@code emailMetrics} counters by {@code date} across all tenants with
     * non-blank {@code clientId}, then recomputes rates (same logic as {@link com.aspire.asat.phishing.model.EmailMetrics#calculateRates()}).
     */
    List<DailyEmailMetricsAggregation> aggregateDailyEmailMetricsForAllNonBlankClients(LocalDate startDate);

    /**
     * Same as {@link #aggregateDailyEmailMetricsForAllNonBlankClients(LocalDate)} restricted to one channel.
     * EMAIL includes legacy rows with missing {@code channel}.
     */
    List<DailyEmailMetricsAggregation> aggregateDailyEmailMetricsForAllNonBlankClients(
            LocalDate startDate, CampaignChannel channel);

    /**
     * Daily rows for a client from {@code startDate} onward, scoped to a channel
     * (EMAIL includes missing/null channel on legacy documents).
     */
    List<DashboardStats> findDailyStatsForLastNDays(
            String clientId, LocalDate startDate, CampaignChannel channel);
}
