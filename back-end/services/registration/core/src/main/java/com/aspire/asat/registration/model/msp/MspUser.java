package com.aspire.asat.registration.model.msp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "msp_user")
public class MspUser {

    @Id
    private String id;

    private String mspId;

    private String organizationName;
    private String contactEmail;
    private String mspAdminEmail;
    private String phoneNumber;
    private String phoneCode;
    private String country;
    private String stateProvince;
    private String timeZone;
    private String domain;
    private String mspTypeId;
    private String mspTier;
    private String organizationSize;
    private String organizationType;
    private String organizationStreetAddress;
    private String organizationStreetAddressLine2;
    private String organizationCity;
    private String organizationStateProvince;
    private String organizationCountry;
    private String organizationZipPostalCode;
    private String companyAddress; // Legacy field for backward compatibility
    private String logoUrl;
    private String language;
    private String industry;
    private String subIndustry;
    private String netDaysId;

    // Billing information
    private String billingEmail;
    private String billingName;
    private String billingStreetAddress;
    private String billingStreetAddressLine2;
    private String billingCity;
    private String billingStateProvince;
    private String billingCountry;
    private String billingZipPostalCode;
    private String billingAddress; // Legacy field for backward compatibility

    // Product and package information
    private List<String> productIds;
    private List<String> packageIds;
    private List<String> clientProductIds;


    // Credit information
    private String creditId;
    private Boolean creditEnabled;
    private String creditReason;
    private BigDecimal creditAmount;
    private Date creditStartDate;
    private Date creditEndDate;
    private Boolean autoSuspendOverDue;

    // Invoice information
    private String invoiceId;
    private String invoiceStatus;

    // Additional fields
    private String department;
    private List<String> roleIds;
    private String notes;


    // Status and metadata
    private String status; // PENDING, ACTIVE, SUSPENDED, INACTIVE
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
