package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.PackageUserRangePricing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageUserRangePricingRepository extends MongoRepository<PackageUserRangePricing, String> {

    List<PackageUserRangePricing> findByPackageIdAndIsActiveTrue(String packageId);

    Optional<PackageUserRangePricing> findByPackageIdAndUserRangeIdAndIsActiveTrue(String packageId, String userRangeId);

    boolean existsByPackageIdAndUserRangeIdAndIsActiveTrue(String packageId, String userRangeId);

    void deleteByPackageId(String packageId);

    List<PackageUserRangePricing> findByUserRangeIdAndIsActiveTrue(String userRangeId);
}

