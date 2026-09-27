package com.aspire.asat.cms.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationClientProductDto {

    private String id;
    private String clientAdminId;
    private String productId;
    private String packageId;
    private int licenseCount;
    private int usedLicenseCount;
    private double totalPrice;
    private Instant assignedAt;
    private Instant expiryDate;

    private double pricePerLicense;

    private int validityPeriod;
    private String validityUnit;
}
