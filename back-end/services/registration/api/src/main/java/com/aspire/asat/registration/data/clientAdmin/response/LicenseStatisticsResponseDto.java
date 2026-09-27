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
@Schema(description = "License statistics response for a client")
public class LicenseStatisticsResponseDto {

    @Schema(description = "Total number of licenses assigned to the client", example = "500")
    private int licenseCount;

    @Schema(description = "Total number of licenses currently in use. For Phishing Simulation, "
            + "Smishing Simulation, and Vishing Simulation this is the count of unique campaign "
            + "participants per product+package; for all other products it is the assigned seat count.",
            example = "250")
    private int usedLicenseCount;

    @Schema(description = "Number of available licenses (licenseCount - usedLicenseCount)", example = "250")
    private int availableLicenseCount;

    @Schema(description = "License utilization percentage", example = "50.0")
    private double utilizationPercentage;
}
