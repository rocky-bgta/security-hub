package com.aspire.asat.registration.data.mspUser.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspViewDetailsResponseDto {
    private String id;
    private String mspId;
    private String phoneNumber;
    private String phoneCode;
    private OrganizationInfoForMspResponseDto organization;
    private String billingEmail;
    private String billingName;
    private String billingStreetAddress;
    private String billingStreetAddressLine2;
    private String billingCity;
    private String billingStateProvince;
    private String billingCountry;
    private String billingZipPostalCode;
    private String billingAddress;
    private CreditInfoResponseDto creditInfo;
    private String notes;
    private String status;

    // New fields
    private LicenseAllocationDto licenseAllocation;

    private Instant createdAt;
    private Instant updatedAt;
}
