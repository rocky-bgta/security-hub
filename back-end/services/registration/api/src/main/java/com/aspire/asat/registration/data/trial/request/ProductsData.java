package com.aspire.asat.registration.data.trial.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trial product and package selection")
public class ProductsData {

    @NotBlank(message = "Product ID is required")
    @Schema(description = "ID of the trial product to assign", example = "product-xyz-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotBlank(message = "Package ID is required")
    @Schema(description = "ID of the product package to assign", example = "package-abc-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageId;

    @Schema(description = "Optional product name for display", example = "Security Awareness Training")
    private String productName;

    @Schema(description = "Optional package name for display", example = "Free Trial")
    private String packageName;
}
