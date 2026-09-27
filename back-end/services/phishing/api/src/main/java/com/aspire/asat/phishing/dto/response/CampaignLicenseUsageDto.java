package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unique campaign-participant license usage for one ClientProduct assignment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignLicenseUsageDto {

    /** ClientProduct.id for the licensed product-package assignment. */
    private String productPackageId;
    private int uniqueUserCount;
}
