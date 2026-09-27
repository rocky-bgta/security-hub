package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.CampaignExpiryValidityUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for campaign expiry duration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignExpireDateRequest {

    @NotNull(message = "Expire validity unit is required")
    private CampaignExpiryValidityUnit validityUnit;

    @NotNull(message = "Expire validity period is required")
    @Min(value = 1, message = "Expire validity period must be greater than 0")
    private Integer validityPeriod;
}
