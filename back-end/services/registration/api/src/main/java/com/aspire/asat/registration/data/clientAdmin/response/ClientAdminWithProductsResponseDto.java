package com.aspire.asat.registration.data.clientAdmin.response;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for client admin with associated product details")
public class ClientAdminWithProductsResponseDto {

    @Schema(description = "Unique identifier of the client admin", example = "admin-uuid-123")
    private String id;

    @Schema(description = "Email address of the client admin", example = "admin@aspiredigital.com")
    private String email;

    @Schema(description = "Organization name", example = "Aspire Digital Ltd.")
    private String organizationName;

    @Schema(description = "Contact email for the organization", example = "info@aspiredigital.com")
    private String contactEmail;

    @Schema(description = "Phone number", example = "+8801700000000")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    @Schema(description = "Billing contact name", example = "John Doe")
    private String billingName;

    @Schema(description = "Billing email", example = "billing@aspiredigital.com")
    private String billingEmail;

    @Schema(description = "Whether billing address is the same as organization address", example = "false")
    private Boolean billingUseSameAsOrganizationAddress;

    @Schema(description = "Billing street address line 1", example = "123 Business Street")
    private String billingStreetAddress;

    @Schema(description = "Billing street address line 2", example = "Suite 100")
    private String billingStreetAddressLine2;

    @Schema(description = "Billing city", example = "San Francisco")
    private String billingCity;

    @Schema(description = "Billing zip or postal code", example = "94105")
    private String billingZipPostalCode;

    @Schema(description = "Billing country", example = "United States")
    private String billingCountry;

    @Schema(description = "Billing state or province", example = "California")
    private String billingStateProvince;

    @Schema(description = "MSP ID", example = "msp-uuid-123")
    private String mspId;

    @Schema(description = "Country", example = "Bangladesh")
    private String country;

    @Schema(description = "Country code", example = "BD")
    private String countryCode;

    @Schema(description = "State or province", example = "Dhaka")
    private String state;

    @Schema(description = "State code", example = "DH")
    private String stateCode;

    @Schema(description = "Time zone", example = "Asia/Dhaka")
    private String timeZone;

    @Schema(description = "Language", example = "English")
    private String language;

    @Schema(description = "Industry", example = "Information Technology")
    private String industry;

    @Schema(description = "Sub-industry id", example = "sub-industry-uuid-123")
    private String subIndustryId;

    @Schema(description = "Compliance id", example = "compliance-uuid-123")
    private String complianceId;

    @Schema(description = "Compliance display name", example = "GDPR")
    private String complianceName;

    @Schema(description = "Domain", example = "aspiredigital.com")
    private String domain;

    @Schema(description = "Organization size", example = "Medium")
    private String organizationSize;

    @Schema(description = "Organization type", example = "Corporation")
    private String organizationType;

    @Schema(description = "Street address line 1", example = "123 Gulshan Avenue")
    private String streetAddress;

    @Schema(description = "Street address line 2", example = "Suite 100")
    private String streetAddressLine2;

    @Schema(description = "City", example = "Dhaka")
    private String city;

    @Schema(description = "Zip or postal code", example = "1212")
    private String zipPostalCode;

    @Schema(description = "Logo URL", example = "https://cdn.aspire.com/logos/aspire.png")
    private String logoUrl;

    @Schema(description = "Admin status", example = "ACTIVE")
    private AdminStatus status;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Client admin ID", example = "client-admin-123")
    private String clientAdminId;

    @Schema(description = "Credit ID", example = "credit-123")
    private String creditId;

    @Schema(description = "Tier ID", example = "tier-123")
    private String tierId;

    @Schema(description = "MSP type", example = "RESELLER")
    private String mspType;

    @Schema(description = "MSP admin email", example = "msp-admin@example.com")
    private String mspAdminEmail;

    @Schema(description = "Net days for payment", example = "30")
    private Integer netDays;

    @Schema(description = "Department", example = "IT")
    private String department;

    @Schema(description = "Role IDs", example = "[\"role-1\", \"role-2\"]")
    private List<String> roleIds;

    @Schema(description = "List of associated client products")
    private List<ClientProductDetailDto> clientProducts;

}
