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
@Schema(description = "Paginated response for client admin list")
public class ClientAdminListResponseDto {

    @Schema(description = "List of client admins with their product details")
    private List<ClientAdminWithProductsResponseDto> clientAdmins;

    @Schema(description = "Current page offset", example = "0")
    private Integer offset;

    @Schema(description = "Page size", example = "10")
    private Integer pageSize;

    @Schema(description = "Total number of records", example = "25")
    private Long total;

    @Schema(description = "Total number of pages", example = "3")
    private Integer totalPages;

    @Schema(description = "Whether there are more pages", example = "true")
    private Boolean hasNext;

    @Schema(description = "Whether there are previous pages", example = "false")
    private Boolean hasPrevious;

}
