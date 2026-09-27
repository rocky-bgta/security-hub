package com.aspire.asat.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Schema(description = "Request payload for logging a manual or online payment with multi-method support. Fields like clientId, packageItems, subtotal, vatAmount, discountPercentage, and clientRegion are derived from the invoice.")
public class PaymentRequestDTO {

    @Schema(
            description = "Invoice ID associated with the payment",
            example = "INV-20250408-002",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Invoice ID is required")
    private String invoiceId;

    @Schema(
            description = "Total amount paid across all sources",
            example = "1999.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Total amount is required")
    private Double amount;

    @Schema(
            description = "Payment currency in ISO format (optional - defaults to invoice currency or system default)",
            example = "USD"
    )
    private String currency;

    @Schema(
            description = "Payment date",
            example = "2025-04-08T00:00:00.000Z",
            type = "string",
            format = "date-time",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Payment date is required")
    private Instant date;

    @Schema(
            description = "Optional notes or reference information for the payment",
            example = "Paid partially with credits and Stripe"
    )
    private String notes;

    @Schema(
            description = "Optional coupon ID used for this payment",
            example = "6800ed5fb3e461092ce0d076",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String couponId;

    @Schema(
            description = "Optional coupon code applied to the payment",
            example = "SUMMER25",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String couponCode;

    @Schema(
            description = "List of payment sources detailing methods and amounts (for multi-method partial payments)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Payment sources are required")
    private List<PaymentSourceDTO> paymentSources;

}
