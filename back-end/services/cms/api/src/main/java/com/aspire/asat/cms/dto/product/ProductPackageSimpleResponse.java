package com.aspire.asat.cms.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal product-package response used for batch lookups by package ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple product package with id and name")
public class ProductPackageSimpleResponse {

    @Schema(description = "Package ID", example = "package-uuid-123")
    private String id;

    @Schema(description = "Package name", example = "Premium Package")
    private String name;
}
