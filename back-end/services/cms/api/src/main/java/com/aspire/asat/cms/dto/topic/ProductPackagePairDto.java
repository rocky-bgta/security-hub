package com.aspire.asat.cms.dto.topic;

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
@Schema(description = "Product and package ID pair")
public class ProductPackagePairDto {

    @NotBlank
    @Schema(description = "Product ID", example = "product-123")
    private String productId;

    @NotBlank
    @Schema(description = "Package ID", example = "package-456")
    private String packageId;
}
