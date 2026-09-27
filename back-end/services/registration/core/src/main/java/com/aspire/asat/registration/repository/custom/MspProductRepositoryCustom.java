package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.model.msp.MspProduct;

public interface MspProductRepositoryCustom {

    /**
     * Find all MSP products with optional filters
     * @param mspId     Optional filter by MSP ID
     * @param productId Optional filter by product ID
     * @param packageId Optional filter by package ID
     * @param country   Optional filter by country (from MspUser)
     * @param search    Optional search by MSP name or email (from MspUser)
     * @return List of MspProduct matching the filters
     */
    java.util.List<MspProduct> findAllWithFilters(String mspId, String productId, String packageId, String country, String search);
}

