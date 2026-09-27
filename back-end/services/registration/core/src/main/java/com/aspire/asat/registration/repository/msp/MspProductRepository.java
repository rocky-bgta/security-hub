package com.aspire.asat.registration.repository.msp;

import com.aspire.asat.registration.model.msp.MspProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MSP Product operations
 */
@Repository
public interface MspProductRepository extends MongoRepository<MspProduct, String> {

    /**
     * Find all products by MSP ID
     */
    List<MspProduct> findByMspId(String mspId);

    /**
     * Find products by MSP ID and product ID
     */
    List<MspProduct> findByMspIdAndProductId(String mspId, String productId);

    /**
     * Find product by MSP ID, product ID, and package ID
     */
    MspProduct findByMspIdAndProductIdAndPackageId(String mspId, String productId, String packageId);

    /**
     * Count products by MSP ID
     */
    long countByMspId(String mspId);

    /**
     * Find all products by multiple MSP IDs (batch fetch)
     */
    List<MspProduct> findByMspIdIn(List<String> mspIds);
}

