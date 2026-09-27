package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import com.aspire.asat.phishing.repository.custom.CampaignLicenseUsageRepositoryCustom;
import com.aspire.asat.phishing.service.CampaignLicenseUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignLicenseUsageServiceImpl implements CampaignLicenseUsageService {

    private final CampaignLicenseUsageRepositoryCustom campaignLicenseUsageRepositoryCustom;

    @Override
    public List<CampaignLicenseUsageDto> getCampaignLicenseUsage(String clientId, String productPackageId) {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("clientId is required");
        }
        List<CampaignLicenseUsageDto> usage =
                campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(clientId, productPackageId);
        log.info("Campaign license usage for clientId={}, productPackageId={}: {} rows",
                clientId, productPackageId, usage.size());
        return usage;
    }
}
