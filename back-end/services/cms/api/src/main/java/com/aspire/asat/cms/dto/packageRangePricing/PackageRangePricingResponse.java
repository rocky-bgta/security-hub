package com.aspire.asat.cms.dto.packageRangePricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRangePricingResponse {
    private String id;
    private String packageId;
    private String userRangeId;
    private String rangeName;        // e.g., "10-100"
    private Integer minUsers;
    private Integer maxUsers;
    private Double pricePerUser;
    private Double yearlyPricePerUser;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;
}

