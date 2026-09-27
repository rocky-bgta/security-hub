package com.aspire.asat.cms.dto.packageRangePricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRangePricingRequest {

    @NotBlank(message = "User range ID cannot be blank")
    private String userRangeId;

    @NotNull(message = "Price per user cannot be null")
    @Positive(message = "Price per user must be positive")
    private Double pricePerUser;

    private Double yearlyPricePerUser;
}

