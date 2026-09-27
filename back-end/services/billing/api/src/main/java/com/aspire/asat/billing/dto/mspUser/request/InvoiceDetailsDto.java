package com.aspire.asat.billing.dto.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Invoice summary including subtotal, VAT, discount, and total payable amount")
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
            description = "Discount percentage to apply (optional)",
            example = "10.0"
    )
    private Double discountPercentage;

    @Schema(
            description = "Discount amount in currency (can be derived)",
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
}
