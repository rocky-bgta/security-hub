package com.aspire.asat.billing.dto.mspUser.request;

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
@Schema(description = "Individual package selection info under a product")
public class PackageInfoForMspDto {

    @NotBlank(message = "Package ID is required")
    @Schema(description = "Unique ID of the selected package", example = "package-abc-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageId;

    @NotBlank(message = "Package name is required")
    @Schema(description = "Readable name of the selected package", example = "Cyber Pro Monthly Package", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageName;

    @NotNull(message = "License count is required")
    @Min(value = 1, message = "At least one license must be selected")
    @Schema(description = "Number of licenses selected for this package", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer licenseCount;

    @NotNull(message = "Price per license is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Schema(description = "Price per license for the selected package", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double pricePerLicense;

    @NotNull(message = "Validity period is required")
    @Min(value = 1, message = "Validity period must be at least 1 unit")
    @Schema(description = "Validity duration of the license", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer validityPeriod;

    @NotNull(message = "Validity unit is required")
    @Schema(description = "Unit of validity (MONTH or YEAR)", example = "MONTH", allowableValues = {"MONTH", "YEAR"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private ValidityUnitForMspDto validityUnitForMspDto;
}
