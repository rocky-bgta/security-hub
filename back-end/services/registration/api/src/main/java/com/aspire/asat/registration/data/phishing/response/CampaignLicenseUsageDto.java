package com.aspire.asat.registration.data.phishing.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unique campaign participants for one ClientProduct assignment from the phishing service.
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
