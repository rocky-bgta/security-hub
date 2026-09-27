package com.aspire.asat.registration.data.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsPackageRangePricingRequestDto {
    private String userRangeId;
    private Double pricePerUser;
    private Double yearlyPricePerUser;
}
