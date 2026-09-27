package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.cms.response.CmsTrainingCertificateCountResponseDto;
import com.aspire.asat.registration.data.reports.ProductLicenseOnboardingReportDto;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.service.ProductLicenseOnboardingReportService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductLicenseOnboardingReportServiceImpl implements ProductLicenseOnboardingReportService {

    private static final String STATUS_SUSPEND = "SUSPEND";

    private final ClientProductRepositoryCustom clientProductRepositoryCustom;
    private final UserLicenceRepository userLicenceRepository;
    private final EndUserPackageRepository endUserPackageRepository;
    private final AspireUserRepository aspireUserRepository;
    private final CmsServiceClient cmsServiceClient;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ProductLicenseOnboardingReportDto getReport(String clientAdminId) {
        String resolvedClientAdminId = resolveClientAdminId(clientAdminId);
        log.info("Building product license onboarding report for clientAdminId={}", resolvedClientAdminId);

        ClientProductRepositoryCustom.LicenseStatistics licenseStats =
                clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(resolvedClientAdminId, null);

        long totalLicenses = licenseStats.getTotalLicenseCount();
        long activeUsers = licenseStats.getTotalUsedLicenseCount();
        long assignedLicenses = userLicenceRepository.countByClientAdminId(resolvedClientAdminId);
        long unusedLicenses = Math.max(0, totalLicenses - assignedLicenses);

        long accountCreated = aspireUserRepository.countByClientAdminId(resolvedClientAdminId);
        long pendingActivation = aspireUserRepository.countByClientAdminIdAndIsCredentialSent(resolvedClientAdminId, false);
        long accountActivated = aspireUserRepository.countByClientAdminIdAndIsCredentialSent(resolvedClientAdminId, true);
        long suspendedAccounts = aspireUserRepository.countByClientAdminIdAndStatus(resolvedClientAdminId, STATUS_SUSPEND);

        long trainingAssigned = endUserPackageRepository.countByClientAdminId(resolvedClientAdminId);

        CmsTrainingCertificateCountResponseDto cmsCounts =
                cmsServiceClient.fetchTrainingCertificateCounts(resolvedClientAdminId);
        long trainingCompleted = cmsCounts != null ? cmsCounts.getTrainingCompletedCount() : 0L;
        long certificateEarned = cmsCounts != null ? cmsCounts.getCertificateEarnedCount() : 0L;

        return ProductLicenseOnboardingReportDto.builder()
                .totalLicenses(totalLicenses)
                .activeUsers(activeUsers)
                .pendingActivation(pendingActivation)
                .assignedLicenses(assignedLicenses)
                .unusedLicenses(unusedLicenses)
                .suspendedAccounts(suspendedAccounts)
                .accountCreated(accountCreated)
                .accountActivated(accountActivated)
                .trainingAssigned(trainingAssigned)
                .trainingCompleted(trainingCompleted)
                .certificateEarned(certificateEarned)
                .build();
    }

    String resolveClientAdminId(String clientAdminId) {
        if (StringUtils.hasText(clientAdminId)) {
            return clientAdminId.trim();
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && UserType.CLIENT_ADMIN.name().equals(userContext.getUserType())) {
            return userContext.getUserId();
        }

        throw new IllegalArgumentException("clientAdminId is required when the current user is not a CLIENT_ADMIN");
    }
}
