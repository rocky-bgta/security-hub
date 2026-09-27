package com.aspire.asat.registration.data.cms.response;

import com.aspire.asat.registration.data.cms.CmsPackageRangePricingRequestDto;
import com.aspire.asat.registration.data.cms.PackageRangePricingResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Package details from CMS service")
public class CmsPackageDto {

    @Schema(description = "Package ID", example = "be76fce2-d5f2-41b9-bb67-1de31fba3dd3")
    private String id;

    @Schema(description = "Package name", example = "SILVER")
    private String packageName;

    @Schema(description = "Product ID")
    private String productId;

    @Schema(description = "Package price", example = "50")
    private Double price;

    @Schema(description = "Yearly package price", example = "500")
    private Double yearlyPrice;

    @Schema(description = "Package status")
    private String packageStatus;

    @Schema(description = "Base package ID", example = "ca6e350f-0dc1-4673-88ea-c5936f21bd60")
    private String basePackageId;

    @Schema(description = "User range ID for the package (e.g. from buy-now selection)")
    private String userRangeId;

    @Schema(description = "Whether this package is trial")
    private Boolean isTrial;

    @Schema(description = "Whether this package should show in site")
    private Boolean showInSite;

    @Schema(description = "Whether this package uses range pricing")
    private Boolean isPriceRange;

    @Schema(description = "Range pricing request values")
    private List<CmsPackageRangePricingRequestDto> rangePricing;

    @Schema(description = "Range pricing response values")
    private List<PackageRangePricingResponseDto> rangePricingResponse;

    @Schema(description = "List of features included in this package")
    private List<CmsFeatureDto> features;
}
