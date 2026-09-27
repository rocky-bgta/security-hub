package com.aspire.asat.registration.data.buynow.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for buy now password creation - Step 3: Create Password")
public class BuyNowPasswordCreationRequestDto {

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

    @Schema(description = "Selected product ID to assign after signup", example = "b365fbd8-80d6-4239-b261-a6146d2a9402")
    private String productId;

    @Schema(description = "Product name for display", example = "ASAT-SECURITY-V2")
    private String productName;

    @Schema(description = "Selected package ID to assign after signup", example = "be76fce2-d5f2-41b9-bb67-1de31fba3dd3")
    private String packageId;

    @Schema(description = "Package name for display", example = "SILVER")
    private String packageName;

    @Schema(description = "User range ID for the selected package", example = "range-abc-123")
    private String userRangeId;
}

