package com.aspire.asat.billing.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for revenue data grouped by period (month/quarter/year).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Revenue trend data for a specific period")
public class RevenueByPeriodDTO {

    @Schema(description = "Period label", example = "Jan 2024")
    private String period;

    @Schema(description = "Total revenue for the period", example = "45000.00")
    private Double revenue;

    @Schema(description = "Number of new subscriptions in the period", example = "12")
    private Long newSubscriptions;

    @Schema(description = "Total refunds for the period", example = "2500.00")
    private Double refunds;

    @Schema(description = "Net revenue (revenue - refunds)", example = "42500.00")
    private Double netRevenue;
}

