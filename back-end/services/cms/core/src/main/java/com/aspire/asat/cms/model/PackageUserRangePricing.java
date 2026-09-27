package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "package_user_range_pricing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageUserRangePricing {
    @Id
    private String id;

    private String packageId;          // Reference to ProductPackage.id

    private String userRangeId;        // Reference to UserRange.id

    private Double pricePerUser;      // Price per user for this range (monthly)

    private Double yearlyPricePerUser; // Yearly price per user for this range

    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private Boolean isActive = true;
}

