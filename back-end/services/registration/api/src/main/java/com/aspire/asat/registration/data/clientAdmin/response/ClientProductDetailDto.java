package com.aspire.asat.registration.data.clientAdmin.response;

import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
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
@Schema(description = "Detailed information about a client product")
public class ClientProductDetailDto {

    @Schema(description = "Unique identifier of the client product", example = "client-product-uuid-123")
    private String id;

    @Schema(description = "Client admin ID", example = "admin-uuid-123")
    private String clientAdminId;

    @Schema(description = "Product ID", example = "product-xyz-001")
    private String productId;

    @Schema(description = "Package ID", example = "package-abc-123")
    private String packageId;

    @Schema(description = "Product details from CMS service")
    private CmsProductResponseDto product;

    @Schema(description = "Package details from CMS service")
    private CmsPackageDto packageDetails;

    @Schema(description = "Number of licenses", example = "10")
    private Integer licenseCount;

    @Schema(description = "Number of used licenses", example = "5")
    private Integer usedLicenseCount;

    @Schema(description = "Price per license", example = "99.99")
    private Double pricePerLicense;

    @Schema(description = "Total price", example = "999.90")
    private Double totalPrice;

    @Schema(description = "Validity period", example = "12")
    private Integer validityPeriod;

    @Schema(description = "Validity unit", example = "MONTH")
    private String validityUnit;

    @Schema(description = "Assignment timestamp", example = "2024-01-15T10:30:00Z")
    private Instant assignedAt;

    @Schema(description = "Expiry date", example = "2025-01-15T10:30:00Z")
    private Instant expiryDate;

    @Schema(description = "License status", example = "ACTIVE")
    private String licenseStatus;

}
