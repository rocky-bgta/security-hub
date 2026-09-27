package com.aspire.asat.registration.data.mspUser.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLicenseUpdateDto {
    private String mspProductId;
    private String productId;
    private String packageId;
    private Integer licenseCount;
    private Double pricePerLicense;
    private Integer validityPeriod;
    private ValidityUnitForMspDto validityUnit;
    private boolean remove;
}
