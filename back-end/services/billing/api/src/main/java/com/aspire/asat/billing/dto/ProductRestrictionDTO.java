package com.aspire.asat.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRestrictionDTO {
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    private String productName;         // Optional: Product name for display
    
    private String packageId;           // Optional: Package ID (null = all packages)
    
    private String packageName;         // Optional: Package name for display
}
