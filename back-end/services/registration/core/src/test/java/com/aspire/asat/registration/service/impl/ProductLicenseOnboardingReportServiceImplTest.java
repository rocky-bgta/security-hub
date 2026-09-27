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
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductLicenseOnboardingReportServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private ClientProductRepositoryCustom clientProductRepositoryCustom;
    @Mock
    private UserLicenceRepository userLicenceRepository;
    @Mock
    private EndUserPackageRepository endUserPackageRepository;
    @Mock
    private AspireUserRepository aspireUserRepository;
    @Mock
    private CmsServiceClient cmsServiceClient;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private ProductLicenseOnboardingReportServiceImpl productLicenseOnboardingReportService;

    @Test
    void getReport_aggregatesAllMetrics() {
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, null))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(20, 6));
        when(userLicenceRepository.countByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(10L);
        when(aspireUserRepository.countByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(10L);
        when(aspireUserRepository.countByClientAdminIdAndIsCredentialSent(CLIENT_ADMIN_ID, false)).thenReturn(4L);
        when(aspireUserRepository.countByClientAdminIdAndIsCredentialSent(CLIENT_ADMIN_ID, true)).thenReturn(6L);
        when(aspireUserRepository.countByClientAdminIdAndStatus(CLIENT_ADMIN_ID, "SUSPEND")).thenReturn(0L);
        when(endUserPackageRepository.countByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(6L);
        when(cmsServiceClient.fetchTrainingCertificateCounts(CLIENT_ADMIN_ID))
                .thenReturn(CmsTrainingCertificateCountResponseDto.builder()
                        .trainingCompletedCount(1)
                        .certificateEarnedCount(2)
                        .build());

        ProductLicenseOnboardingReportDto report =
                productLicenseOnboardingReportService.getReport(CLIENT_ADMIN_ID);

        assertEquals(20L, report.getTotalLicenses());
        assertEquals(6L, report.getActiveUsers());
        assertEquals(4L, report.getPendingActivation());
        assertEquals(10L, report.getAssignedLicenses());
        assertEquals(10L, report.getUnusedLicenses());
        assertEquals(0L, report.getSuspendedAccounts());
        assertEquals(10L, report.getAccountCreated());
        assertEquals(6L, report.getAccountActivated());
        assertEquals(6L, report.getTrainingAssigned());
        assertEquals(1L, report.getTrainingCompleted());
        assertEquals(2L, report.getCertificateEarned());

        verify(cmsServiceClient).fetchTrainingCertificateCounts(CLIENT_ADMIN_ID);
    }

    @Test
    void resolveClientAdminId_usesContextWhenQueryParamMissing() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .build());

        assertEquals(CLIENT_ADMIN_ID, productLicenseOnboardingReportService.resolveClientAdminId(null));
    }

    @Test
    void resolveClientAdminId_throwsWhenMissingAndNotClientAdmin() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .userId("msp-1")
                .userType(UserType.MSP.name())
                .build());

        assertThrows(IllegalArgumentException.class,
                () -> productLicenseOnboardingReportService.resolveClientAdminId(null));
    }
}
