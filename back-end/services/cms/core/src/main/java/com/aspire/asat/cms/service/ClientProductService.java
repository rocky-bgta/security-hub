package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.client.ClientPackageSimpleResponse;
import com.aspire.asat.cms.dto.client.ClientProductSimpleResponse;

import java.util.List;

/**
 * Service interface for client product operations
 */
public interface ClientProductService {
    
    /**
     * Get list of unique products assigned to a client admin
     * Products are retrieved from ClientProductReplica model
     * and enriched with Product model data
     * 
     * @param clientAdminId the client admin ID
     * @return List of unique products with id, productName, and thumbnailUrl
     */
    List<ClientProductSimpleResponse> getUniqueProductsByClientAdminId(String clientAdminId);
    
    /**
     * Get list of packages assigned to a client admin for a specific product
     * Packages are retrieved from ClientProductReplica model
     * and enriched with ProductPackage model data
     * 
     * @param clientAdminId the client admin ID
     * @param productId the product ID
     * @return List of packages with id and name
     */
    List<ClientPackageSimpleResponse> getPackagesByClientAdminIdAndProductId(String clientAdminId, String productId);
}

