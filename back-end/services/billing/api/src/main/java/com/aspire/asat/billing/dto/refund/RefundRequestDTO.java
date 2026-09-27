package com.aspire.asat.billing.dto.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a refund")
public class RefundRequestDTO {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "ID of the original payment", example = "payment_123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentId;

    @NotBlank(message = "Invoice ID is required")
    @Schema(description = "ID of the associated invoice", example = "invoice_456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String invoiceId;

    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    @Schema(description = "Amount to refund", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double refundAmount;

    @NotBlank(message = "Reason is required")
    @Schema(description = "Reason for the refund", example = "Customer requested refund due to service issues", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "Currency of the refund", example = "USD")
    private String currency;

    @Schema(description = "Additional notes", example = "Processed as per support ticket #12345")
    private String notes;
}

