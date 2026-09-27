package com.aspire.asat.registration.data.mspUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseAllocationDto {
    private Integer totalLicenses;
    private Integer usedLicenses;
    private Integer availableLicenses;
    private double usagePercentage;
}
