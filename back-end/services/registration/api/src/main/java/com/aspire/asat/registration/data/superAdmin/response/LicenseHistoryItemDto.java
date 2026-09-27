package com.aspire.asat.registration.data.superAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "License history item for super admin")
public class LicenseHistoryItemDto {

    @Schema(description = "Product ID", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Product name from CMS service", example = "Cybersecurity Essentials")
    private String productName;

    @Schema(description = "Package ID", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Package name from CMS service", example = "Cyber Pro Monthly")
    private String packageName;

    @Schema(description = "Number of licenses", example = "10")
    private Integer licenseCount;

    @Schema(description = "Number of used licenses", example = "5")
    private Integer usedLicenseCount;

    @Schema(description = "Assignment timestamp", example = "2024-01-15T10:30:00Z")
    private Instant assignedAt;

    @Schema(description = "Expiry date", example = "2025-01-15T10:30:00Z")
    private Instant expiryDate;

    @Schema(description = "License status", example = "ACTIVE")
    private String licenseStatus;

    @Schema(description = "Client admin ID", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Client name (organization name)", example = "Acme Corporation")
    private String clientName;

    @Schema(description = "MSP name (organization name)", example = "Tech Solutions MSP")
    private String mspName;

    @Schema(description = "Client admin email", example = "admin@acme.com")
    private String email;
}

