package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.data.enums.OnboardBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "client_admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAdmin {

    private String id; // UUID as String

    private String email;
    private String hashedPassword;

    private String contactEmail;
    private String phoneNumber;
    private String phoneCode;
    private String billingName;
    private String billingEmail;
    private String mspId;
    private String mspName;

    private String country;
    private String countryCode;   // e.g., "us" - kept for backward compatibility

    private String state;
    private String stateCode;     // e.g., "CA"

    private String timeZone;
    private String language;
    private String industry;
    private String subIndustryId;
    private String complianceId;
    private String complianceName;
    private String domain;
    private String organizationSize;
    private String organizationType;
    
    // Organization address fields
    private String streetAddress;
    private String streetAddressLine2;
    private String city;
    private String zipPostalCode;

    // branding fields
    private String organizationName;
    private String logoUrl;



    private List<String> clientProductIds;

    private AdminStatus status; // PENDING, ACTIVE, etc.
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    private String clientAdminId;

    private OnboardBy onboardBy; // TRIAL, BUY_NOW, MSP, or ASPIRE_ADMIN

    private String creditId;
    private String tierId;
    private String mspType;        // e.g., RESELLER, DISTRIBUTOR, etc.
    private String mspAdminEmail;
    private Integer netDays;       // e.g., 30, 60 (for due period)
    private String department;
    private List<String> roleIds;
    
    // Billing address fields
    private boolean billingUseSameAsOrganizationAddress;
    private String billingStreetAddress;
    private String billingStreetAddressLine2;
    private String billingCity;
    private String billingZipPostalCode;
    private String billingCountry;
    private String billingStateProvince;

    // Buy-now flow: selected product/package for tracking (no ClientProduct created)
    private String selectedProductId;
    private String selectedProductName;
    private String selectedPackageId;
    private String selectedPackageName;
    private String selectedUserRangeId;

}
