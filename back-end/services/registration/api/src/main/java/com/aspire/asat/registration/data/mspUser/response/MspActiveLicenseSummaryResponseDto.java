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
@Schema(description = "MSP active license summary response")
public class MspActiveLicenseSummaryResponseDto {

    @Schema(description = "Total active license count across all MSPs", example = "5000")
    private Integer licenseCount;

    @Schema(description = "Total active used license count across all MSPs", example = "2500")
    private Integer usedLicenseCount;
}

