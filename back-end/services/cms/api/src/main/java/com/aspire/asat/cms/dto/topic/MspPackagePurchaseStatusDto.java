package com.aspire.asat.cms.dto.topic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package purchase status for an MSP relative to a product's full package catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspPackagePurchaseStatusDto {
    private String packageName;

    @JsonProperty("isPurchase")
    private Boolean isPurchase;
}
