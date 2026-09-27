package com.aspire.asat.billing.dto.analytics;

import com.aspire.asat.billing.dto.invoice.PaymentFailureReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for failed payment analysis breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Failed payment breakdown by reason")
public class FailedPaymentAnalysisDTO {

    @Schema(description = "Failure reason enum value", example = "INSUFFICIENT_FUNDS")
    private PaymentFailureReason reason;

    @Schema(description = "Human-readable failure reason", example = "Insufficient Funds")
    private String reasonLabel;

    @Schema(description = "Number of failures for this reason", example = "12")
    private Long count;

    @Schema(description = "Percentage of total failures", example = "40.0")
    private Double percentage;
}

