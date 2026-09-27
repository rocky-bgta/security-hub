package com.aspire.asat.cms.dto.clientDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Request DTO for creating or updating client dashboard data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardRequestDto {

    @NotBlank(message = "Client admin ID is required")
    @Schema(description = "Client admin ID", example = "admin-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientAdminId;

    @NotNull(message = "Total product count is required")
    @Min(value = 0, message = "Total product count cannot be negative")
    @Schema(description = "Total number of products", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalProduct;

    @NotNull(message = "Total license count is required")
    @Min(value = 0, message = "Total license count cannot be negative")
    @Schema(description = "Total number of licenses", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalLicense;

    @NotNull(message = "Total topic count is required")
    @Min(value = 0, message = "Total topic count cannot be negative")
    @Schema(description = "Total number of topics", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalTopic;

    @NotNull(message = "Total certificate count is required")
    @Min(value = 0, message = "Total certificate count cannot be negative")
    @Schema(description = "Total number of certificates", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCertificate;

    @Schema(description = "List of client product data to be replicated in CMS service")
    private List<ClientProductData> clientProductData;
}
