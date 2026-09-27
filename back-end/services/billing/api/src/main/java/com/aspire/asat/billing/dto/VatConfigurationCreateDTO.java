package com.aspire.asat.billing.dto;

import com.aspire.asat.billing.RegionVatConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new VAT configuration")
public class VatConfigurationCreateDTO {

    @Schema(example = "ad30c701-9827-4fb7-b404-fac01fed5e77", description = "Country ID (UUID format)")
    @NotBlank
    private String id;

    @Schema(example = "Bangladesh")
    @NotBlank
    private String countryName;

    @Schema(description = "Default VAT rate if region-based is disabled", example = "15.0")
    @NotNull
    private Double defaultVatRate;

    @Schema(description = "Flag indicating if VAT is region-based", example = "false")
    private boolean regionBased;

    @Schema(description = "List of region-based VAT configurations")
    private List<RegionVatConfig> regions;
}
