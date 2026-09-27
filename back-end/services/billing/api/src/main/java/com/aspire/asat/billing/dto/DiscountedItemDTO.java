package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountedItemDTO {
    private String packageId;
    private Double originalPrice;
    private Double discountedPrice;
    private boolean couponApplied;
}
