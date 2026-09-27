package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.registration.data.cms.CmsPackageRangePricingRequestDto;
import com.aspire.asat.registration.data.cms.PackageRangePricingResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFeatureDto;
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
@Schema(description = "Assigned MSP package response with CMS package details and msp_products assignment fields")
public class MspAssignedPackageResponseDto {

    private String id;
    private String packageName;
    private String productId;
    private List<CmsFeatureDto> features;
    private Double price;
    private Double yearlyPrice;
    private String packageStatus;
    private String basePackageId;
    private Boolean isTrial;
    private Boolean showInSite;
    private Boolean isPriceRange;
    private List<CmsPackageRangePricingRequestDto> rangePricing;
    private List<PackageRangePricingResponseDto> rangePricingResponse;

    @Schema(description = "msp_products document id")
    private String mspProductId;
    private Integer licenseCount;
    private Integer usedLicenseCount;
    private Double pricePerLicense;
    private Double totalPrice;
    private Integer validityPeriod;
    private String validityUnit;
    private Instant assignedAt;
    private Instant expiryDate;
    private String licenseStatus;
    private String countryId;
}
