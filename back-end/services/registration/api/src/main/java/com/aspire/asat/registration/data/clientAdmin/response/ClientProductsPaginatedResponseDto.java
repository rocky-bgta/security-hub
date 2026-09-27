package com.aspire.asat.registration.data.clientAdmin.response;

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
@Schema(description = "Paginated response DTO for client products assigned to a client admin")
public class ClientProductsPaginatedResponseDto {

    @Schema(description = "List of client products with detailed information")
    private List<ClientProductDetailedDto> items;

    @Schema(description = "Current page offset", example = "0")
    private Integer offset;

    @Schema(description = "Number of items per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Total number of items", example = "25")
    private Long total;

    @Schema(description = "Search term used", example = "cybersecurity")
    private String search;

    @Schema(description = "Field used for sorting", example = "assignedAt")
    private String sortBy;

    @Schema(description = "Sort order", example = "desc")
    private String order;

    @Schema(description = "Client admin ID", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Client admin email", example = "admin@aspiredigital.com")
    private String clientAdminEmail;

    @Schema(description = "Organization name", example = "Aspire Digital Ltd.")
    private String organizationName;
}
