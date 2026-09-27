package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDto {
    private String id;
    private String productName;
    private String productDescription;
    private String productStatus;
    private String thumbnailUrl;
    private String createdAt;
    private String updatedAt;
    private String lastModifiedBy;
}

