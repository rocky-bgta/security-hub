package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.enums.ProductStatus;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRepository extends MongoRepository<Feature, String> {

    boolean existsByFeatureName(String featureName);

    @Query("{ 'featureName': { $regex: ?0, $options: 'i' } }")
    Page<Feature> findByFeatureName(String text, Pageable pageable);

    Page<Feature> findByFeatureStatus(FeatureStatus status, Pageable pageable);
}
