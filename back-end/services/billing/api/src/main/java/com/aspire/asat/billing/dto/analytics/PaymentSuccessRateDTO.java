package com.aspire.asat.billing.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for payment success rate metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment success and completion rate metrics")
public class PaymentSuccessRateDTO {

    @Schema(description = "Payment success rate as percentage", example = "92.5")
    private Double paymentSuccessRate;

    @Schema(description = "Total number of payments", example = "400")
    private Long totalPayments;

    @Schema(description = "Number of successful payments", example = "370")
    private Long successfulPayments;

    @Schema(description = "Number of failed payments", example = "30")
    private Long failedPayments;
}

