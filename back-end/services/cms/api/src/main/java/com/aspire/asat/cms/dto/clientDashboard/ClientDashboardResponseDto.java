package com.aspire.asat.cms.dto.clientDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "Response DTO for client dashboard data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardResponseDto {

    @Schema(description = "Dashboard ID", example = "dashboard-123")
    private String id;

    @Schema(description = "Client admin ID", example = "admin-123")
    private String clientAdminId;

    @Schema(description = "Total number of products", example = "5")
    private Integer totalProduct;

    @Schema(description = "Total number of licenses", example = "100")
    private Integer totalLicense;

    @Schema(description = "Total number of topics", example = "25")
    private Integer totalTopic;

    @Schema(description = "Total number of certificates", example = "15")
    private Integer totalCertificate;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}
