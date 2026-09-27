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
@Schema(description = "Paginated response DTO for license history")
public class LicenseHistoryPaginatedResponseDto {

    @Schema(description = "List of license history items")
    private List<LicenseHistoryItemDto> items;

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

    @Schema(description = "Client admin ID filter applied", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Product ID filter applied", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Package ID filter applied", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Country filter applied", example = "United States")
    private String country;

    @Schema(description = "MSP ID filter applied", example = "msp-uuid-123")
    private String mspId;

    @Schema(description = "Search by organization name applied", example = "Acme Corp")
    private String search;
}

