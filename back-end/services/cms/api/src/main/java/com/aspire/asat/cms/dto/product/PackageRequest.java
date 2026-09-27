package com.aspire.asat.cms.dto.product;

import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    private String id;

    @NotBlank(message = "Package name cannot be blank")
    private String packageName;

    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    private List<FeatureDto> features;

    // Price is optional - can be null if rangePricing is provided
    // Validation is handled in service layer to ensure either price or rangePricing is provided
    private Double price;

    private Double yearlyPrice;

    @NotNull(message = "Bundle status cannot be null")
    private PackageStatus packageStatus;

    private String basePackageId;

    private Boolean isTrial = false;
    private Boolean showInSite = false;

    private Boolean isPriceRange = false;  // Indicates whether package uses range-based pricing

    // Range-based pricing (optional)
    private List<PackageRangePricingRequest> rangePricing;  // For request

    // Range pricing response (for API responses)
    private List<PackageRangePricingResponse> rangePricingResponse;  // For response

}
