package com.aspire.asat.registration.data.mspUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLicenseDto {
    private String productId;
    private String productName;
    private Integer totalLicenses;
    private Integer usedLicenses;
    private Integer availableLicenses;
}
