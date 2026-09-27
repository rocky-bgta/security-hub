package com.aspire.asat.billing.dto.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product selection details including multiple packages")
public class ProductSelectionForMspDto {

    @NotBlank(message = "Product ID is required")
    @Schema(description = "Unique ID of the selected product", example = "product-xyz-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotBlank(message = "Product name is required")
    @Schema(description = "Readable name of the selected product", example = "Cybersecurity Essentials", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotNull(message = "Package information list is required")
    @Schema(description = "List of package-specific selections for this product", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull PackageInfoForMspDto> packages;
}
