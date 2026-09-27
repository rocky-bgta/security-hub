package com.aspire.asat.billing.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete billing analytics response containing all metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete billing analytics response")
public class BillingAnalyticsResponseDTO {

    @Schema(description = "Summary metrics for dashboard cards")
    private BillingAnalyticsSummaryDTO summary;

    @Schema(description = "Revenue trend data grouped by period")
    private List<RevenueByPeriodDTO> revenueTrend;

    @Schema(description = "Top performing packages")
    private List<TopPackageDTO> topPackages;

    @Schema(description = "Failed payments breakdown by reason")
    private List<FailedPaymentAnalysisDTO> failedPaymentAnalysis;

    @Schema(description = "Payment success rate metrics")
    private PaymentSuccessRateDTO paymentSuccessRate;

    @Schema(description = "Analytics period used for the data")
    private AnalyticsPeriod period;

    @Schema(description = "Start date of the analytics period")
    private String startDate;

    @Schema(description = "End date of the analytics period")
    private String endDate;
}

