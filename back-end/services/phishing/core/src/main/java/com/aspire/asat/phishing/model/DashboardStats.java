package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * MongoDB entity for aggregated dashboard statistics.
 * Pre-computed statistics for fast dashboard loading.
 */
@Document(collection = "dashboard_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "client_period_date_idx", def = "{'clientId': 1, 'period': 1, 'date': -1}", unique = true)
})
public class DashboardStats {

    @Id
    private String id;

    private String clientId;

    @Builder.Default
    private StatsPeriod period = StatsPeriod.DAILY;

    private LocalDate date;

    /**
     * Legacy discriminator. Live uniqueness remains {@code clientId + period + date};
     * SMS/VOICE series are stored as {@link #smsMetrics} / {@link #voiceMetrics} on this same document
     * so existing unique indexes in Mongo are not broken.
     */
    @Builder.Default
    private CampaignChannel channel = CampaignChannel.EMAIL;

    @Builder.Default
    private CampaignMetrics campaignMetrics = new CampaignMetrics();

    @Builder.Default
    private EmailMetrics emailMetrics = new EmailMetrics();

    private CampaignMetrics smsCampaignMetrics;

    private EmailMetrics smsMetrics;

    private CampaignMetrics voiceCampaignMetrics;

    private EmailMetrics voiceMetrics;

    @Builder.Default
    private UserRiskMetrics userRiskMetrics = new UserRiskMetrics();

    @Builder.Default
    private BreachMetrics breachMetrics = new BreachMetrics();

    private Instant updatedAt;
}
