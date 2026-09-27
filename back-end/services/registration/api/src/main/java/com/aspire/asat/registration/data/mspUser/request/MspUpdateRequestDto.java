package com.aspire.asat.registration.data.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspUpdateRequestDto {

    // Organization Information
    private String organizationName;
    private String mspTier;
    private String mspTypeId;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Email(message = "MSP admin email must be valid")
    private String mspAdminEmail;

    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    private String country;
    private String stateProvince;
    private String timeZone;
    private String language;
    private String industry;
    private String domain;

    @Schema(description = "Organization type ID from metadata dropdown", example = "org-type-id-123")
    private String organizationType;

    @Schema(description = "Organization size ID from metadata dropdown", example = "org-size-id-123")
    private String organizationSize;

    // Organization Address
    private String organizationStreetAddress;
    private String organizationStreetAddressLine2;
    private String organizationCity;
    private String organizationStateProvince;
    private String organizationCountry;
    private String organizationZipPostalCode;

    private String logoUrl;

    // Billing Information
    @Email(message = "Billing email must be valid")
    private String billingEmail;
    private String billingName;
    private String billingStreetAddress;
    private String billingStreetAddressLine2;
    private String billingCity;
    private String billingStateProvince;
    private String billingCountry;
    private String billingZipPostalCode;

    // Product and License Updates
    @Valid
    private List<ProductLicenseUpdateDto> productUpdates;

    // Client Assignments
    private List<String> clientIdsToAssign;
    private List<String> clientIdsToRemove;

    // Credit/Payment Terms Updates
    @Valid
    private CreditUpdateDto creditUpdate;

    private String notes;
}
