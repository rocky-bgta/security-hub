package com.aspire.asat.registration.data.superAdmin.response;

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
@Schema(description = "Paginated response DTO for MSP license history")
public class MspLicenseHistoryPaginatedResponseDto {

    @Schema(description = "List of MSP license history items")
    private List<MspLicenseHistoryItemDto> items;

    @Schema(description = "Current page offset", example = "0")
    private Integer offset;

    @Schema(description = "Number of items per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Total number of items", example = "25")
    private Long total;

    @Schema(description = "Field used for sorting", example = "assignedAt")
    private String sortBy;

    @Schema(description = "Sort order", example = "desc")
    private String order;

    @Schema(description = "MSP ID filter applied", example = "msp-uuid-123")
    private String mspId;

    @Schema(description = "Product ID filter applied", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Package ID filter applied", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Country filter applied", example = "United States")
    private String country;

    @Schema(description = "Search filter applied", example = "Tech Solutions")
    private String search;
}

