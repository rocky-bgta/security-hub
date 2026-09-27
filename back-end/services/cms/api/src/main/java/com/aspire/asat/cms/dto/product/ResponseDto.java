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

public class ResponseDto {

    private String id;
    private String productName;
    private String productDescription;
    private ProductStatus productStatus;
    private List<String> courseIds;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private Boolean isTrial;
    private Boolean showInSite;
    private Integer displayOrder;

}
