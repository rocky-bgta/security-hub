package com.aspire.asat.registration.data.trial.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign additional trial products to an existing trial account")
public class AssignTrialProductsRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email of the existing trial account", example = "trail-asat-v5@yopmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotEmpty(message = "At least one product is required")
    @Valid
    @Schema(description = "Products to assign to the existing trial account", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ProductsData> productsData;
}
