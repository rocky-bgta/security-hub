package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Product with single package and topic count from CMS service")
public class CmsProductWithSinglePackageResponseDto {

    @Schema(description = "Product ID", example = "b365fbd8-80d6-4239-b261-a6146d2a9402")
    private String productId;

    @Schema(description = "Product name", example = "ASAT-SECURITY-V2")
    private String productName;

    @Schema(description = "Product description", example = "Test AA description")
    private String productDescription;

    @Schema(description = "Product status", example = "ENABLED")
    private String productStatus;

    @Schema(description = "Thumbnail URL", example = "https://content.aspireelearning.com/string")
    private String thumbnailUrl;

    @Schema(description = "Created at timestamp")
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    private Instant updatedAt;

    @Schema(description = "Last modified by")
    private String lastModifiedBy;

    @Schema(description = "Package details")
    private CmsPackageDto packages;

    @Schema(description = "Number of topics for this product-package combination", example = "2")
    private Integer topicCount;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    private List<String> tags;

}

