package com.aspire.asat.cms.dto.clientAdminProductUsageReport;

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
@Schema(description = "Client admin product usage report summary response")
public class ClientAdminProductUsageReportResponseDto {

    @Schema(description = "Client admin ID", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Total number of unique active products", example = "5")
    private int totalProduct;

    @Schema(description = "Total number of unique active topics (modules)", example = "25")
    private int totalActiveModule;

    @Schema(description = "Total license count across all active products", example = "500")
    private int totalInteraction;

    @Schema(description = "Average engagement percentage (used licenses / total licenses)", example = "62.5")
    private double averageEngagement;

    @Schema(description = "Product-level usage breakdown for the client admin")
    private List<ClientAdminProductUsageReportProductDto> products;
}
