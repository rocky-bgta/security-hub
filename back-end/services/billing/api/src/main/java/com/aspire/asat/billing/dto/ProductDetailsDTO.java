package com.aspire.asat.billing.dto;

import lombok.Data;

@Data
public class ProductDetailsDTO {
    private String productName;  // Name of the product
    private String packageName;  // Associated package
    private Integer quantity;    // Quantity purchased
    private Double rate;         // Price per unit
}
