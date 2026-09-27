package com.aspire.asat.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRestriction {
    private String productId;           // Product ID (UUID)
    private String productName;         // Product name (for display purposes)
    private String packageId;           // Package ID (UUID) - Optional, null means all packages
    private String packageName;         // Package name (for display purposes)
}
