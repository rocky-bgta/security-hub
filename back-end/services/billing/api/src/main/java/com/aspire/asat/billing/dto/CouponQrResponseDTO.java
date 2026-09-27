package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponQrResponseDTO {
    private String code;       // The coupon code (e.g., SPRING2025)
    private String urlCoupon;  // URL to apply the coupon
    private String qrCodeUrl;  // Publicly accessible QR image link
}
