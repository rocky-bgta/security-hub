package com.aspire.asat.billing.dto;

import com.aspire.asat.billing.RegionVatConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response structure for VAT configuration")
public class VatConfigurationResponseDTO {
    @Schema(description = "Country ID (UUID format)")
    private String id;
    private String countryName;
    private Double defaultVatRate;
    private boolean regionBased;
    private List<RegionVatConfig> regions;
    private Instant createdAt;
    private Instant updatedAt;
}

