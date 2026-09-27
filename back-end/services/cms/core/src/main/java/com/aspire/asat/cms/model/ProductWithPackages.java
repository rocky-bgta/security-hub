package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithPackages {
    private String id;
    private String productName;
    private String productDescription;
    private ProductStatus productStatus;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private List<ProductPackage> packages;
    private Integer displayOrder;
}
