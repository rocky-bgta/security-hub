package com.aspire.asat.cms.dto.packageRangePricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPackageRangePricingRequest {

    @NotBlank(message = "Package ID cannot be blank")
    private String packageId;

    @NotEmpty(message = "Range pricing list cannot be empty")
    @Valid
    private List<PackageRangePricingRequest> rangePricing;
}

