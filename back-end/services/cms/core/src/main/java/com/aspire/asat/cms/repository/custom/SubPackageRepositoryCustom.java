package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.model.SubPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubPackageRepositoryCustom {
    
    /**
     * Find SubPackages with dynamic filtering criteria
     * 
     * @param search Search term for SubPackage name (optional)
     * @param status Filter by SubPackage status (optional)
     * @param clientId Filter by client ID (optional)
     * @param productId Filter by product ID (optional)
     * @param clientAdminIds Filter by list of client admin IDs (optional; takes precedence over clientId)
     * @param pageable Pagination and sorting information
     * @return Page of SubPackages matching the criteria
     */
    Page<SubPackage> findSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds,
            Pageable pageable
    );
    
    /**
     * Count SubPackages with dynamic filtering criteria
     * 
     * @param search Search term for SubPackage name (optional)
     * @param status Filter by SubPackage status (optional)
     * @param clientId Filter by client ID (optional)
     * @param productId Filter by product ID (optional)
     * @param clientAdminIds Filter by list of client admin IDs (optional; takes precedence over clientId)
     * @return Total count of SubPackages matching the criteria
     */
    long countSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds
    );
    
    /**
     * Find Trial SubPackages with dynamic filtering criteria (isTrial = true)
     * 
     * @param search Search term for SubPackage name (optional)
     * @param status Filter by SubPackage status (optional)
     * @param clientId Filter by client ID (optional)
     * @param productId Filter by product ID (optional)
     * @param pageable Pagination and sorting information
     * @return Page of Trial SubPackages matching the criteria
     */
    Page<SubPackage> findTrialSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            Pageable pageable
    );
    
    /**
     * Count Trial SubPackages with dynamic filtering criteria (isTrial = true)
     * 
     * @param search Search term for SubPackage name (optional)
     * @param status Filter by SubPackage status (optional)
     * @param clientId Filter by client ID (optional)
     * @param productId Filter by product ID (optional)
     * @return Total count of Trial SubPackages matching the criteria
     */
    long countTrialSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId
    );

    /**
     * Count non-deleted sub-packages scoped for the package assignment report summary.
     */
    long countSubPackagesForReport(String clientAdminId, List<String> clientAdminIds);

    /**
     * Count distinct topicIds placed on non-deleted sub-packages in the given client scope.
     */
    long countDistinctTopicsInSubPackages(String clientAdminId, List<String> clientAdminIds);
}
