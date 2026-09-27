package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Organization information response with ID-Name pairs")
public class OrganizationInfoForMspResponseDto {

    @Schema(description = "Legal name of the organization", example = "Aspire Digital Ltd.")
    private String organizationName;

    @Schema(description = "Organization type with metadata ID and display name")
    private IdNameDto organizationType;

    @Schema(description = "Primary contact email for the organization", example = "info@aspiredigital.com")
    private String contactEmail;

    @Schema(description = "Official phone number with country code", example = "+8801700000000")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    @Schema(description = "Country where the organization is located", example = "Bangladesh")
    private String country;

    @Schema(description = "State, province, or division of the organization", example = "Dhaka")
    private String stateProvince;

    @Schema(description = "Time zone for the organization's location", example = "Asia/Dhaka")
    private String timeZone;

    @Schema(description = "Default language for the organization", example = "English")
    private String language;

    @Schema(description = "Industry with metadata ID and display name")
    private IdNameDto industry;

    @Schema(description = "Primary domain or sector of operation", example = "aspiredigital.com")
    private String domain;

    @Schema(description = "Organization size with metadata ID and display name")
    private IdNameDto organizationSize;

    @Schema(description = "Street address line 1 for organization", example = "123 Gulshan Avenue")
    private String streetAddress;

    @Schema(description = "Street address line 2 (optional) - Apt, Suite, Building", example = "Suite 100")
    private String streetAddressLine2;

    @Schema(description = "City for organization address", example = "Dhaka")
    private String city;

    @Schema(description = "Zip or Postal Code for organization address", example = "1212")
    private String zipPostalCode;

    @Schema(description = "URL to the uploaded organization logo (optional)", example = "https://cdn.aspire.com/logos/aspire.png")
    private String logoUrl;

    @Schema(description = "Tier information with ID and name")
    private IdNameDto tier;

    @Schema(description = "MSP Type information with ID and name")
    private IdNameDto mspType;

    @Schema(description = "Net Days information with ID and name")
    private IdNameDto netDays;

    @Schema(description = "Email address for the admin user of the organization", example = "admin@aspiredigital.com")
    private String mspAdminEmail;
}

