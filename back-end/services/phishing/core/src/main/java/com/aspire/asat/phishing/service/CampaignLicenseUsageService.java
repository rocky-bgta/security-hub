package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;

import java.util.List;

public interface CampaignLicenseUsageService {

    /**
     * Unique campaign participants per productPackageId for the given client.
     *
     * @param clientId          campaign client admin id (required)
     * @param productPackageId  optional ClientProduct.id filter
     */
    List<CampaignLicenseUsageDto> getCampaignLicenseUsage(String clientId, String productPackageId);
}
