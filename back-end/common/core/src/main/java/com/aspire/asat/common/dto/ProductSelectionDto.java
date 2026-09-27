package com.aspire.asat.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product and package selection details for onboarding")
public class ProductSelectionDto {

    private String productId;
    private String packageId;
    private Integer licenseCount;
    private Double pricePerLicense;
    private Integer validityPeriod;
    private ValidityUnit validityUnit;
    private String productName;
    private String packageName;
}
