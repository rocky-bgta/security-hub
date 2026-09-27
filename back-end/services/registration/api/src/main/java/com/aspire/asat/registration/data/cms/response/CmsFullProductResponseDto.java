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
@Schema(description = "Full CMS service response including packages")
public class CmsFullProductResponseDto {

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

    @Schema(description = "List of product tags")
    private List<String> tags;

    @Schema(description = "Created at timestamp")
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    private Instant updatedAt;

    @Schema(description = "Last modified by")
    private String lastModifiedBy;

    @Schema(description = "List of packages for this product")
    private List<CmsPackageDto> packages;

    @Schema(description = "Whether this product is a trial product")
    private Boolean isTrial;

    @Schema(description = "Whether this product should show in site")
    private Boolean showInSite;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;
}
