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
@Schema(description = "Information related to the client's organization")
public class OrganizationInfoForMspDto {

    @NotBlank(message = "Organization name is required")
    @Schema(
            description = "Legal name of the organization",
            example = "Aspire Digital Ltd.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be a valid email address")
    @Schema(
            description = "Primary contact email for the organization",
            example = "info@aspiredigital.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String contactEmail;

    @NotBlank(message = "Phone number is required")
    @Schema(
            description = "Official phone number with country code",
            example = "+8801700000000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @Schema(
            description = "International dialing code",
            example = "+880",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneCode;

    @NotBlank(message = "Country is required")
    @Schema(
            description = "Country where the organization is located",
            example = "Bangladesh",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String country;

    @NotBlank(message = "State or province is required")
    @Schema(
            description = "State, province, or division of the organization",
            example = "Dhaka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String stateProvince;

    @NotBlank(message = "Time zone is required")
    @Schema(
            description = "Time zone for the organization's location",
            example = "Asia/Dhaka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String timeZone;

    @NotBlank(message = "Language is required")
    @Schema(
            description = "Default language for the organization",
            example = "English",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String language;

    @NotBlank(message = "Industry is required")
    @Schema(
            description = "Industry id, code, or name. Created automatically if it does not exist.",
            example = "Information Technology",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String industry;

    @Schema(
            description = "Sub-industry id, code, or name. Created automatically if it does not exist.",
            example = "Software Development",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String subIndustry;

    @NotBlank(message = "Domain is required")
    @Schema(
            description = "Primary domain or sector of operation",
            example = "aspiredigital.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String domain;

    @NotBlank(message = "Organization size is required")
    @Schema(
            description = "Size of the organization (Small, Medium, Large)",
            example = "Medium",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationSize;

    @Schema(
            description = "Organization type ID from metadata dropdown",
            example = "org-type-id-123"
    )
    private String organizationType;

    @NotBlank(message = "Street address is required")
    @Schema(
            description = "Street address line 1 for organization",
            example = "123 Gulshan Avenue",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String streetAddress;

    @Schema(
            description = "Street address line 2 (optional) - Apt, Suite, Building",
            example = "Suite 100"
    )
    private String streetAddressLine2;

    @NotBlank(message = "City is required")
    @Schema(
            description = "City for organization address",
            example = "Dhaka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String city;

    @Schema(
            description = "Zip or Postal Code for organization address",
            example = "1212"
    )
    private String zipPostalCode;

    @Schema(
            description = "URL to the uploaded organization logo (optional)",
            example = "https://cdn.aspire.com/logos/aspire.png"
    )
    private String logoUrl;

    @Schema(description = "Tier id for MSP")
    private String tierId;

    @Schema(description = "MSP Type ID", example = "msp-type-id-123")
    private String mspTypeId;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    @Schema(
            description = "Email address for the admin user of the organization",
            example = "admin@aspiredigital.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mspAdminEmail;

}
