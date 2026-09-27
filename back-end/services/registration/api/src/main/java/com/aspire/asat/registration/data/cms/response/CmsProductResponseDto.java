package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CMS service response for product with packages")
public class CmsProductResponseDto {

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

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "List of tags associated with the product", example = "Security, Phishing")
    private List<String> tags;
}
