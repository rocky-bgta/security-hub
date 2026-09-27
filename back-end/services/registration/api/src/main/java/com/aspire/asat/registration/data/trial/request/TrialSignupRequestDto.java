package com.aspire.asat.registration.data.trial.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for trial signup - Step 1: Account Details")
public class TrialSignupRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address for the trial account", example = "mdtest001@yopmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number with country code", example = "+880 1784-669597", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phoneCode;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name of the user", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name of the user", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "Company name is required")
    @Schema(description = "Company or organization name", example = "MSA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String companyName;

    @NotBlank(message = "Number of employees is required")
    @Schema(description = "Number of employees in the organization", example = "1-10 employees", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numberOfEmployees;

    @NotBlank(message = "How did you hear about us is required")
    @Schema(description = "Source where user heard about the service", example = "LinkedIn", requiredMode = Schema.RequiredMode.REQUIRED)
    private String howDidYouHearAboutUs;

    @Valid
    @Schema(description = "Selected trial products and packages. Optional for the original single-product signup flow.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ProductsData> productsData;
}

