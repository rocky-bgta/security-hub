package com.aspire.asat.cms.dto.clientAdminProductUsageReport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product-level usage metrics for a client admin product usage report")
public class ClientAdminProductUsageReportProductDto {

    @Schema(description = "Client product ID", example = "client-product-uuid-123")
    private String id;

    @Schema(description = "Product ID", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Package ID", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Product name resolved from sub-package", example = "Cybersecurity Fundamentals")
    private String productName;

    @Schema(description = "Total assigned licenses (used licenses)", example = "25")
    private int totalUsers;

    @Schema(description = "Total license count for this product", example = "50")
    private int totalLicenseCount;

    @Schema(description = "Utilization percentage (totalUsers / totalLicenseCount)", example = "50.0")
    private double utilizationPercentage;
}
