package com.aspire.asat.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionVatConfig {
    private String id;   // e.g., "CA" or UUID
    private String regionName;   // e.g., "California"
    private double vatRate;      // e.g., 7.25
}
