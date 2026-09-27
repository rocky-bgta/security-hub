package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating client admin information")
public class ClientAdminUpdateRequestDto {

    // Organization Details - Basic Information (from first screenshot)
    @Schema(description = "Organization name", example = "Tech Solutions Inc.")
    private String organizationName;

    @Schema(description = "Industry in which the organization operates", example = "Technology")
    private String industry;

    @Schema(description = "Sub-industry id (optional)", example = "sub-industry-uuid-123")
    private String subIndustryId;

    @Schema(description = "Compliance id (optional)", example = "compliance-uuid-123")
    private String complianceId;

    @Schema(description = "Compliance display name (optional)", example = "GDPR")
    private String complianceName;

    @Schema(description = "Contact email for the organization", example = "contact@techsolutions.com")
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @Schema(description = "Country where the organization is located", example = "United States")
    private String country;

    @Schema(description = "Phone number with country code", example = "+11234567890")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    @Schema(description = "Organization type", example = "Corporation")
    private String organizationType;

    // Organization Details - Extended (from second screenshot)
    @Schema(description = "Street address line 1 of the organization", example = "123 Tech Street")
    private String streetAddress;

    @Schema(description = "Street address line 2 (optional)", example = "Suite 100")
    private String streetAddressLine2;

    @Schema(description = "City where the organization is located", example = "San Francisco")
    private String city;

    @Schema(description = "State, province, or division", example = "California")
    private String stateProvince;

    @Schema(description = "Zip or postal code", example = "94105")
    private String zipPostalCode;

    @Schema(description = "Time zone for the organization's location", example = "America/New_York")
    private String timeZone;

    @Schema(description = "Default language for the organization", example = "English")
    private String language;

    @Schema(description = "Size of the organization", example = "Medium (51-250)")
    private String organizationSize;


    // Billing Details (from the third and fourth screenshots)
    @Schema(description = "Whether billing address is same as organization address", example = "true")
    private Boolean sameAsOrganizationAddress;

    @Schema(description = "Billing contact name", example = "Tech Solutions Inc.")
    private String billingName;

    @Schema(description = "Billing contact email address", example = "billing@techsolutions.com")
    @Email(message = "Billing email must be a valid email address")
    private String billingEmail;

    @Schema(description = "Billing country", example = "United States")
    private String billingCountry;

    @Schema(description = "Billing street address line 1", example = "123 Business Street")
    private String billingStreetAddress;

    @Schema(description = "Billing street address line 2 (optional)", example = "Suite 100")
    private String billingStreetAddressLine2;

    @Schema(description = "Billing city", example = "San Francisco")
    private String billingCity;

    @Schema(description = "Billing zip or postal code", example = "94105")
    private String billingZipPostalCode;

    @Schema(description = "Billing state or province", example = "California")
    private String billingStateProvince;

    @Schema(description = "URL to the uploaded organization logo", example = "https://cdn.aspire.com/logos/techsolutions.png")
    private String logoUrl;

    @Schema(description = "Country code", example = "US")
    private String countryCode;

    @Schema(description = "State code", example = "CA")
    private String stateCode;

}
