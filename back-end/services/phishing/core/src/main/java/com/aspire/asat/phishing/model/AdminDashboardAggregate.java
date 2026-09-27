package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.StatsPeriod;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Separate aggregate storage for admin risk dashboard UI.
 */
@Document(collection = "phish_prone_admin_dashboard_aggregates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "admin_client_period_date_idx", def = "{'clientId': 1, 'period': 1, 'date': -1}", unique = true)
})
public class AdminDashboardAggregate {

    @Id
    private String id;

    private String clientId;

    @Builder.Default
    private StatsPeriod period = StatsPeriod.DAILY;

    private LocalDate date;

    @Builder.Default
    private CohortMetrics cohortMetrics = new CohortMetrics();

    @Builder.Default
    private UserRiskMetrics userRiskMetrics = new UserRiskMetrics();

    @Builder.Default
    private List<TopRiskUserSnapshot> topRiskUsers = new ArrayList<>();

    private Instant updatedAt;
}
