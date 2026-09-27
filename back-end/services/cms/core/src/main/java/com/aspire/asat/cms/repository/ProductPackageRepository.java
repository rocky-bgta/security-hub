package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ProductPackage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductPackageRepository extends MongoRepository<ProductPackage, String> {
    List<ProductPackage> findByProductId(String productId);
    void deleteByProductId(String productId);
}

