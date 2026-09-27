package com.aspire.asat.billing.dto.mspUser.request;

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

    @NotBlank(message = "Organization type is required")
    @Schema(
            description = "Type of organization (e.g., Private, NGO, Government)",
            example = "Private",
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

    @NotBlank(message = "Industry is required")
    @Schema(
            description = "Industry in which the organization operates",
            example = "Information Technology",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String industry;

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


    @NotBlank(message = "Address is required")
    @Schema(
            description = "Physical address of the organization",
            example = "123 Gulshan Avenue, Dhaka 1212",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String address;

    @Schema(
            description = "URL to the uploaded organization logo (optional)",
            example = "https://cdn.aspire.com/logos/aspire.png"
    )
    private String logoUrl;

    @Schema(description = "Net payment days (for invoice settlement terms)", example = "30")
    private Integer netDays;

    @Schema(description = "Tier id for MSP")
    private String tierId;

    @Schema(description = "Type of MSP (e.g., MSP, MSP-Partner, MSP-Partner-Partner)")
    private String mspType;

    @Schema(description = "MSP admin email")
    private String mspAdminEmail;

}
