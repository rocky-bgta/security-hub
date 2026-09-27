package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.EndUserPackage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndUserPackageRepository extends MongoRepository<EndUserPackage, String> {

    boolean existsByUserIdAndActiveTrue(String userId);
}
