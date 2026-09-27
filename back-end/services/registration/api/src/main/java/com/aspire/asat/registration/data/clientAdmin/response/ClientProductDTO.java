package com.aspire.asat.registration.data.clientAdmin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductDTO {

    private String id;
    private String clientAdminId;

    private String productId;
    private String packageId;

    private int licenseCount;
    private int usedLicenseCount;

    private double pricePerLicense;
    private double totalPrice;

    private int validityPeriod;
    private String validityUnit;

    private Instant assignedAt;
    private Instant expiryDate;
}
