package com.aspire.asat.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentPackageItemDTO {

    @Schema(
            description = "Unique ID of the package or item",
            example = "course-101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Package ID is required")
    private String packageId;

    @Schema(
            description = "Price of the individual item/package",
            example = "499.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Package price is required")
    private Double price;
}
