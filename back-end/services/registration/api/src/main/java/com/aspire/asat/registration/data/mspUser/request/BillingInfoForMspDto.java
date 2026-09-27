package com.aspire.asat.registration.data.mspUser.request;

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
@Schema(description = "DTO containing billing contact details")
public class BillingInfoForMspDto {

    @NotBlank(message = "Billing email is required")
    @Email(message = "Billing email must be a valid email address")
    @Schema(
            description = "Billing contact email address",
            example = "billing@company.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String billingEmail;

    @NotBlank(message = "Billing name is required")
    @Schema(
            description = "Full name of the billing contact person or entity",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String billingName;

    @Schema(
            description = "Street address line 1 for billing",
            example = "123 Main St"
    )
    private String streetAddress;

    @Schema(
            description = "Street address line 2 (optional) - Apt, Suite, Building",
            example = "Suite 100"
    )
    private String streetAddressLine2;

    @Schema(
            description = "City for billing address",
            example = "New York"
    )
    private String city;

    @Schema(
            description = "State or Province for billing address",
            example = "New York"
    )
    private String stateProvince;

    @Schema(
            description = "Country for billing address",
            example = "United States"
    )
    private String country;

    @Schema(
            description = "Zip or Postal Code for billing address",
            example = "10001"
    )
    private String zipPostalCode;

}
