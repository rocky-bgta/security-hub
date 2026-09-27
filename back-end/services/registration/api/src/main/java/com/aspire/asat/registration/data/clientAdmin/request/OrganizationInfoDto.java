package com.aspire.asat.registration.data.clientAdmin.request;

import com.aspire.asat.registration.data.validation.ValidPhoneNumber;
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
@ValidPhoneNumber(countryNameField = "countryName")
@Schema(description = "Information related to the client's organization")
public class OrganizationInfoDto {

    @NotBlank(message = "Organization name is required")
    @Schema(
            description = "Legal name of the organization",
            example = "Aspire Digital Ltd.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationName;

    @NotBlank(message = "Organization type is required")
    @Schema(
            description = "UUID of the organization type",
            example = "org-type-uuid-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationType;

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
            description = "International dialing code for the organization phone number",
            example = "+880",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneCode;

    @NotBlank(message = "Country is required")
    @Schema(
            description = "CountryId where the organization is located",
            example = "country-uuid-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String country;

    @Schema(
            description = "Country name (for display in validation messages; send the selected country's name when using a dropdown)",
            example = "Bangladesh",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String countryName;

    @Schema(
            description = "State, province, or division of the organization",
            example = "Dhaka",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String stateProvince;

    @Schema(
            description = "Time zone for the organization's location",
            example = "Asia/Dhaka",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String timeZone;

    @Schema(
            description = "Default language for the organization",
            example = "English",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String language;

    @Schema(
            description = "Industry in which the organization operates",
            example = "Information Technology",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String industry;

    @Schema(
            description = "Sub-industry id (optional). References /api/v1/dropdown/sub-industries.",
            example = "sub-industry-uuid-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String subIndustryId;

    @Schema(
            description = "Compliance id (optional). Caller-supplied; denormalized label stored alongside.",
            example = "compliance-uuid-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String complianceId;

    @Schema(
            description = "Compliance display name (optional)",
            example = "GDPR",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String complianceName;

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

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    @Schema(
            description = "Email address for the admin user of the organization",
            example = "admin@aspiredigital.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String adminEmail;

    @NotBlank(message = "Street address is required")
    @Schema(
            description = "Street address line 1 of the organization",
            example = "123 Gulshan Avenue",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String streetAddress;

    @Schema(
            description = "Street address line 2 of the organization (optional)",
            example = "Suite 100",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String streetAddressLine2;

    @NotBlank(message = "City is required")
    @Schema(
            description = "City where the organization is located",
            example = "Dhaka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String city;

    @NotBlank(message = "Zip/Postal code is required")
    @Schema(
            description = "Zip or postal code of the organization",
            example = "1212",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String zipPostalCode;

    @Schema(
            description = "URL to the uploaded organization logo (optional)",
            example = "https://cdn.aspire.com/logos/aspire.png"
    )
    private String logoUrl;
}
