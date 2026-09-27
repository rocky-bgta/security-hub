package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.LicenseHistoryItemDto;
import com.aspire.asat.registration.data.superAdmin.response.LicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.MspLicenseHistoryItemDto;
import com.aspire.asat.registration.data.superAdmin.response.MspLicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.repository.custom.MspProductRepositoryCustom;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.SuperAdminService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

    private final ClientProductRepositoryCustom clientProductRepositoryCustom;
    private final MspProductRepositoryCustom mspProductRepositoryCustom;
    private final CmsServiceClient cmsServiceClient;
    private final ClientAdminRepository clientAdminRepository;
    private final MspUsersRepository mspUsersRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public LicenseHistoryPaginatedResponseDto getLicenseHistory(
            String clientAdminId,
            String productId,
            String packageId,
            String country,
            String mspId,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order) {

        log.info("Retrieving license history with filters - clientAdminId: {}, productId: {}, packageId: {}, country: {}, mspId: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                clientAdminId, productId, packageId, country, mspId, search, offset, pageSize, sortBy, order);

        // Set default values and validate
        int pageOffset = offset != null ? Math.max(0, offset) : 0; // Ensure non-negative
        int pageSizeValue = (pageSize != null && pageSize > 0) ? Math.min(pageSize, 100) : 10; // Ensure positive, max 100
        String sortField = sortBy != null ? sortBy : "assignedAt";
        String sortOrder = order != null ? order : "desc";

        // Validate sort field and order
        if (!isValidSortField(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField + ". Valid fields are: assignedAt, expiryDate, licenseCount, usedLicenseCount, licenseStatus");
        }
        if (!isValidSortOrder(sortOrder)) {
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder + ". Valid orders are: asc, desc");
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        // Fetch all matching ClientProduct records with filters (aggregated with ClientAdmin)
        List<ClientProduct> allClientProducts = clientProductRepositoryCustom.findAllWithFilters(
                clientAdminId, productId, packageId, country, mspId, search);

        log.debug("Found {} client products matching filters", allClientProducts.size());

        // Batch fetch ClientAdmin records
        Set<String> clientAdminIds = allClientProducts.stream()
                .map(ClientProduct::getClientAdminId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());

        Map<String, ClientAdmin> clientAdminMap = new HashMap<>();
        if (!clientAdminIds.isEmpty()) {
            List<ClientAdmin> clientAdmins = clientAdminRepository.findAllById(clientAdminIds);
            clientAdminMap = clientAdmins.stream()
                    .collect(Collectors.toMap(ClientAdmin::getId, admin -> admin));
        }

        // Batch fetch MspUser records for unique mspIds
        Set<String> mspIds = clientAdminMap.values().stream()
                .map(ClientAdmin::getMspId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());

        Map<String, MspUser> mspUserMap = new HashMap<>();
        if (!mspIds.isEmpty()) {
            for (String mspIdValue : mspIds) {
                mspUsersRepository.findByMspId(mspIdValue)
                        .ifPresent(mspUser -> mspUserMap.put(mspIdValue, mspUser));
            }
        }

        // Convert to DTOs with CMS enrichment
        // Use a cache to avoid duplicate CMS calls for same product-package combinations
        Map<String, CmsProductWithSinglePackageResponseDto> productPackageCache = new HashMap<>();
        List<LicenseHistoryItemDto> allItems = new ArrayList<>();

        for (ClientProduct clientProduct : allClientProducts) {
            ClientAdmin clientAdmin = clientAdminMap.get(clientProduct.getClientAdminId());
            MspUser mspUser = clientAdmin != null && clientAdmin.getMspId() != null 
                    ? mspUserMap.get(clientAdmin.getMspId()) 
                    : null;
            
            LicenseHistoryItemDto item = convertToLicenseHistoryItemDto(
                    clientProduct, productPackageCache, clientAdmin, mspUser);
            allItems.add(item);
        }

        // Apply sorting
        allItems = applySorting(allItems, sortField, sortOrder);

        // Apply pagination with bounds checking
        long totalCount = allItems.size();
        int startIndex = Math.max(0, Math.min(pageOffset, allItems.size())); // Ensure startIndex is within bounds
        int endIndex = Math.min(startIndex + pageSizeValue, allItems.size());
        
        List<LicenseHistoryItemDto> paginatedItems;
        if (startIndex < allItems.size()) {
            paginatedItems = allItems.subList(startIndex, endIndex);
        } else {
            paginatedItems = new ArrayList<>(); // Return empty list if startIndex is beyond bounds
        }

        // Build response
        return LicenseHistoryPaginatedResponseDto.builder()
                .items(paginatedItems)
                .offset(pageOffset)
                .pageSize(pageSizeValue)
                .total(totalCount)
                .sortBy(sortField)
                .order(sortOrder)
                .clientAdminId(clientAdminId)
                .productId(productId)
                .packageId(packageId)
                .country(country)
                .mspId(mspId)
                .search(search)
                .build();
    }

    /**
     * Convert ClientProduct to LicenseHistoryItemDto with CMS enrichment
     */
    private LicenseHistoryItemDto convertToLicenseHistoryItemDto(
            ClientProduct clientProduct,
            Map<String, CmsProductWithSinglePackageResponseDto> cache,
            ClientAdmin clientAdmin,
            MspUser mspUser) {

        String cacheKey = clientProduct.getProductId() + ":" + clientProduct.getPackageId();
        CmsProductWithSinglePackageResponseDto productPackageDetails = cache.get(cacheKey);

        // Fetch from CMS if not in cache
        if (productPackageDetails == null) {
            try {
                log.debug("Fetching product package details for productId: {}, packageId: {}",
                        clientProduct.getProductId(), clientProduct.getPackageId());

                productPackageDetails = cmsServiceClient.getProductPackageDetails(
                        clientProduct.getProductId(),
                        clientProduct.getPackageId());

                // Cache the result (even if null) to avoid repeated calls
                cache.put(cacheKey, productPackageDetails);
            } catch (Exception e) {
                log.warn("Error fetching product package details for productId: {}, packageId: {}",
                        clientProduct.getProductId(), clientProduct.getPackageId(), e);
                // Cache null to avoid repeated failed calls
                cache.put(cacheKey, null);
            }
        }

        // Extract product and package names
        String productName = null;
        String packageName = null;

        if (productPackageDetails != null) {
            productName = productPackageDetails.getProductName();
            if (productPackageDetails.getPackages() != null) {
                packageName = productPackageDetails.getPackages().getPackageName();
            }
        }

        // Extract client and MSP information
        String clientName = clientAdmin != null ? clientAdmin.getOrganizationName() : null;
        String email = clientAdmin != null ? clientAdmin.getEmail() : null;
        String mspName = mspUser != null ? mspUser.getOrganizationName() : null;

        return LicenseHistoryItemDto.builder()
                .productId(clientProduct.getProductId())
                .productName(productName)
                .packageId(clientProduct.getPackageId())
                .packageName(packageName)
                .licenseCount(clientProduct.getLicenseCount())
                .usedLicenseCount(clientProduct.getUsedLicenseCount())
                .assignedAt(clientProduct.getAssignedAt())
                .expiryDate(clientProduct.getExpiryDate())
                .licenseStatus(clientProduct.getLicenseStatus())
                .clientAdminId(clientProduct.getClientAdminId())
                .clientName(clientName)
                .mspName(mspName)
                .email(email)
                .build();
    }

    /**
     * Validate if the sort field is allowed
     */
    private boolean isValidSortField(String sortField) {
        List<String> allowedSortFields = List.of(
                "assignedAt", "expiryDate", "licenseCount", "usedLicenseCount", "licenseStatus"
        );
        return allowedSortFields.contains(sortField);
    }

    /**
     * Validate if the sort order is valid
     */
    private boolean isValidSortOrder(String sortOrder) {
        return "asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder);
    }

    /**
     * Apply sorting to the list of license history items
     */
    private List<LicenseHistoryItemDto> applySorting(List<LicenseHistoryItemDto> items, String sortField, String sortOrder) {
        return items.stream()
                .sorted((a, b) -> {
                    int comparison = 0;

                    switch (sortField) {
                        case "assignedAt":
                            comparison = compareInstants(a.getAssignedAt(), b.getAssignedAt());
                            break;
                        case "expiryDate":
                            comparison = compareInstants(a.getExpiryDate(), b.getExpiryDate());
                            break;
                        case "licenseCount":
                            comparison = Integer.compare(
                                    a.getLicenseCount() != null ? a.getLicenseCount() : 0,
                                    b.getLicenseCount() != null ? b.getLicenseCount() : 0
                            );
                            break;
                        case "usedLicenseCount":
                            comparison = Integer.compare(
                                    a.getUsedLicenseCount() != null ? a.getUsedLicenseCount() : 0,
                                    b.getUsedLicenseCount() != null ? b.getUsedLicenseCount() : 0
                            );
                            break;
                        case "licenseStatus":
                            String statusA = a.getLicenseStatus() != null ? a.getLicenseStatus() : "";
                            String statusB = b.getLicenseStatus() != null ? b.getLicenseStatus() : "";
                            comparison = statusA.compareTo(statusB);
                            break;
                        default:
                            comparison = 0;
                    }

                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .toList();
    }

    /**
     * Compare two Instant objects safely
     */
    private int compareInstants(Instant a, Instant b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    @Override
    public MspLicenseHistoryPaginatedResponseDto getMspLicenseHistory(
            String mspId,
            String productId,
            String packageId,
            String country,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order) {

        log.info("Retrieving MSP license history with filters - mspId: {}, productId: {}, packageId: {}, country: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                mspId, productId, packageId, country, search, offset, pageSize, sortBy, order);

        // Set default values and validate
        int pageOffset = offset != null ? Math.max(0, offset) : 0; // Ensure non-negative
        int pageSizeValue = (pageSize != null && pageSize > 0) ? Math.min(pageSize, 100) : 10; // Ensure positive, max 100
        String sortField = sortBy != null ? sortBy : "assignedAt";
        String sortOrder = order != null ? order : "desc";

        // Validate sort field and order
        if (!isValidSortField(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField + ". Valid fields are: assignedAt, expiryDate, licenseCount, usedLicenseCount, licenseStatus");
        }
        if (!isValidSortOrder(sortOrder)) {
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder + ". Valid orders are: asc, desc");
        }

        // Fetch all matching MspProduct records with filters
        List<MspProduct> allMspProducts = mspProductRepositoryCustom.findAllWithFilters(
                mspId, productId, packageId, country, search);

        log.debug("Found {} MSP products matching filters", allMspProducts.size());

        // Batch fetch MspUser records by id (MspProduct.mspId holds MspUser document _id)
        Set<String> mspIds = allMspProducts.stream()
                .map(MspProduct::getMspId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());

        Map<String, MspUser> mspUserMap = new HashMap<>();
        if (!mspIds.isEmpty()) {
            List<MspUser> mspUsers = mspUsersRepository.findAllById(mspIds);
            mspUserMap = mspUsers.stream().collect(Collectors.toMap(MspUser::getId, u -> u));
        }

        // Convert to DTOs with CMS enrichment
        // Use a cache to avoid duplicate CMS calls for same product-package combinations
        Map<String, CmsProductWithSinglePackageResponseDto> productPackageCache = new HashMap<>();
        List<MspLicenseHistoryItemDto> allItems = new ArrayList<>();

        for (MspProduct mspProduct : allMspProducts) {
            MspUser mspUser = mspUserMap.get(mspProduct.getMspId());
            MspLicenseHistoryItemDto item = convertToMspLicenseHistoryItemDto(
                    mspProduct, productPackageCache, mspUser);
            allItems.add(item);
        }

        // Apply sorting
        allItems = applyMspSorting(allItems, sortField, sortOrder);

        // Apply pagination with bounds checking
        long totalCount = allItems.size();
        int startIndex = Math.max(0, Math.min(pageOffset, allItems.size())); // Ensure startIndex is within bounds
        int endIndex = Math.min(startIndex + pageSizeValue, allItems.size());
        
        List<MspLicenseHistoryItemDto> paginatedItems;
        if (startIndex < allItems.size()) {
            paginatedItems = allItems.subList(startIndex, endIndex);
        } else {
            paginatedItems = new ArrayList<>(); // Return empty list if startIndex is beyond bounds
        }

        // Build response
        return MspLicenseHistoryPaginatedResponseDto.builder()
                .items(paginatedItems)
                .offset(pageOffset)
                .pageSize(pageSizeValue)
                .total(totalCount)
                .sortBy(sortField)
                .order(sortOrder)
                .mspId(mspId)
                .productId(productId)
                .packageId(packageId)
                .country(country)
                .search(search)
                .build();
    }

    /**
     * Convert MspProduct to MspLicenseHistoryItemDto with CMS enrichment
     */
    private MspLicenseHistoryItemDto convertToMspLicenseHistoryItemDto(
            MspProduct mspProduct,
            Map<String, CmsProductWithSinglePackageResponseDto> cache,
            MspUser mspUser) {

        String cacheKey = mspProduct.getProductId() + ":" + mspProduct.getPackageId();
        CmsProductWithSinglePackageResponseDto productPackageDetails = cache.get(cacheKey);

        // Fetch from CMS if not in cache
        if (productPackageDetails == null) {
            try {
                log.debug("Fetching product package details for productId: {}, packageId: {}",
                        mspProduct.getProductId(), mspProduct.getPackageId());

                productPackageDetails = cmsServiceClient.getProductPackageDetails(
                        mspProduct.getProductId(),
                        mspProduct.getPackageId());

                // Cache the result (even if null) to avoid repeated calls
                cache.put(cacheKey, productPackageDetails);
            } catch (Exception e) {
                log.warn("Error fetching product package details for productId: {}, packageId: {}",
                        mspProduct.getProductId(), mspProduct.getPackageId(), e);
                // Cache null to avoid repeated failed calls
                cache.put(cacheKey, null);
            }
        }

        // Extract product and package names
        String productName = null;
        String packageName = null;

        if (productPackageDetails != null) {
            productName = productPackageDetails.getProductName();
            if (productPackageDetails.getPackages() != null) {
                packageName = productPackageDetails.getPackages().getPackageName();
            }
        }

        // Extract MSP information
        String mspName = mspUser != null ? mspUser.getOrganizationName() : null;
        String email = mspUser != null ? mspUser.getMspAdminEmail() : null;

        return MspLicenseHistoryItemDto.builder()
                .productId(mspProduct.getProductId())
                .productName(productName)
                .packageId(mspProduct.getPackageId())
                .packageName(packageName)
                .licenseCount(mspProduct.getLicenseCount())
                .usedLicenseCount(mspProduct.getUsedLicenseCount())
                .assignedAt(mspProduct.getAssignedAt())
                .expiryDate(mspProduct.getExpiryDate())
                .licenseStatus(mspProduct.getLicenseStatus())
                .mspId(mspProduct.getMspId())
                .mspName(mspName)
                .email(email)
                .build();
    }

    /**
     * Apply sorting to the list of MSP license history items
     */
    private List<MspLicenseHistoryItemDto> applyMspSorting(List<MspLicenseHistoryItemDto> items, String sortField, String sortOrder) {
        return items.stream()
                .sorted((a, b) -> {
                    int comparison = 0;

                    switch (sortField) {
                        case "assignedAt":
                            comparison = compareInstants(a.getAssignedAt(), b.getAssignedAt());
                            break;
                        case "expiryDate":
                            comparison = compareInstants(a.getExpiryDate(), b.getExpiryDate());
                            break;
                        case "licenseCount":
                            comparison = Integer.compare(
                                    a.getLicenseCount() != null ? a.getLicenseCount() : 0,
                                    b.getLicenseCount() != null ? b.getLicenseCount() : 0
                            );
                            break;
                        case "usedLicenseCount":
                            comparison = Integer.compare(
                                    a.getUsedLicenseCount() != null ? a.getUsedLicenseCount() : 0,
                                    b.getUsedLicenseCount() != null ? b.getUsedLicenseCount() : 0
                            );
                            break;
                        case "licenseStatus":
                            String statusA = a.getLicenseStatus() != null ? a.getLicenseStatus() : "";
                            String statusB = b.getLicenseStatus() != null ? b.getLicenseStatus() : "";
                            comparison = statusA.compareTo(statusB);
                            break;
                        default:
                            comparison = 0;
                    }

                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .toList();
    }
}

