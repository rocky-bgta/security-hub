package com.aspire.asat.registration.data.buynow.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for buy now signup - Step 1: Account Details")
public class BuyNowSignupRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address for the buy now account", example = "mdtest001@yopmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number with country code", example = "+880 1784-669597", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phoneCode;

    @NotBlank(message = "Company name is required")
    @Schema(description = "Company or organization name", example = "MSA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String companyName;

    @NotBlank(message = "Number of employees is required")
    @Schema(description = "Number of employees in the organization", example = "1-10 employees", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numberOfEmployees;

    @Schema(description = "Source where user heard about the service", example = "LinkedIn", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String howDidYouHearAboutUs;
}

