package com.aspire.asat.registration.data.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product restrictions in coupon.
 * When packageId is null or blank, the coupon applies to ALL packages of the product.
 * When packageId is specified, the coupon only applies to that specific package.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRestrictionDTO {
    private String productId;
    private String productName;
    private String packageId;    // Optional: null means all packages of this product
    private String packageName;  // Optional: for display purposes
}

