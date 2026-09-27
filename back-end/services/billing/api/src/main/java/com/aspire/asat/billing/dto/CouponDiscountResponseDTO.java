package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDiscountResponseDTO {
    private Double actualAmount;     // Total price before discount
    private Double discountAmount;   // Total discount
    private Double subtotal;         // actualAmount - discountAmount
    private Double vatAmount;        // Calculated on actualAmount
    private Double grandTotal;       // subtotal + vatAmount
    private List<DiscountedItemDTO> breakdown;
}
