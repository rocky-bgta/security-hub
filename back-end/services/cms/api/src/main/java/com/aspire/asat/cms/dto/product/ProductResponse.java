package com.aspire.asat.cms.dto.product;

import com.aspire.asat.cms.dto.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String productId;
    private String productName;
    private String productDescription;
    private ProductStatus productStatus;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private List<PackageRequest> packages;
    private Boolean isTrial;
    private Boolean showInSite;
    private Integer displayOrder;
}
