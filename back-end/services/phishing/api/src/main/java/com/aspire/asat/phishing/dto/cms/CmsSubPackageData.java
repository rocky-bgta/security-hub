package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phishing-local mirror of CMS GET /sub-packages/{{id}} {@code data} payload.
 * CMS may expose {@code productId} / {@code packageId} at root (preferred) or only via
 * nested {@code productDetails} / {@code packageDetails}; use {@link #effectiveProductId()}
 * and {@link #effectivePackageId()}. {@code productPackageId} is only on the SubPackage entity root.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsSubPackageData {

    private String id;
    private String name;
    private String productId;
    private String packageId;
    private String productPackageId;
    private String clientId;
    private String clientAdminId;

    private ProductDetailsRef productDetails;
    private PackageDetailsRef packageDetails;

    public String effectiveProductId() {
        if (productId != null && !productId.isBlank()) {
            return productId;
        }
        if (productDetails != null && productDetails.getId() != null && !productDetails.getId().isBlank()) {
            return productDetails.getId();
        }
        return null;
    }

    public String effectivePackageId() {
        if (packageId != null && !packageId.isBlank()) {
            return packageId;
        }
        if (packageDetails != null && packageDetails.getId() != null && !packageDetails.getId().isBlank()) {
            return packageDetails.getId();
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductDetailsRef {
        private String id;
        private String productName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageDetailsRef {
        private String id;
        private String name;
        private String productId;
    }
}
