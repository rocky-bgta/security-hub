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
public class ProductWithSinglePackageResponse {
    private String productId;
    private String productName;
    private String productDescription;
    private ProductStatus productStatus;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private PackageRequest packages; // Single package instead of List
    private Integer topicCount; // Number of topics for this product and package
    private Integer displayOrder;
}
