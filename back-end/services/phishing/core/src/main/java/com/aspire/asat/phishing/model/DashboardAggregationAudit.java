package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Immutable audit entry per dashboard aggregation run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "phish_prone_dashboard_aggregation_audit")
public class DashboardAggregationAudit {

    @Id
    private String id;

    @Indexed
    private String clientId;

    private Instant rollingWindowStart;

    private Instant rollingWindowEnd;

    private double currentAvgHrs;

    private double previousAvgHrs;

    private double currentReportRatePercent;

    private double previousReportRatePercent;

    @Indexed
    private Instant runAt;
}
