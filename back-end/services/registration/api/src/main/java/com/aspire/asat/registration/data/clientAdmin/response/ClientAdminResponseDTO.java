package com.aspire.asat.registration.data.clientAdmin.response;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
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
public class ClientAdminResponseDTO {

    private String id;
    private String email;
    private String organizationName;
    private String contactEmail;
    private String phoneNumber;
    private String billingName;
    private String billingEmail;
    private Boolean billingUseSameAsOrganizationAddress;
    private String billingStreetAddress;
    private String billingStreetAddressLine2;
    private String billingCity;
    private String billingZipPostalCode;
    private String billingCountry;
    private String billingStateProvince;
    private String mspId;
    private String country;
    private String countryCode;
    private String state;
    private String stateCode;
    private String timeZone;
    private String language;
    private String industry;
    private String subIndustryId;
    private String complianceId;
    private String complianceName;
    private String domain;
    private String organizationSize;
    private String streetAddress;
    private String streetAddressLine2;
    private String city;
    private String zipPostalCode;
    private String logoUrl;
    private List<ProductInfo> clientProducts;
    private AdminStatus status;
    private Instant createdAt;
    private String clientAdminId;
    private String creditId;
    private String tierId;
    private String mspType;
    private String mspAdminEmail;
    private Integer netDays;
    private String department;
    private List<String> roleIds;
}
