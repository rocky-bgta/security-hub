package com.aspire.asat.registration.data.trial.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful trial signup - Step 4: Trial Summary")
public class TrialSignupResponseDto {

    @Schema(description = "Trial package name", example = "Security Awareness Training")
    private String trialPackage;

    @Schema(description = "Trial period in days", example = "30 Days")
    private String trialPeriod;

    @Schema(description = "Number of licenses included", example = "Up to 5 users")
    private String licenses;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address of the user", example = "mdtest001@yopmail.com")
    private String email;

    @Schema(description = "Company name", example = "MSA")
    private String companyName;

    @Schema(description = "User ID for login", example = "uuid-here")
    private String userId;

    @Schema(description = "Assigned or evaluated trial products")
    private List<ProductsDataResponse> productsData;
}

