package com.aspire.asat.registration.data.trial.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for password creation - Step 3: Create Password")
public class PasswordCreationRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address of the user", example = "mdtest001@yopmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 64, message = "Password must not exceed 64 characters")
    @Schema(description = "Password for the account (8-64 characters)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(max = 64, message = "Password must not exceed 64 characters")
    @Schema(description = "Confirm password must match password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @Valid
    @Schema(description = "Trial products and packages to assign. Optional when productId and subPackageId are provided.")
    private List<ProductsData> productsData;

    @Schema(description = "ID of the trial product to assign (legacy single-product flow)", example = "product-xyz-001")
    private String productId;

    @Schema(description = "ID of the package within the product (legacy single-product flow)", example = "subpackage-abc-123")
    private String subPackageId;

    private String productName;
    private String subPackageName;
}

