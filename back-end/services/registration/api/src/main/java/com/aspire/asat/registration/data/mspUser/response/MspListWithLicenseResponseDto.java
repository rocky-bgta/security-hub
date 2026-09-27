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
@Schema(description = "MSP list item response with aggregated license counts")
public class MspListWithLicenseResponseDto {

    @Schema(description = "MSP ID", example = "msp-id-123")
    private String id;

    @Schema(description = "Organization name", example = "Aspire Digital Ltd.")
    private String organizationName;

    @Schema(description = "MSP admin email", example = "admin@aspiredigital.com")
    private String mspAdminEmail;

    @Schema(description = "MSP tier", example = "tier-premium-001")
    private String mspTier;

    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Total license count aggregated from all products", example = "150")
    private Integer licenseCount;

    @Schema(description = "Total used license count aggregated from all products", example = "75")
    private Integer usedLicenseCount;

    @Schema(description = "Country", example = "Bangladesh")
    private String country;
}

