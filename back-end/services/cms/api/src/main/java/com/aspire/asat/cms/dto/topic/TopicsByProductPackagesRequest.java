package com.aspire.asat.cms.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to fetch topics by a list of productId/packageId pairs")
public class TopicsByProductPackagesRequest {

    @Valid
    @Schema(description = "List of productId/packageId key-value pairs. "
            + "When null/empty, assigned total is 0 and totalLocked is the full topic count.")
    private List<ProductPackagePairDto> productPackages;

    @Valid
    @Schema(description = "Optional topic filters (category, compliance, metadata, status, selectedTopicId, etc.). "
            + "Pagination and sort are not taken from filter; use offset, pageSize, sortBy, and order on this request.")
    private TopicFilterRequest filter;

    @Schema(description = "Optional search by topic name or description (used when filter.searchText is blank)")
    private String search;

    @Schema(description = "Page offset (default: 0)", example = "0")
    private Integer offset;

    @Schema(description = "Page size (default: 10)", example = "10")
    private Integer pageSize;

    @Schema(description = "Sort field (default: createdAt)", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort order asc/desc (default: desc)", example = "desc")
    private String order;

    @Schema(description = "When true, return topics that do NOT match any of the product/package pairs "
            + "(locked / unassigned). Default false returns matching (assigned) topics.")
    private Boolean excludeMatchingPairs;
}
