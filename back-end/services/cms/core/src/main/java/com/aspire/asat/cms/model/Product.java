package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;
    private String productName;
    private String productDescription;
    private ProductStatus productStatus;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    @LastModifiedBy
    private String lastModifiedBy;

    private Boolean isTrial = false;
    private Boolean showInSite = false;

    private Integer displayOrder;

}
