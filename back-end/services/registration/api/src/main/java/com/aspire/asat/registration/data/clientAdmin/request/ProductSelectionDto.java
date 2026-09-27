package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product and package selection details for onboarding")
public class ProductSelectionDto {

    @NotBlank(message = "Product ID is required")
    @Schema(
            description = "Unique ID of the selected product",
            example = "product-xyz-001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String productId;

    @NotBlank(message = "Package ID is required")
    @Schema(
            description = "Unique ID of the selected package under the product",
            example = "package-abc-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String packageId;


    @Schema(
            description = "Unique ID of the selected range under the package",
            example = "range-abc-123"
    )
    private String userRangeId;

    @NotNull(message = "License count is required")
    @Min(value = 1, message = "At least one license must be selected")
    @Schema(
            description = "Number of licenses selected for the package",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer licenseCount;

    @NotNull(message = "Price per license is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Schema(
            description = "Price for a single license of the package",
            example = "99.99",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double pricePerLicense;

    @NotNull(message = "Validity period is required")
    @Min(value = 1, message = "Validity period must be at least 1 unit")
    @Schema(
            description = "Duration of the license validity",
            example = "12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer validityPeriod;

    @NotNull(message = "Validity unit is required")
    @Schema(
            description = "Unit of time for validity duration",
            example = "MONTH",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"MONTH", "YEAR"}
    )
    private ValidityUnit validityUnit;

    @NotBlank(message = "Product name is required")
    @Schema(
            description = "Readable name of the selected product",
            example = "Cybersecurity Essentials",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String productName;

    @NotBlank(message = "Package name is required")
    @Schema(
            description = "Readable name of the selected package",
            example = "Cyber Pro Monthly Package",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String packageName;
}
