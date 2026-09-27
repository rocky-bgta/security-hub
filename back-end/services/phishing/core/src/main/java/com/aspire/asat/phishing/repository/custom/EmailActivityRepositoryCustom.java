package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Custom repository for EmailActivity bulk recipient lookups.
 */
public interface EmailActivityRepositoryCustom {

    /**
     * Find activities by campaign IDs and recipient IDs sorted by newest first.
     */
    List<EmailActivity> findByCampaignIdsAndRecipientIds(
            List<String> campaignIds, List<String> recipientIds);

    /**
     * Paginated activity log with optional type and time range (legacy rules preserved).
     * When {@code campaignIds} is empty, returns an empty page. When null, does not filter by campaign.
     */
    Page<EmailActivity> findEmailActivityLog(
            String clientId,
            ActivityType activityType,
            Instant startTime,
            Instant endTime,
            String search,
            Collection<String> campaignIds,
            Pageable pageable);

    long countEmailActivityLog(
            String clientId,
            ActivityType activityType,
            Instant startTime,
            Instant endTime,
            String search,
            Collection<String> campaignIds);

    /**
     * Aggregates per-recipient activity counters within {@code [startInclusive, endExclusive)}.
     * Restricted to {@code campaignIds}. Empty campaign IDs yield an empty list.
     */
    List<RecipientWindowActivityMetrics> aggregateRecipientMetricsForWindow(
            String clientId,
            Instant startInclusive,
            Instant endExclusive,
            Collection<String> campaignIds,
            DashboardChannelScope.ActivityMapping activityMapping);

    /**
     * Aggregates client-level activity totals within {@code [startInclusive, endExclusive)}.
     * Restricted to {@code campaignIds}. Empty campaign IDs yield zeros.
     */
    ClientWindowActivityTotals aggregateClientActivityTotalsForWindow(
            String clientId,
            Instant startInclusive,
            Instant endExclusive,
            Collection<String> campaignIds,
            DashboardChannelScope.ActivityMapping activityMapping);
}
