package com.aspire.asat.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Details of each payment source (Credit, Stripe, PayPal, etc.) used in a payment")
public class PaymentSourceDTO {

    @Schema(
            description = "Payment method used for this part of the payment",
            example = "CREDIT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String method;         // "CREDIT", "STRIPE", "PAYPAL", "BANK_TRANSFER", etc.

    @Schema(
            description = "Amount paid through this payment method",
            example = "600.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double amount;

    @Schema(
            description = "Transaction ID if applicable (for Stripe/PayPal etc.), null for manual or credit",
            example = "txn_1GqIC8JInqZqH9Xqv1EB94Or",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String transactionId;   // optional

    @Schema(
            description = "True if this method is online (e.g., Stripe, PayPal), False for Credit or Bank Transfer",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean online;

    @Schema(
            description = "Optional metadata for payment details (e.g., bank transfer details, check payment details)",
            example = "{\"bankName\": \"Chase Bank\", \"accountNumber\": \"123456789\"}",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Object metadata;  // Can store BankTransferDetailsDto or CheckPaymentDetailsDto
}
