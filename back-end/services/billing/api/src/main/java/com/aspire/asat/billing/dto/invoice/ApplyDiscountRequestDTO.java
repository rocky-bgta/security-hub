package com.aspire.asat.billing.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to apply an invoice-level discount before payment (admin only)")
public class ApplyDiscountRequestDTO {

    @NotNull
    @Schema(description = "Type of discount: FLAT or PERCENTAGE", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "Discount percentage when discountType is PERCENTAGE", example = "10.0")
    private Double discountPercentage;

    @Schema(description = "Flat discount amount when discountType is FLAT", example = "100.0")
    private Double discountAmount;

    @Schema(description = "Whether to send the updated invoice PDF by email", example = "true")
    private boolean sendEmail;

    @Schema(description = "Optional reason appended to invoice status note for audit", example = "Finance approval")
    private String reason;
}
