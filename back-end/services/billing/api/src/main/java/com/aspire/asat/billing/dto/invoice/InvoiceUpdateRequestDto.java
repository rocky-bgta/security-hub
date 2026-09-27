package com.aspire.asat.billing.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating invoice payment status and completed payment details.
 * This is a focused DTO containing only payment-related fields for updating invoices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating invoice payment status and payment details")
public class InvoiceUpdateRequestDto {

    @Schema(
            description = "Payment status (PENDING or COMPLETED). If provided, updates invoice status accordingly.",
            example = "COMPLETED",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private PaymentStatusType paymentStatus;

    @Valid
    @Schema(
            description = "Completed payment details including payment method and associated information. " +
                    "Optional - can be provided to update payment details without changing status.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private CompletedPaymentDto completedPayment;

    @Schema(
            description = "Optional URL for bank receipt document",
            example = "https://s3.amazonaws.com/receipts/bank-receipt-123.pdf",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String bankReceiptUrl;
}

