package com.aspire.asat.registration.data.mspUser.request;

import com.aspire.asat.registration.data.cms.request.CmsTopicFilterRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to fetch CMS topics for MSP client products with optional filters")
public class MspClientProductTopicsRequestDto {

    @Schema(description = "Optional CMS topic filters (no pagination/sort — use offset, pageSize, sortBy, order)")
    private CmsTopicFilterRequestDto filter;

    @Schema(description = "Optional search (used when filter.searchText is blank)")
    private String search;

    @Schema(description = "When true (default), return topics assigned to the MSP. "
            + "When false, return topics not assigned to the MSP (locked).")
    private Boolean isAvailable;

    @Schema(description = "Page offset (0-based, default: 0)", example = "0")
    private Integer offset;

    @Schema(description = "Page size (default: 10)", example = "10")
    private Integer pageSize;

    @Schema(description = "Sort field (default: createdAt)", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort order (default: desc)", example = "desc")
    private String order;
}
