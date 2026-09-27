package com.aspire.asat.phishing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Asset inventory counts for Aspire Admin / MSP dashboard cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard asset inventory counts")
public class AssetInventoryCountsDto {

    @Schema(description = "Total campaigns (campaign presets)", example = "190")
    private long campaignPresets;

    @Schema(description = "Total email templates", example = "150")
    private long emailTemplates;

    @Schema(description = "Total sending profiles", example = "100")
    private long sendingProfiles;

    @Schema(description = "Total landing pages", example = "53")
    private long landingPages;
}
