package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDashboardResponseDTO {
    private Long totalCoupons;
    private Long activeCoupons;
    private Long inactiveCoupons;
    private Long totalUsage;
    private Double estimatedSavings;
}

