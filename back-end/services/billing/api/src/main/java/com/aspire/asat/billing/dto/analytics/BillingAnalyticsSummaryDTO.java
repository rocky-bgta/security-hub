package com.aspire.asat.billing.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for billing analytics summary cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Billing analytics summary metrics")
public class BillingAnalyticsSummaryDTO {

    @Schema(description = "Total revenue from paid invoices", example = "145000.00")
    private Double totalRevenue;

    @Schema(description = "Percentage change from last period", example = "8.5")
    private Double totalRevenueChangePercent;

    @Schema(description = "Total refunds processed", example = "7500.00")
    private Double totalRefunds;

    @Schema(description = "Refund rate as percentage of total revenue", example = "5.2")
    private Double refundRate;

    @Schema(description = "Number of failed payments", example = "30")
    private Long failedPaymentsCount;

    @Schema(description = "Label for failed payments period", example = "This month")
    private String failedPaymentsLabel;

    @Schema(description = "Number of new subscriptions/invoices", example = "37")
    private Long newSubscriptions;

    @Schema(description = "Percentage change in new subscriptions from last period", example = "23.0")
    private Double newSubscriptionsChangePercent;

    @Schema(description = "Total active licenses count", example = "88")
    private Long activeLicenses;

    @Schema(description = "Label for active licenses", example = "Total active subscriptions")
    private String activeLicensesLabel;
}

