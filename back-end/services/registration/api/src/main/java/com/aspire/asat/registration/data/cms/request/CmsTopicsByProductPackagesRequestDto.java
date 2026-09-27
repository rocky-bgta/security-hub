package com.aspire.asat.registration.data.cms.request;

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
@Schema(description = "CMS request to fetch topics by productId/packageId pairs")
public class CmsTopicsByProductPackagesRequestDto {

    private List<CmsProductPackagePairDto> productPackages;
    private CmsTopicFilterRequestDto filter;
    private String search;
    private Integer offset;
    private Integer pageSize;
    private String sortBy;
    private String order;

    @Schema(description = "When true, return topics that do NOT match any of the product/package pairs")
    private Boolean excludeMatchingPairs;
}
