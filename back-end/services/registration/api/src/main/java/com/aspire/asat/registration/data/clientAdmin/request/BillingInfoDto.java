package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
public class BillingInfoDto {

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
            description = "Whether billing address is the same as organization address",
            example = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean useSameAsOrganizationAddress;

    @Schema(
            description = "Billing street address line 1 (required if useSameAsOrganizationAddress is false)",
            example = "123 Business Street",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String streetAddress;

    @Schema(
            description = "Billing street address line 2 (optional)",
            example = "Suite 100",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String streetAddressLine2;

    @Schema(
            description = "Billing city (required if useSameAsOrganizationAddress is false)",
            example = "San Francisco",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String city;

    @Schema(
            description = "Billing zip or postal code (required if useSameAsOrganizationAddress is false)",
            example = "94105",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String zipPostalCode;

    @Schema(
            description = "Billing country (required if useSameAsOrganizationAddress is false)",
            example = "United States",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String country;

    @Schema(
            description = "Billing state or province (optional)",
            example = "California",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String stateProvince;

    @AssertTrue(message = "Billing address fields are required when useSameAsOrganizationAddress is false")
    private boolean isValidBillingAddress() {
        if (useSameAsOrganizationAddress == null || useSameAsOrganizationAddress) {
            return true; // If using same address, billing fields are not required
        }
        // If not using same address, required fields must be present
        return streetAddress != null && !streetAddress.isBlank() &&
               city != null && !city.isBlank() &&
               zipPostalCode != null && !zipPostalCode.isBlank() &&
               country != null && !country.isBlank();
    }

}
