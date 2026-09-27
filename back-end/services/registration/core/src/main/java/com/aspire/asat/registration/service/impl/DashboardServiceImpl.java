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
import com.aspire.asat.registration.service.DashboardService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service implementation for dashboard-related operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String STATUS_SUSPEND = "SUSPEND";

    private final AspireUserRepository aspireUserRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final MspProductRepository mspProductRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public UserStatusCountResponseDto getUserStatusCounts(String mspId) {
        String resolvedMspId = resolveMspId(mspId);

        if (resolvedMspId != null) {
            return getMspScopedUserStatusCounts(resolvedMspId);
        }

        log.info("Fetching global user status counts from AspireUser table");

        long totalActive = aspireUserRepository.countByStatus(STATUS_ACTIVE);
        long totalInactive = aspireUserRepository.countByStatus(STATUS_INACTIVE);
        long totalSuspended = aspireUserRepository.countByStatus(STATUS_SUSPEND);

        log.info("User status counts - Active: {}, Inactive: {}, Suspended: {}",
                totalActive, totalInactive, totalSuspended);

        return UserStatusCountResponseDto.builder()
                .totalActive(totalActive)
                .totalInactive(totalInactive)
                .totalSuspended(totalSuspended)
                .build();
    }

    @Override
    public LicenseDistributionResponseDto getLicenseDistribution(String mspId) {
        String resolvedMspId = resolveMspId(mspId);

        List<MspProduct> products = resolvedMspId != null
                ? mspProductRepository.findByMspId(resolvedMspId)
                : mspProductRepository.findAll();

        log.info("Fetching license distribution resolvedMspId={} productCount={}",
                resolvedMspId, products.size());

        return calculateLicenseDistribution(products);
    }

    private LicenseDistributionResponseDto calculateLicenseDistribution(List<MspProduct> products) {
        Instant now = Instant.now();
        long totalAvailable = 0L;
        long totalAllocated = 0L;
        long totalActive = 0L;
        long totalExpired = 0L;

        for (MspProduct product : products) {
            if (product == null) {
                continue;
            }

            int licenseCount = product.getLicenseCount();
            int usedLicenseCount = product.getUsedLicenseCount();
            int unused = licenseCount - usedLicenseCount;

            totalAllocated += licenseCount;

            Instant expiryDate = product.getExpiryDate();
            if (expiryDate == null) {
                continue;
            }

            if (expiryDate.isAfter(now)) {
                totalAvailable += unused;
                totalActive += usedLicenseCount;
            } else if (expiryDate.isBefore(now)) {
                totalExpired += unused;
            }
        }

        return LicenseDistributionResponseDto.builder()
                .totalAvailable(totalAvailable)
                .totalAllocated(totalAllocated)
                .totalActive(totalActive)
                .totalExpired(totalExpired)
                .build();
    }

    private UserStatusCountResponseDto getMspScopedUserStatusCounts(String mspId) {
        log.info("Fetching MSP-scoped user status counts for mspId={}", mspId);

        List<String> clientAdminIds = clientAdminRepository.findAllByMspId(mspId).stream()
                .map(ClientAdmin::getId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        if (clientAdminIds.isEmpty()) {
            log.info("No client admins found for mspId={}, returning zero counts", mspId);
            return UserStatusCountResponseDto.builder()
                    .totalActive(0L)
                    .totalInactive(0L)
                    .totalSuspended(0L)
                    .build();
        }

        long totalActive = aspireUserRepository.countByStatusScoped(STATUS_ACTIVE, clientAdminIds);
        long totalInactive = aspireUserRepository.countByStatusScoped(STATUS_INACTIVE, clientAdminIds);
        long totalSuspended = aspireUserRepository.countByStatusScoped(STATUS_SUSPEND, clientAdminIds);

        log.info("MSP {} user status counts ({} clients) - Active: {}, Inactive: {}, Suspended: {}",
                mspId, clientAdminIds.size(), totalActive, totalInactive, totalSuspended);

        return UserStatusCountResponseDto.builder()
                .totalActive(totalActive)
                .totalInactive(totalInactive)
                .totalSuspended(totalSuspended)
                .build();
    }

    /**
     * Explicit mspId wins. If omitted and caller is MSP, use context userId as mspId.
     * Otherwise return null for global (Aspire Admin / System User) counts.
     */
    private String resolveMspId(String requestMspId) {
        if (requestMspId != null && !requestMspId.isBlank()) {
            return requestMspId.trim();
        }

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        if (context == null) {
            return null;
        }

        if (isMspUser(context)
                && context.getUserId() != null
                && !context.getUserId().isBlank()) {
            return context.getUserId().trim();
        }

        return null;
    }

    private boolean isMspUser(CurrentUserContext context) {
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return UserType.MSP.name().equalsIgnoreCase(context.getUserType());
        }
    }
}
