package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.dashboard.LicenseDistributionResponseDto;
import com.aspire.asat.registration.data.dashboard.UserStatusCountResponseDto;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.msp.MspProductRepository;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    private static final String MSP_ID = "msp-123";

    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private MspProductRepository mspProductRepository;
    @Mock private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getUserStatusCounts_globalWhenNoMspScope() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(aspireAdminContext());
        when(aspireUserRepository.countByStatus("ACTIVE")).thenReturn(10L);
        when(aspireUserRepository.countByStatus("INACTIVE")).thenReturn(3L);
        when(aspireUserRepository.countByStatus("SUSPEND")).thenReturn(2L);

        UserStatusCountResponseDto result = dashboardService.getUserStatusCounts(null);

        assertEquals(10L, result.getTotalActive());
        assertEquals(3L, result.getTotalInactive());
        assertEquals(2L, result.getTotalSuspended());
        verify(clientAdminRepository, never()).findAllByMspId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getUserStatusCounts_usesRequestMspId() {
        when(clientAdminRepository.findAllByMspId(MSP_ID)).thenReturn(List.of(
                ClientAdmin.builder().id("ca-1").build(),
                ClientAdmin.builder().id("ca-2").build()
        ));
        when(aspireUserRepository.countByStatusScoped("ACTIVE", List.of("ca-1", "ca-2"))).thenReturn(7L);
        when(aspireUserRepository.countByStatusScoped("INACTIVE", List.of("ca-1", "ca-2"))).thenReturn(1L);
        when(aspireUserRepository.countByStatusScoped("SUSPEND", List.of("ca-1", "ca-2"))).thenReturn(2L);

        UserStatusCountResponseDto result = dashboardService.getUserStatusCounts(MSP_ID);

        assertEquals(7L, result.getTotalActive());
        assertEquals(1L, result.getTotalInactive());
        assertEquals(2L, result.getTotalSuspended());
        verify(clientAdminRepository).findAllByMspId(MSP_ID);
    }

    @Test
    void getUserStatusCounts_mspCallerUsesContextUserIdWhenMspIdNull() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(MSP_ID));
        when(clientAdminRepository.findAllByMspId(MSP_ID)).thenReturn(List.of(
                ClientAdmin.builder().id("ca-1").build()
        ));
        when(aspireUserRepository.countByStatusScoped(eq("ACTIVE"), eq(List.of("ca-1")))).thenReturn(4L);
        when(aspireUserRepository.countByStatusScoped(eq("INACTIVE"), eq(List.of("ca-1")))).thenReturn(0L);
        when(aspireUserRepository.countByStatusScoped(eq("SUSPEND"), eq(List.of("ca-1")))).thenReturn(1L);

        UserStatusCountResponseDto result = dashboardService.getUserStatusCounts(null);

        assertEquals(4L, result.getTotalActive());
        assertEquals(0L, result.getTotalInactive());
        assertEquals(1L, result.getTotalSuspended());
        verify(clientAdminRepository).findAllByMspId(MSP_ID);
    }

    @Test
    void getUserStatusCounts_returnsZerosWhenMspHasNoClients() {
        when(clientAdminRepository.findAllByMspId(MSP_ID)).thenReturn(List.of());

        UserStatusCountResponseDto result = dashboardService.getUserStatusCounts(MSP_ID);

        assertEquals(0L, result.getTotalActive());
        assertEquals(0L, result.getTotalInactive());
        assertEquals(0L, result.getTotalSuspended());
        verify(aspireUserRepository, never()).countByStatusScoped(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getLicenseDistribution_globalForAspireAdmin() {
        Instant future = Instant.now().plus(30, ChronoUnit.DAYS);
        Instant past = Instant.now().minus(10, ChronoUnit.DAYS);

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(aspireAdminContext());
        when(mspProductRepository.findAll()).thenReturn(List.of(
                MspProduct.builder().licenseCount(100).usedLicenseCount(40).expiryDate(future).build(),
                MspProduct.builder().licenseCount(50).usedLicenseCount(20).expiryDate(past).build()
        ));

        LicenseDistributionResponseDto result = dashboardService.getLicenseDistribution(null);

        // allocated = 100 + 50
        assertEquals(150L, result.getTotalAllocated());
        // available = (100-40) for non-expired only
        assertEquals(60L, result.getTotalAvailable());
        // active = 40 for non-expired only
        assertEquals(40L, result.getTotalActive());
        // expired = (50-20) for expired only
        assertEquals(30L, result.getTotalExpired());
        verify(mspProductRepository).findAll();
        verify(mspProductRepository, never()).findByMspId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getLicenseDistribution_scopesByMspIdWhenProvided() {
        Instant future = Instant.now().plus(30, ChronoUnit.DAYS);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).licenseCount(80).usedLicenseCount(25).expiryDate(future).build()
        ));

        LicenseDistributionResponseDto result = dashboardService.getLicenseDistribution(MSP_ID);

        assertEquals(80L, result.getTotalAllocated());
        assertEquals(55L, result.getTotalAvailable());
        assertEquals(25L, result.getTotalActive());
        assertEquals(0L, result.getTotalExpired());
        verify(mspProductRepository).findByMspId(MSP_ID);
        verify(mspProductRepository, never()).findAll();
    }

    @Test
    void getLicenseDistribution_mspCallerUsesContextUserIdWhenMspIdNull() {
        Instant past = Instant.now().minus(5, ChronoUnit.DAYS);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(MSP_ID));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).licenseCount(40).usedLicenseCount(10).expiryDate(past).build()
        ));

        LicenseDistributionResponseDto result = dashboardService.getLicenseDistribution(null);

        assertEquals(40L, result.getTotalAllocated());
        assertEquals(0L, result.getTotalAvailable());
        assertEquals(0L, result.getTotalActive());
        assertEquals(30L, result.getTotalExpired());
        verify(mspProductRepository).findByMspId(MSP_ID);
    }

    @Test
    void getLicenseDistribution_systemUserGetsGlobalCounts() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(systemUserContext());
        when(mspProductRepository.findAll()).thenReturn(List.of());

        LicenseDistributionResponseDto result = dashboardService.getLicenseDistribution(null);

        assertEquals(0L, result.getTotalAllocated());
        assertEquals(0L, result.getTotalAvailable());
        assertEquals(0L, result.getTotalActive());
        assertEquals(0L, result.getTotalExpired());
        verify(mspProductRepository).findAll();
    }

    private CurrentUserContext aspireAdminContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.ASPIRE_ADMIN.name());
        context.setUserId("admin-1");
        return context;
    }

    private CurrentUserContext systemUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.SYSTEM_USER.name());
        context.setUserId("system-1");
        return context;
    }

    private CurrentUserContext mspContext(String mspId) {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserType(UserType.MSP.name());
        context.setUserId(mspId);
        return context;
    }
}
