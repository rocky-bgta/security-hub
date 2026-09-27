package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MSP dashboard summary totals")
public class MspDashboardResponseDto {

    @Schema(description = "Count of unique products assigned to the MSP", example = "5")
    private long totalProducts;

    @Schema(description = "Sum of licenseCount across all MSP product-package assignments", example = "500")
    private long totalLicenses;

    @Schema(description = "Count of unique packages assigned to the MSP", example = "12")
    private long totalPackages;

    @Schema(description = "Count of clients (client admins) under the MSP", example = "25")
    private long totalClients;
}
