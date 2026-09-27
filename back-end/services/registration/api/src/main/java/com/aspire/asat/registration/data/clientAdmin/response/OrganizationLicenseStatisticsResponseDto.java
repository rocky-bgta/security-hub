package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Organization dashboard license statistics response for a client admin")
public class OrganizationLicenseStatisticsResponseDto {

    @Schema(description = "Total available licenses (total licenseCount - allocated - expired)", example = "150")
    private int totalAvailableLicenses;

    @Schema(description = "Total allocated licenses (sum of usedLicenseCount across all products)", example = "250")
    private int totalAllocatedLicenses;

    @Schema(description = "Total active licenses (allocated - expired)", example = "200")
    private int totalActiveLicenses;

    @Schema(description = "Total expired licenses (sum of unused licenses from expired product-packages)", example = "50")
    private int totalExpiredLicenses;
}

