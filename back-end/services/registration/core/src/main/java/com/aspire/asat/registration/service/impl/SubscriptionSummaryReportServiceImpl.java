package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.reports.QuickRange;
import com.aspire.asat.registration.data.reports.SubscriptionDetailRowDTO;
import com.aspire.asat.registration.data.reports.SubscriptionSummaryTotalsDTO;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.service.SubscriptionSummaryReportService;
import com.aspire.asat.registration.utils.CsvUtil;
import com.aspire.asat.registration.utils.QuickRangeResolver;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Composes the Subscription Summary Report from {@link ClientProductRepositoryCustom}
 * (DB-narrowed by scope + assignedAt range), enriching each record with client,
 * product and package names. Product/package names are resolved cache-first via
 * {@link CmsServiceClient#getFullProductFromCache(String)} over the distinct
 * product ids only, and client names via a single batched
 * {@link ClientAdminRepository#findAllById(Iterable)} lookup - so the hot path
 * performs no per-row remote/DB calls. Name search, status derivation and
 * pagination happen in-service because product/package names live in the CMS.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionSummaryReportServiceImpl implements SubscriptionSummaryReportService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ClientProductRepositoryCustom clientProductRepositoryCustom;
    private final ClientAdminRepository clientAdminRepository;
    private final CmsServiceClient cmsServiceClient;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public SubscriptionSummaryTotalsDTO getSummary(String search,
                                                   String clientAdminId,
                                                   String mspId,
                                                   String status,
                                                   Instant fromDate,
                                                   Instant toDateExclusive,
                                                   QuickRange quickRange) {
        log.info("Building subscription summary - clientAdminId: {}, mspId: {}, status: {}, search: {}, fromDate: {}, toDateExclusive: {}, quickRange: {}",
                clientAdminId, mspId, status, search, fromDate, toDateExclusive, quickRange);

        List<EnrichedSubscription> rows = buildEnrichedSubscriptions(
                search, clientAdminId, mspId, status, fromDate, toDateExclusive, quickRange);
        return aggregate(rows);
    }

    @Override
    public AllResponseDto<List<SubscriptionDetailRowDTO>> getDetailList(String search,
                                                                        String clientAdminId,
                                                                        String mspId,
                                                                        String status,
                                                                        Instant fromDate,
                                                                        Instant toDateExclusive,
                                                                        QuickRange quickRange,
                                                                        int offset,
                                                                        int pageSize) {
        log.info("Building subscription detail list - clientAdminId: {}, mspId: {}, status: {}, search: {}, fromDate: {}, toDateExclusive: {}, quickRange: {}, offset: {}, pageSize: {}",
                clientAdminId, mspId, status, search, fromDate, toDateExclusive, quickRange, offset, pageSize);

        List<EnrichedSubscription> rows = buildEnrichedSubscriptions(
                search, clientAdminId, mspId, status, fromDate, toDateExclusive, quickRange);

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        long total = rows.size();
        int skip = safeOffset * safePageSize;

        List<SubscriptionDetailRowDTO> pageItems;
        if (skip >= rows.size()) {
            pageItems = List.of();
        } else {
            pageItems = rows.subList(skip, Math.min(skip + safePageSize, rows.size()))
                    .stream()
                    .map(EnrichedSubscription::row)
                    .collect(Collectors.toList());
        }

        return new AllResponseDto<>(safeOffset, safePageSize, total, pageItems);
    }

    @Override
    public byte[] exportCsv(String search,
                            String clientAdminId,
                            String mspId,
                            String status,
                            Instant fromDate,
                            Instant toDateExclusive,
                            QuickRange quickRange) {
        log.info("Exporting subscription summary CSV - clientAdminId: {}, mspId: {}, status: {}, search: {}, fromDate: {}, toDateExclusive: {}, quickRange: {}",
                clientAdminId, mspId, status, search, fromDate, toDateExclusive, quickRange);

        List<EnrichedSubscription> rows = buildEnrichedSubscriptions(
                search, clientAdminId, mspId, status, fromDate, toDateExclusive, quickRange);
        return buildCsv(rows);
    }

    /**
     * Resolve scope + date window, fetch the DB-narrowed set, enrich with names,
     * then apply the in-service status + search filters. The returned list is
     * shared by all three endpoints.
     */
    private List<EnrichedSubscription> buildEnrichedSubscriptions(String search,
                                                                  String clientAdminId,
                                                                  String mspId,
                                                                  String status,
                                                                  Instant fromDate,
                                                                  Instant toDateExclusive,
                                                                  QuickRange quickRange) {
        ReportScope scope = resolveScope(clientAdminId, mspId);
        QuickRangeResolver.DateRange range = QuickRangeResolver.resolve(quickRange, fromDate, toDateExclusive);

        List<ClientProduct> products = clientProductRepositoryCustom.findSubscriptionsForReport(
                scope.clientAdminId(), scope.mspId(), range.from(), range.toExclusive());

        if (products.isEmpty()) {
            return List.of();
        }

        Map<String, String> clientNames = resolveClientNames(products);
        Map<String, String> productNames = new HashMap<>();
        Map<String, String> packageNames = new HashMap<>();
        resolveProductAndPackageNames(products, productNames, packageNames);

        Instant now = Instant.now();
        String normalizedStatus = normalize(status);
        String normalizedSearch = normalize(search);
        String searchLower = normalizedSearch == null ? null : normalizedSearch.toLowerCase();

        List<EnrichedSubscription> enriched = new ArrayList<>(products.size());
        for (ClientProduct product : products) {
            String derivedStatus = deriveStatus(product, now);

            if (normalizedStatus != null && !normalizedStatus.equalsIgnoreCase(derivedStatus)) {
                continue;
            }

            String clientName = clientNames.getOrDefault(product.getClientAdminId(), product.getClientAdminId());
            String productName = productNames.getOrDefault(product.getProductId(), product.getProductId());
            String packageName = packageNames.getOrDefault(product.getPackageId(), product.getPackageId());

            if (searchLower != null && !matchesSearch(searchLower, clientName, productName, packageName)) {
                continue;
            }

            SubscriptionDetailRowDTO row = SubscriptionDetailRowDTO.builder()
                    .clientName(clientName)
                    .productName(productName)
                    .packageName(packageName)
                    .startDate(product.getAssignedAt())
                    .endDate(product.getExpiryDate())
                    .status(derivedStatus)
                    .totalLicense(product.getLicenseCount())
                    .usedLicense(product.getUsedLicenseCount())
                    .clientAdminId(product.getClientAdminId())
                    .productId(product.getProductId())
                    .packageId(product.getPackageId())
                    .build();

            enriched.add(new EnrichedSubscription(product, row));
        }

        return enriched;
    }

    /**
     * Auto-scope by current user: CLIENT_ADMIN -> own clientAdminId; MSP -> own
     * mspId (plus an optional explicit clientAdminId narrowing); other user types
     * honour the requested filters as-is.
     */
    private ReportScope resolveScope(String requestedClientAdminId, String requestedMspId) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        UserType userType = safeUserType(context.getUserType());

        if (UserType.CLIENT_ADMIN.equals(userType)) {
            return new ReportScope(context.getClientAdminId(), null);
        }
        if (UserType.MSP.equals(userType)) {
            return new ReportScope(normalize(requestedClientAdminId), context.getMspId());
        }
        return new ReportScope(normalize(requestedClientAdminId), normalize(requestedMspId));
    }

    private Map<String, String> resolveClientNames(List<ClientProduct> products) {
        Set<String> clientAdminIds = products.stream()
                .map(ClientProduct::getClientAdminId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        if (clientAdminIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> names = new HashMap<>();
        for (ClientAdmin admin : clientAdminRepository.findAllById(clientAdminIds)) {
            names.put(admin.getId(), admin.getOrganizationName());
        }
        return names;
    }

    /**
     * Populate productId->name and packageId->name maps using the CMS cache,
     * iterating the DISTINCT product ids only (cache-first, no per-row calls).
     */
    private void resolveProductAndPackageNames(List<ClientProduct> products,
                                               Map<String, String> productNames,
                                               Map<String, String> packageNames) {
        Set<String> productIds = products.stream()
                .map(ClientProduct::getProductId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        for (String productId : productIds) {
            CmsFullProductResponseDto product = cmsServiceClient.getFullProductFromCache(productId);
            if (product == null) {
                continue;
            }
            if (product.getProductName() != null) {
                productNames.put(productId, product.getProductName());
            }
            if (product.getPackages() != null) {
                for (CmsPackageDto pkg : product.getPackages()) {
                    if (pkg.getId() != null && pkg.getPackageName() != null) {
                        packageNames.put(pkg.getId(), pkg.getPackageName());
                    }
                }
            }
        }
    }

    private SubscriptionSummaryTotalsDTO aggregate(List<EnrichedSubscription> rows) {
        Set<String> productIds = new HashSet<>();
        Set<String> packageIds = new HashSet<>();
        long totalLicenses = 0;
        long activeLicenses = 0;
        long pendingLicenses = 0;
        long expiredLicenses = 0;

        for (EnrichedSubscription enriched : rows) {
            ClientProduct product = enriched.product();
            String derivedStatus = enriched.row().getStatus();

            if (product.getProductId() != null) {
                productIds.add(product.getProductId());
            }
            if (product.getPackageId() != null) {
                packageIds.add(product.getPackageId());
            }

            totalLicenses += product.getLicenseCount();

            boolean expired = STATUS_EXPIRED.equalsIgnoreCase(derivedStatus);
            if (expired) {
                expiredLicenses += product.getLicenseCount();
            } else {
                activeLicenses += product.getUsedLicenseCount();
            }

            if (STATUS_PENDING.equalsIgnoreCase(product.getLicenseStatus())) {
                pendingLicenses += product.getLicenseCount();
            }
        }

        long unusedLicenses = totalLicenses - activeLicenses;

        return SubscriptionSummaryTotalsDTO.builder()
                .totalProducts(productIds.size())
                .totalPackages(packageIds.size())
                .totalLicenses(totalLicenses)
                .activeLicenses(activeLicenses)
                .unusedLicenses(unusedLicenses)
                .pendingLicenses(pendingLicenses)
                .expiredLicenses(expiredLicenses)
                .build();
    }

    private byte[] buildCsv(List<EnrichedSubscription> rows) {
        StringBuilder sb = CsvUtil.newCsv();
        sb.append("Client Name,Product Name,Package Name,Start Date,End Date,Status,Total License,Used License\n");

        for (EnrichedSubscription enriched : rows) {
            SubscriptionDetailRowDTO row = enriched.row();
            sb.append(CsvUtil.escapeCsv(row.getClientName())).append(',');
            sb.append(CsvUtil.escapeCsv(row.getProductName())).append(',');
            sb.append(CsvUtil.escapeCsv(row.getPackageName())).append(',');
            sb.append(row.getStartDate() != null ? row.getStartDate().toString() : "").append(',');
            sb.append(row.getEndDate() != null ? row.getEndDate().toString() : "").append(',');
            sb.append(CsvUtil.escapeCsv(row.getStatus())).append(',');
            sb.append(row.getTotalLicense()).append(',');
            sb.append(row.getUsedLicense()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String deriveStatus(ClientProduct product, Instant now) {
        if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(now)) {
            return STATUS_EXPIRED;
        }
        return product.getLicenseStatus();
    }

    private static boolean matchesSearch(String searchLower, String clientName, String productName, String packageName) {
        return containsIgnoreCase(clientName, searchLower)
                || containsIgnoreCase(productName, searchLower)
                || containsIgnoreCase(packageName, searchLower);
    }

    private static boolean containsIgnoreCase(String value, String searchLower) {
        return value != null && value.toLowerCase().contains(searchLower);
    }

    private UserType safeUserType(String userType) {
        try {
            return UserType.fromString(userType);
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized userType '{}' in current context; treating as system scope", userType);
            return null;
        }
    }

    private static String normalize(String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    private record ReportScope(String clientAdminId, String mspId) {
    }

    private record EnrichedSubscription(ClientProduct product, SubscriptionDetailRowDTO row) {
    }
}
