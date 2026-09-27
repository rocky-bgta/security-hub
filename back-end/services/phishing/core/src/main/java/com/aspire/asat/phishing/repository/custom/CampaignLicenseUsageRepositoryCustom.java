package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;

import java.util.List;

/**
 * Aggregation queries for unique phishing license usage.
 */
public interface CampaignLicenseUsageRepositoryCustom {

    /**
     * Counts rows in phishing_user_licence (one seat per unique user assignment),
     * grouped by productPackageId. Optional productPackageId filter narrows results.
     * {@code clientId} is the client admin id.
     */
    List<CampaignLicenseUsageDto> countUniqueUsersByProductPackageId(String clientId, String productPackageId);
}
