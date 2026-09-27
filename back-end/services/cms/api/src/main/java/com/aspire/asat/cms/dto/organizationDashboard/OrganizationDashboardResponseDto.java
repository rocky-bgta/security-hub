package com.aspire.asat.cms.dto.organizationDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "Response DTO for organization dashboard data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDashboardResponseDto {

    @Schema(description = "Dashboard ID", example = "dashboard-123")
    private String id;

    @Schema(description = "Organization admin ID", example = "org-admin-123")
    private String organizationAdminId;

    @Schema(description = "Total number of products", example = "10")
    private Integer totalProduct;

    @Schema(description = "Total number of packages", example = "25")
    private Integer totalPackage;

    @Schema(description = "Total number of licenses", example = "500")
    private Integer totalLicense;

    @Schema(description = "Total number of clients", example = "50")
    private Integer totalClient;

    @Schema(description = "Total number of MSPs", example = "5")
    private Integer totalMsp;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;

    @Schema(description = "User who created the dashboard", example = "admin-user-123")
    private String createdBy;

    @Schema(description = "User who last updated the dashboard", example = "admin-user-123")
    private String updatedBy;
}

