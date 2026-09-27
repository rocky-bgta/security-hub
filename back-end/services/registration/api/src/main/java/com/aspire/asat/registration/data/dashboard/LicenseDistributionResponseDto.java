package com.aspire.asat.registration.data.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * License distribution counts aggregated from {@code msp_products}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MSP product license distribution totals")
public class LicenseDistributionResponseDto {

    @Schema(description = "Sum of (licenseCount - usedLicenseCount) where expiryDate > now", example = "120")
    private long totalAvailable;

    @Schema(description = "Sum of licenseCount across all scoped msp_products", example = "500")
    private long totalAllocated;

    @Schema(description = "Sum of usedLicenseCount where expiryDate > now", example = "80")
    private long totalActive;

    @Schema(description = "Sum of (licenseCount - usedLicenseCount) where expiryDate < now", example = "30")
    private long totalExpired;
}
