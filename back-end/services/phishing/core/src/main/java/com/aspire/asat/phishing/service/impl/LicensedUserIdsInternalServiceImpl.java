package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import com.aspire.asat.phishing.service.LicensedUserIdsInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicensedUserIdsInternalServiceImpl implements LicensedUserIdsInternalService {

    private final PhishingUserLicenceRepository phishingUserLicenceRepository;

    @Override
    public List<String> getLicensedUserIds(String clientId, String productPackageId) {
        if (!StringUtils.hasText(clientId)) {
            throw new PhishingValidationException("clientId is required");
        }
        if (!StringUtils.hasText(productPackageId)) {
            throw new PhishingValidationException("productPackageId is required");
        }
        return phishingUserLicenceRepository.findLicensedUserIds(
                clientId.trim(), productPackageId.trim());
    }
}
