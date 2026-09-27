package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserLicence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLicenceRepository extends MongoRepository<UserLicence, String> {

    boolean existsByUserIdAndPackageId(String userId, String packageId);
}