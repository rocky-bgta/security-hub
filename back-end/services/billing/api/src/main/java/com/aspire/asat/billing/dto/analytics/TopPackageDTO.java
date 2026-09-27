package com.aspire.asat.billing.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for top performing packages analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Top performing package data")
public class TopPackageDTO {

    @Schema(description = "Package name", example = "PLATINUM")
    private String packageName;

    @Schema(description = "Number of subscriptions for this package", example = "45")
    private Long subscriptionCount;

    @Schema(description = "Total revenue from this package", example = "135000.00")
    private Double revenue;

    @Schema(description = "Percentage of total revenue", example = "65.0")
    private Double revenuePercentage;
}

