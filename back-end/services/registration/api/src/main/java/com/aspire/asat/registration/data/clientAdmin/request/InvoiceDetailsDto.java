package com.aspire.asat.registration.data.clientAdmin.request;

import com.aspire.asat.registration.data.enums.DiscountType;
import com.aspire.asat.registration.data.enums.PaymentStatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Invoice summary including subtotal, VAT, discount, payment status, and total payable amount")
public class InvoiceDetailsDto {

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Subtotal must be greater than zero")
    @Schema(
            description = "Base amount before applying any discounts or VAT",
            example = "1000.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double subtotal;

    @Schema(
            description = "Type of discount applied (FLAT for flat amount, PERCENTAGE for percentage-based)",
            example = "PERCENTAGE"
    )
    private DiscountType discountType;

    @Schema(
            description = "Discount percentage to apply (used when discountType is PERCENTAGE)",
            example = "10.0"
    )
    private Double discountPercentage;

    @Schema(
            description = "Discount amount in currency (used when discountType is FLAT, or derived from percentage)",
            example = "100.0"
    )
    private Double discountAmount;

    @Schema(
            description = "Coupon code applied, if any",
            example = "SUMMER10"
    )
    private String couponCode;

    @NotNull(message = "VAT rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "VAT rate cannot be negative")
    @Schema(
            description = "VAT rate in percentage (e.g., 5 for 5%)",
            example = "5.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double vatRate;

    @Schema(
            description = "Amount of VAT in currency (can be derived)",
            example = "45.0"
    )
    private Double vatAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than zero")
    @Schema(
            description = "Total payable amount after applying discount and VAT",
            example = "945.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double totalAmount;

    @NotNull(message = "Payment status is required")
    @Schema(
            description = "Payment status (PENDING for pending payment list, COMPLETED for completed payment)",
            example = "PENDING",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PaymentStatusType paymentStatus;

    @Valid
    @Schema(
            description = "Completed payment details (required when paymentStatus is COMPLETED)"
    )
    private CompletedPaymentDto completedPayment;
}
