package com.aspire.asat.registration.data.trial.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trial product selection with existing-trial flag")
public class ProductsDataResponse {

    @Schema(description = "ID of the trial product", example = "product-xyz-001")
    private String productId;

    @Schema(description = "ID of the product package", example = "package-abc-123")
    private String packageId;

    @Schema(description = "True when this email already has a trial for the product and package", example = "false")
    private Boolean existingTrail;
}
